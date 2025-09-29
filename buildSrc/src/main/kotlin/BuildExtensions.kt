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

@file:Suppress("UnusedReceiverParameter", "unused", "TopLevelPropertyNaming", "ObjectPropertyName")

import io.spine.gradle.repo.standardToSpineSdk
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.ScriptHandlerScope
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Provides shortcuts to reference our dependency objects.
 *
 * Dependency objects cannot be used under `plugins` section because `io` is a value
 * declared in auto-generated `org.gradle.kotlin.dsl.PluginAccessors.kt` file.
 * It conflicts with our own declarations.
 *
 * In such cases, a shortcut to apply a plugin can be created:
 *
 * ```
 * val PluginDependenciesSpec.`gradle-doctor`: PluginDependencySpec
 *     get() = id(GradleDoctor.pluginId).version(GradleDoctor.version)
 * ```
 *
 * But for some plugins, it is impossible to apply them directly to a project.
 * For example, when a plugin is not published to Gradle Portal, it can only be
 * applied with the buildscript's classpath. Thus, it is necessary to leave some freedom
 * upon how to apply them. In such cases, just a shortcut to a dependency object
 * can be declared without applying the plugin in-place.
 */
private const val ABOUT_DEPENDENCY_EXTENSIONS = ""

/**
 * Applies [standard][io.spine.gradle.repo.standardToSpineSdk] repositories to this `buildscript`.
 */
fun ScriptHandlerScope.standardSpineSdkRepositories() {
    repositories.standardToSpineSdk()
}

/**
 * Sets the remote debug option for this [JavaExec] task.
 *
 * The port number is [5566][BuildSettings.REMOTE_DEBUG_PORT].
 *
 * @param enabled If `true` the task will be suspended.
 */
fun JavaExec.remoteDebug(enabled: Boolean = true) {
    debugOptions {
        this@debugOptions.enabled.set(enabled)
        port.set(BuildSettings.REMOTE_DEBUG_PORT)
        server.set(true)
        suspend.set(true)
    }
}

/**
 * Sets the remote debug option for the task of [JavaExec] type with the given name.
 *
 * The port number is [5566][BuildSettings.REMOTE_DEBUG_PORT].
 *
 * @param enabled If `true` the task will be suspended.
 * @throws IllegalStateException if the task with the given name is not found, or,
 *  if the taks is not of [JavaExec] type.
 */
fun Project.setRemoteDebug(taskName: String, enabled: Boolean = true) {
    val task = tasks.findByName(taskName)
    check(task != null) {
        "Could not find a task named `$taskName` in the project `$name`."
    }
    check(task is JavaExec) {
        "The task `$taskName` is not of type `JavaExec`."
    }
    task.remoteDebug(enabled)
}

/**
 * Sets remote debug options for the `launchSpineCompiler` task.
 *
 * @param enabled if `true` the task will be suspended.
 *
 * @see remoteDebug
 */
fun Project.spineCompilerRemoteDebug(enabled: Boolean = true) =
    setRemoteDebug("launchSpineCompiler", enabled)

/**
 * Sets remote debug options for the `launchTestSpineCompiler` task.
 *
 * @param enabled if `true` the task will be suspended.
 *
 * @see remoteDebug
 */
fun Project.testSpineCompilerRemoteDebug(enabled: Boolean = true) =
    setRemoteDebug("launchTestSpineCompiler", enabled)

/**
 * Sets remote debug options for the `launchTestFixturesSpineCompiler` task.
 *
 * @param enabled if `true` the task will be suspended.
 *
 * @see remoteDebug
 */
fun Project.testFixturesSpineCompilerRemoteDebug(enabled: Boolean = true) =
    setRemoteDebug("launchTestFixturesSpineCompiler", enabled)

/**
 * Parts of names of configurations to be excluded by
 * `artifactMeta/excludeConfigurations/containing` in the modules
 * where `io.spine.atifact-meta` plugin is applied.
 */
val buildToolConfigurations: Array<String> = arrayOf(
    "detekt",
    "jacoco",
    "pmd",
    "checkstyle",
    "checkerframework",
    "ksp",
    "dokka",
)
