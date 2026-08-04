/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    groovy
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

/**
 * The version of Google Artifact Registry used by `buildSrc`.
 *
 * The version `2.1.5` is the latest before `2.2.0`, which introduces breaking changes.
 *
 * @see <a href="https://mvnrepository.com/artifact/com.google.cloud.artifactregistry/artifactregistry-auth-common">
 *     Google Artifact Registry at Maven</a>
 */
val googleAuthToolVersion = "2.1.5"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    dependOnAuthCommon()
}

dependOnBuildSrcJar()

/**
 * Adds a dependency on a `buildSrc.jar`, iff:
 *  1) the `src` folder is missing, and
 *  2) `buildSrc.jar` is present in `buildSrc/` folder instead.
 *
 * This approach is used in the scope of integration testing.
 */
fun Project.dependOnBuildSrcJar() {
    val srcFolder = this.rootDir.resolve("src")
    val buildSrcJar = rootDir.resolve("buildSrc.jar")
    if (!srcFolder.exists() && buildSrcJar.exists()) {
        logger.info("Adding the pre-compiled 'buildSrc.jar' to 'implementation' dependencies.")
        dependencies {
            implementation(files("buildSrc.jar"))
        }
    }
}

/**
 * Includes the `implementation` dependency on `artifactregistry-auth-common`,
 * with the version defined in [googleAuthToolVersion].
 *
 * `artifactregistry-auth-common` has transitive dependency on Gson and Apache `commons-codec`.
 * Gson from version `2.8.6` until `2.8.9` is vulnerable to Deserialization of Untrusted Data
 * (https://devhub.checkmarx.com/cve-details/CVE-2022-25647/).
 *
 *  Apache `commons-codec` before 1.13 is vulnerable to information exposure
 * (https://devhub.checkmarx.com/cve-details/Cxeb68d52e-5509/).
 *
 * We use Gson `2.10.1` and we force it in `forceProductionDependencies()`.
 * We use `commons-code` with version `1.16.0`, forcing it in `forceProductionDependencies()`.
 *
 * So, we should be safe with the current version `artifactregistry-auth-common` until
 * we migrate to a later version.
 */
fun DependencyHandlerScope.dependOnAuthCommon() {
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    implementation(
        "com.google.cloud.artifactregistry:artifactregistry-auth-common:$googleAuthToolVersion"
    ) {
        exclude(group = "com.google.guava")
    }
}
