/*-
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io DevOps
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.wcm.devops.jenkins.pipeline.credentials.Credential
import io.wcm.devops.jenkins.pipeline.credentials.CredentialConstants
import io.wcm.devops.jenkins.pipeline.credentials.CredentialParser
import io.wcm.devops.jenkins.pipeline.managedfiles.ManagedFile
import io.wcm.devops.jenkins.pipeline.managedfiles.ManagedFileConstants
import io.wcm.devops.jenkins.pipeline.managedfiles.ManagedFileParser
import io.wcm.devops.jenkins.pipeline.utils.PatternMatcher
import io.wcm.devops.jenkins.pipeline.utils.logging.Logger
import io.wcm.devops.jenkins.pipeline.utils.resources.JsonLibraryResource
import net.sf.json.JSON
import org.jenkinsci.plugins.workflow.cps.DSL

/**
 * Test step for auto lookup demonstration
 */
void call() {
  Logger log = new Logger(this)

  String scmUrl = "git@git.company.com/client/project.git"

  // test credential auto lookup
  testCredentialAutoLookup(log, scmUrl, 'GIT-SSH-company-credentials')

  // test maven global settings lookup
  testManagedFileLookup(log, ManagedFileConstants.GLOBAL_MAVEN_SETTINGS_PATH, scmUrl, 'maven-global-settings-for-company')

  // test maven local settings lookup
  testManagedFileLookup(log, ManagedFileConstants.MAVEN_SETTINS_PATH, scmUrl, 'maven-local-settings-for-client')

  // test npm config lookup
  testManagedFileLookup(log, ManagedFileConstants.NPM_CONFIG_USERCONFIG_PATH, scmUrl, 'npm-settings-for-company-repository')

  // test npmrc lookup
  testManagedFileLookup(log, ManagedFileConstants.NPMRC_PATH, scmUrl, 'npmrc-settings-for-company-repository')

  // test bundle-config lookup
  testManagedFileLookup(log, ManagedFileConstants.BUNDLE_CONFIG_PATH, scmUrl, 'bundler-settings-for-company-repository')
}

void testCredentialAutoLookup(Logger log, String scmUrl, String expectedId) {
  // initialize the pattern matcher to reuse it later
  PatternMatcher matcher = new PatternMatcher()
  // load the json
  JsonLibraryResource jsonRes = new JsonLibraryResource((DSL) steps, CredentialConstants.SCM_CREDENTIALS_PATH)
  JSON credentialJson = jsonRes.load()
  // parse the credentials
  CredentialParser parser = new CredentialParser()
  List<Credential> credentials = parser.parse(credentialJson)
  // try to find matching credential
  Credential credential = (Credential) matcher.getBestMatch(scmUrl, credentials)
  if(credential != null) {
    log.info("Found credential for scm url '$scmUrl': '${credential.id}'")
    assertEquals(log, expectedId, credential.id)
  }
  else {
    log.info("no credintials for scm url '$scmUrl' found")  
  }
}

void testManagedFileLookup(Logger log, String jsonPath, String scmUrl, String expectedId) {
  // initialize the pattern matcher to reuse it later
  PatternMatcher matcher = new PatternMatcher()
  // load the json
  JsonLibraryResource jsonRes = new JsonLibraryResource((DSL) steps, jsonPath)
  JSON managedFileJson = jsonRes.load()
  // parse the credentials
  ManagedFileParser parser = new ManagedFileParser()
  List<ManagedFile> managedFiles = parser.parse(managedFileJson)
  // try to find matching managed file
  ManagedFile managedFile = (ManagedFile) matcher.getBestMatch(scmUrl, managedFiles)
  if(managedFile != null) {
    log.info("Found managed file for scm url '$scmUrl': '${managedFile.id}'")
    assertEquals(log, expectedId, managedFile.id)
  }
  else {
    log.info("No managed file found for scm url '$scmUrl'")
  }
}

void assertEquals(Logger log, String expectedId, String actualId) {
  if (expectedId != actualId) {
    error("Assertion error. Expected '$expectedId', actual: '$actualId'")
  } else {
    log.info("Got correct credential/managed file with id: '${expectedId}'")
  }
}
