# Gradle NPM Repository Plugin
Moved to https://gitlab.com/artfable-public/gradle-plugins/gradle-npm-repository-plugin

[ ![artifactory](https://img.shields.io/badge/Artifactory-v0.0.6-green) ](https://artfable.jfrog.io/ui/packages/gav:%2F%2Fcom.artfable.gradle:gradle-npm-repository-plugin)

## Overview
The plugin that was written on [kotlin](https://kotlinlang.org) for loading dependencies from [npm](https://www.npmjs.com/) repository. It loads dependencies **without** installing or loading nodeJS! 

## Install
```kotlin
buildscript {
    repositories {
        maven(url = "https://artfable.jfrog.io/artifactory/default-maven-local")
    }
    dependencies {
        classpath("com.artfable.gradle:gradle-npm-repository-plugin:0.0.6")
    }
}

apply(plugin = "artfable.npm")
```

It'll add a task `npmLoad`

For use in `plugins {}` see [Gradle resolution strategy](https://docs.gradle.org/current/userguide/custom_plugins.html#note_for_plugins_published_without_java_gradle_plugin)

## Usage
Parameters to configure:
+ dependencies - map of dependencies and their versions (versions can be specified according [semver](https://docs.npmjs.com/misc/semver))
+ packageJSONFile - package.json file (incompatible with `dependencies`)
+ output - path to output directory, where unpacked dependencies will be saved
+ excludes - names of descendant dependencies that should be ignored (for example: you put dependencies A of v1 (version 1) and B of version >= 1 
and dependency A of v1 depends on B of v1 and latest version of B in a repository is 2. 
If B is in excluded, its version will be resolved as 2, otherwise 1)

```kotlin
npm { // configure<GradleNpmRepositoryExtension> { // if not configured through plugins {}
    output = "$buildDir/resources/main/public/libs/"
    excludes = setOf("underscore")
//    packageJSONFile = "$projectDir/src/main/resources/package.json" // instead of dependencies
    dependencies = mapOf(
            "backbone" to "1.3.3",
            "lodash" to "^4.17",
            "js-md5" to "0.7.3"
    )
}
```

