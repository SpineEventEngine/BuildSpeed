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

import io.spine.internal.dependency.Spine
import io.spine.internal.gradle.base.build
import io.spine.internal.gradle.standardToSpineSdk
import java.lang.System.lineSeparator
import java.time.Instant

plugins {
    java
    id("com.google.protobuf")
    idea
    id("com.osacky.doctor") version "0.8.1"
}

buildscript {
    standardSpineSdkRepositories()

    dependencies {
        classpath(variantOf(spine.mcJava) { classifier("all") })
    }

    configurations.all {
        resolutionStrategy.force(
            spine.protoData.plugin,
            spine.protoData.compiler,
            spine.protoData.codegenJava,
            spine.validation.codegenJava,
        )
    }
}

repositories.standardToSpineSdk()

apply(plugin = Spine.McJava.pluginId)

dependencies {
    implementation(spine.server)
}

configurations.all {
    resolutionStrategy.force(
        spine.protoData.compiler,
        spine.protoData.codegenJava,
        spine.validation.codegenJava,
        spine.validation.runtimeJava,
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

var startTime: Long? = null

afterEvaluate {
    startTime = System.currentTimeMillis()
}

@Suppress("PropertyName")
val LOG_FILE_MAX_LINES = 500

val recordExecTime by tasks.registering {
    outputs.upToDateWhen { false }

    doLast {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime!!
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        val readableDuration = "$minutes:$seconds"

        System.out.println("Task execution took $readableDuration.")

        val userName = System.getProperty("user.name")
        val timestamp = Instant.now().toString()
        val versions = "core:${spine.versions.core.get()};" +
                "ProtoData:${spine.versions.protoData.get()};" +
                "Validation:${spine.versions.validation.get()};" +
                "mc-java:${spine.versions.mcJava.get()}"
        val logRecord = "$readableDuration :: $userName :: $timestamp :: $versions"

        val logFile = file("$projectDir/journal.log")

        if (!logFile.exists()) {
            logFile.createNewFile()
        }

        var lines = logFile.readLines().toMutableList()
        if (lines.size > LOG_FILE_MAX_LINES) {
            lines = lines.subList(lines.size - LOG_FILE_MAX_LINES, lines.size)
        }
        val updatedLines = mutableListOf(logRecord)
        updatedLines.addAll(lines)
        logFile.writeText(updatedLines.joinToString(lineSeparator()))
    }
}

tasks.build {
    finalizedBy(recordExecTime)
}
