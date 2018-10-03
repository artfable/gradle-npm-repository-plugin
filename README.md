# Gradle NPM Repository Plugin
(version: 0.0.3)

## Overview
The plugin that was written on [kotlin](https://kotlinlang.org) for loading dependencies from [npm](https://www.npmjs.com/) repository. It loads dependencies **without** installing or loading nodeJS! 

## Install
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.github.artfable.gradle:gradle-npm-repository-plugin:0.0.1"
    }
}

apply plugin: 'artfable.npm'
```

It'll add a task `npmLoad`

## Usage
Parameters to configure:
+ dependencies - map of dependencies and their versions (versions can be specified according [semver](https://docs.npmjs.com/misc/semver))
+ packageJSONFile - package.json file (incompatible with `dependencies`)
+ output - path to output directory, where unpacked dependencies will be saved
+ excludes - names of decedent dependencies that should be ignored (for example: you put dependencies A of v1 (version 1) and B of version >= 1 
and dependency A of v1 depends on B of v1 and latest version of B in a repository is 2. 
If B is in excluded, its version will be resolved as 2, otherwise 1)

```groovy
npm {
//    packageJSONFile = "$projectDir/src/main/resources/package.json"
    output = "$buildDir/resources/main/public/libs/"
    excludes = ['underscore']
    dependencies = [
            'backbone': '1.3.3',
            'lodash': '^4.17',
            'js-md5': '0.7.3'
    ]
}
```

