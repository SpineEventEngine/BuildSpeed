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

import io.spine.dependency.local.Spine
import io.spine.dependency.local.McJava
import io.spine.dependency.local.ProtoData
import io.spine.dependency.local.CoreJava
import io.spine.dependency.local.Validation
import io.spine.gradle.UpdateJournal
import io.spine.gradle.base.build
import io.spine.gradle.standardToSpineSdk

plugins {
    java
    id("com.google.protobuf")
    idea
    id("com.osacky.doctor") version "0.8.1"
}

buildscript {
    standardSpineSdkRepositories()

    val protoData = io.spine.dependency.local.ProtoData
    val validation = io.spine.dependency.local.Validation
    val mcJava = io.spine.dependency.local.McJava
    dependencies {
        classpath(mcJava.pluginLib(mcJava.version))
    }

    configurations.all {
        resolutionStrategy.force(
            protoData.pluginLib,
            protoData.backend,
            protoData.java,
            validation.java,
            validation.javaBundle,
        )
    }
}

repositories.standardToSpineSdk()

apply(plugin = McJava.pluginId)

dependencies {
    implementation(CoreJava.server)
}

configurations.all {
    resolutionStrategy.force(
        ProtoData.backend,
        ProtoData.java,
        Validation.java,
        Validation.runtime,
    )
}

idea {
    module {
        generatedSourceDirs = listOf(
            "$projectDir/generated/main/java",
            "$projectDir/generated/main/kotlin"
        ).map(::file).toSet()
    }
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
    startTime = startTimeMillis
    versions.set(
        mapOf(
            "core" to CoreJava.version,
            "ProtoData" to ProtoData.version,
            "Validation" to Validation.version,
            "mc-java" to McJava.version
        )
    )
}
tasks.build {
    finalizedBy(recordExecTime)
}
