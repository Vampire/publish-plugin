/*
 * Copyright 2021 the original author or authors.
 *
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
 */

package io.github.gradlenexus.publishplugin

import org.junit.jupiter.api.Test

class MavenNexusPublishPluginTests : BaseNexusPublishPluginTests() {
    init {
        publishPluginId = "maven-publish"
        publishPluginContent = """ mavenJava(MavenPublication) {
                                        from(components.java)
                                    }"""
        publishGoalPrefix = "publishMavenJava"
        publicationTypeName = "MAVEN"
        snapshotArtifactList = listOf(
            "sample-0.0.1-.*.pom",
            "sample-0.0.1-.*.jar"
        )
        artifactList = listOf(
            "sample-0.0.1.pom",
            "sample-0.0.1.jar"
        )
        pluginArtifactList = listOf(
            "/org/example/gradle-plugin/0.0.1/gradle-plugin-0.0.1.pom",
            "/org/example/foo/org.example.foo.gradle.plugin/0.0.1/org.example.foo.gradle.plugin-0.0.1.pom"
        )
    }

    @Test
    fun `setting publication type to null will use maven`() {
        projectDir.resolve("settings.gradle").write(
            """
            rootProject.name = 'sample'
        """
        )

        projectDir.resolve("build.gradle").write(
            """
            plugins {
                id('java-library')
                id('$publishPluginId')
                id('io.github.gradle-nexus.publish-plugin')
            }
            group = 'org.example'
            version = '0.0.1-SNAPSHOT'
            publishing {
                publications {
                     $publishPluginContent
                }
            }
            nexusPublishing {
                repositories {
                    myNexus {
                        publicationType = null
                        nexusUrl = uri('${server.baseUrl()}/shouldNotBeUsed')
                        snapshotRepositoryUrl = uri('${server.baseUrl()}/snapshots')
                        allowInsecureProtocol = true
                        username = 'username'
                        password = 'password'
                    }
                }
            }
            """
        )

        expectArtifactUploads("/snapshots")

        val result = run("publishToMyNexus")

        assertSkipped(result, ":initializeMyNexusStagingRepository")
        snapshotArtifactList.forEach { assertUploaded("/snapshots/org/example/sample/0.0.1-SNAPSHOT/$it") }
    }
}
