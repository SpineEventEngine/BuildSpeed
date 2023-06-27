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

package io.spine.internal.gradle

import java.io.File
import java.time.Instant
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * This task tracks how long does the execution of the Gradle tasks take and logs the measurement
 * into a file.
 *
 * The tasks uses `journal.log` file in the project root directory. Along with the execution time,
 * we log the info about the run, such as the versions of Spine dependencies.
 *
 * In the journal, one line corresponds to one run.
 */
abstract class UpdateJournal : DefaultTask() {

    @get:Internal
    var startTime: Long? = null

    @get:Internal
    abstract val versions: MapProperty<String, Provider<String>>

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun updateJournal() {
        val duration = calculateDuration()

        logger.lifecycle("Task execution took $duration.")

        val userName = System.getProperty("user.name")
        val timestamp = Instant.now().toString()
        val printedVersions = printVersions()
        val versions = printedVersions
        val logRecord = "$duration :: $userName :: $timestamp :: $versions"

        val logFile = prepareFile()

        var lines = logFile.readLines().toMutableList()
        if (lines.size > LOG_FILE_MAX_LINES) {
            lines = lines.subList(0, LOG_FILE_MAX_LINES)
        }
        val updatedLines = mutableListOf(logRecord)
        updatedLines.addAll(lines)
        logFile.writeText(updatedLines.joinToString(System.lineSeparator()))
    }

    private fun prepareFile(): File {
        val logFile = project.file("${project.projectDir}/journal.log")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        return logFile
    }

    private fun printVersions() = versions
        .get()
        .entries
        .joinToString(";") { (k, v) -> "$k:${v.get()}" }

    private fun calculateDuration(): String {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime!!
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:$seconds"
    }
}

/**
 * The maximum number of history lines kept in the journal.
 */
private const val LOG_FILE_MAX_LINES = 500
