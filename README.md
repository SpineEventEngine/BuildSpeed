# BuildPerformance

This repository contains a set of Protobuf model definitions.

Unlike many other [Spine examples](https://github.com/spine-examples), this repo does not contain
Java or any other implementations. Instead, it focuses on the model definitions, providing
a sizable amount of various Proto definitions.

Using the definitions in this repo, we can test performance of our build tools, comparing it to
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

To launch the build performance tests with Gradle, follow these steps.

1. Add the repo as a Git submodule to your project.
2. Create a Gradle task which would call the `substitute-settings.py` script, setting the versions
   to the versions from the local environment:
```kotlin
val prepareBuildPerformanceSettings by tasks.registering(Exec::class) {
    environment(
        "MC_JAVA_VERSION" to Spine.McJava.version,
        "CORE_VERSION" to Spine.ArtifactVersion.core,
        "PROTO_DATA_VERSION" to ProtoData.version,
        "VALIDATION_VERSION" to Validation.version
    )
    workingDir = File(rootDir, "BuildPerformance")
    commandLine("./substitute-settings.py")
}
```
   This script creates/overrides the `settings.gradle.kts` file with the relevant
   Spine dependency versions.
3. Create a task calls the `build` task on this project:
```kotlin
tasks.register<RunBuild>("checkPerformance") {
    directory = "$rootDir/BuildPerformance"

    dependsOn(prepareBuildPerformanceSettings, localPublish)
    shouldRunAfter(check)
}
```
4. Launch the `checkPerformance` task. It's not recommended to include this task into the build
   by default due to the long execution time. Instead, run it manually when needed and/or launch
   it on CI.

## Extra Gradle configuration

If the performance tests require additional configuration, e.g. configuring the tested plugins,
add a file called `buildperformance.gradle.kts` to the root of the project. This script plugin
will be applied to the `BuildPerformance` project.
