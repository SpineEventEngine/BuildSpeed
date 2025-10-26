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

import io.spine.gradle.UpdateJournal
import io.spine.gradle.base.build
import io.spine.gradle.repo.standardToSpineSdk
import java.util.function.Supplier

buildscript {
    standardSpineSdkRepositories()
    dependencies {
        classpath(libs.coreJvmCompiler)
    }
}

group = "io.spine.tools.tests"
version = "1.0.0-SNAPSHOT"

plugins {
    java
    kotlin("jvm")
    id("com.google.protobuf")
    id("com.osacky.doctor") version "0.8.1"
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(BuildSettings.jvmTarget)
    }
}

tasks.withType<JavaCompile>().configureEach {
    val javaVer = BuildSettings.javaVersion.toString()
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
}

apply(plugin = "io.spine.core-jvm")

repositories.standardToSpineSdk()

dependencies {
    implementation(libs.coreJvmServer)
}

val customConfigFile = "../build-speed.gradle.kts"

if (file(customConfigFile).exists()) {
    apply(from = customConfigFile)
}

/**
 * Set up the `recordExecTime` task that logs the execution time of the build.
 *
 * In order to record the true time of Gradle's task execution phase, we obtain the current time
 * in the `afterEvaluate` block in this script. This start time is passed to the task.
 * The task calculates the execution time and stores this info in a journal file.
 *
 * `recordExecTime` runs each time `build` is called.
 */

var startTimeMillis: Long? = null

afterEvaluate {
    startTimeMillis = System.currentTimeMillis()
}

val recordExecTime by tasks.registering(UpdateJournal::class) {
    startTime = Supplier { startTimeMillis!! }
    versions.set(
        mapOf(
            "Compiler" to libs.versions.spineCompiler.get(),
            "CoreJvmCompiler" to libs.versions.coreJvmCompiler.get(),
            "CoreJvm" to libs.versions.coreJvm.get(),
            "Validation" to libs.versions.validation.get(),
        )
    )
}
tasks.build {
    finalizedBy(recordExecTime)
}
