/*
* Copyright 2023, TeamDev. All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
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

@file:Suppress("UnstableApiUsage")

/**
 * This version of `settings.gradle.kts` is used when `BuildPerformance` is build separately.
 *
 * Change the versions of Spine dependencies below to test the build performance locally.
 *
 * See `settings.gradle.kts.template` for the version of the file used in GitHub Actions.
 */

dependencyResolutionManagement {
    versionCatalogs {
        create("spine") {
            val mcJava = version("mcJava", "2.0.0-SNAPSHOT.151")
            val core = version("core", "2.0.0-SNAPSHOT.141")
            val protoData = version("protoData", "0.8.5")
            val validation = version("validation", "2.0.0-SNAPSHOT.81")

            val baseGroup = "io.spine"
            val toolsGroup = "$baseGroup.tools"
            val protoDataGroup = "$baseGroup.protodata"
            val validationGroup = "$baseGroup.validation"

            library(
                "mcJava",
                toolsGroup, "spine-mc-java-plugins"
            ).versionRef(mcJava)

            library(
                "server",
                baseGroup, "spine-server"
            ).versionRef(core)

            library(
                "protoData.plugin",
                baseGroup, "protodata"
            ).versionRef(protoData)

            library(
                "protoData.compiler",
                protoDataGroup, "protodata-compiler"
            ).versionRef(protoData)

            library(
                "protoData.codegenJava",
                protoDataGroup, "protodata-codegen-java"
            ).versionRef(protoData)

            library(
                "validation.codegenJava",
                validationGroup, "spine-validation-java-bundle"
            ).versionRef(validation)

            library(
                "validation.runtimeJava",
                validationGroup, "spine-validation-java-runtime"
            ).versionRef(validation)
        }
    }
}
