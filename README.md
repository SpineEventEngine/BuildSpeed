# BuildSpeed

This repository contains a set of Protobuf model definitions.

Unlike many other [Spine examples](https://github.com/spine-examples), this repo does not contain
Java or any other implementations. Instead, it focuses on the model definitions, providing
a sizable amount of various Proto definitions.

Using the definitions in this repo, we can test speed of our build tools, comparing it to
the past performance.

The repo contains 1152 message declarations and 32 enum declarations in 192 `.proto` files.
This includes command, event, rejection, entity, ID, and value object types.
The declarations are split into several identical packages which only differ in names.
This allows us to have more data for testing build tool performance.

## Manual usage

To use this repo locally for one-time tests, follow these steps.

1. Add the repo as a Git submodule to your project.
2. Copy the `settings.gradle.kts.template` file as `settings.gradle.kts`.
   Replace all the placeholder versions with the required versions. Or, alternatively,
   add a `dependencySubstitution` block and define which local projects should be used for which
   dependencies.
3. Build the project with the `build` task.

## Using with Gradle

To launch the build speed tests with Gradle, follow these steps.

1. Add the repo as a Git submodule to your project.
2. Create a Gradle task which would call the `substitute-settings.py` script, setting the versions
   to the versions from the local environment:
```kotlin
val prepareBuildSpeedSettings by tasks.registering(Exec::class) {
    environment(
        "MC_JAVA_VERSION" to Spine.McJava.version,
        "CORE_VERSION" to Spine.ArtifactVersion.core,
        "PROTO_DATA_VERSION" to ProtoData.version,
        "VALIDATION_VERSION" to Validation.version
    )
    workingDir = File(rootDir, "BuildSpeed")
    commandLine("./substitute-settings.py")
}
```
   This script creates/overrides the `settings.gradle.kts` file with the relevant
   Spine dependency versions.
3. Create a task calls the `build` task on this project:
```kotlin
tasks.register<RunBuild>("checkSpeed") {
    directory = "$rootDir/BuildSpeed"

    dependsOn(prepareBuildSpeedSettings, localPublish)
    shouldRunAfter(check)
}
```
4. Launch the `checkSpeed` task. It's not recommended to include this task into the build
   by default due to the long execution time. Instead, run it manually when needed and/or launch
   it on CI.

## Extra Gradle configuration

If the speed tests require additional configuration, e.g. configuring the tested plugins,
add a file called `build-speed.gradle.kts` to the root of the project. This script plugin
will be applied to the `BuildSpeed` project.

## The journal file

This repository stores the `journal.log` file and updates it upon each `build`. This file contains
info about the past builds, the speed of the task execution, the versions of Spine dependencies,
the username (for specifying the machine which ran the build).

The journal file should be kept in the Git repository. If this repo is included as a submodule into
another repo and needs updating, the journal file should be preserved in its local form, so that
less relevant build history doesn't overwrite the local build history.
