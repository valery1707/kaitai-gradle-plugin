[![Maven Central](https://maven-badges.herokuapp.com/maven-central/name.valery1707.kaitai/kaitai-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/name.valery1707.kaitai/kaitai-gradle-plugin)
[![License](https://img.shields.io/github/license/valery1707/kaitai-gradle-plugin.svg)](https://opensource.org/licenses/MIT)

[![Build Status](https://travis-ci.org/valery1707/kaitai-gradle-plugin.svg?branch=master)](https://travis-ci.org/valery1707/kaitai-gradle-plugin)
[![CircleCI](https://circleci.com/gh/valery1707/kaitai-gradle-plugin/tree/master.svg?style=svg)](https://circleci.com/gh/valery1707/kaitai-gradle-plugin/tree/master)

Gradle plugin for http://kaitai.io/

# Usage

## Minimum configuration

* Add plugin dependency
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("name.valery1707.kaitai:kaitai-gradle-plugin:0.1.0")
    }
}
```
* Apply plugin
```groovy
apply plugin: 'name.valery1707.kaitai'
```
* Add Kaitai compile dependencies
```groovy
repositories {
    jcenter()
}
dependencies {
    implementation("io.kaitai:kaitai-struct-runtime:0.8")
}
```
* Configure plugin (all params are optional)
```groovy
kaitai {
    packageName = 'preferred.package.name'
}
```

## Plugin parameters

| Name            | Type         | Since | Description                                                                                                             |
|-----------------|--------------|-------|-------------------------------------------------------------------------------------------------------------------------|
| skip            | boolean      | 0.1.0 | Skip plugin execution (don't read/validate any files, don't generate any java types).<br><br>**Default**: `false`       |
| url             | java.net.URL | 0.1.0 | Direct link onto [KaiTai universal zip archive](http://kaitai.io/#download).<br><br>**Default**: Detected from version  |
| version         | String       | 0.1.0 | Version of [KaiTai](http://kaitai.io/#download) library.<br><br>**Default**: `0.8`                                      |
| cacheDir        | java.io.File | 0.1.0 | Cache directory for download KaiTai library.<br><br>**Default**: `build/tmp/kaitai-cache`                               |
| sourceDirectory | java.io.File | 0.1.0 | Source directory with [Kaitai Struct language](http://formats.kaitai.io/) files.<br><br>**Default**: src/main/resources/kaitai |
| includes        | String[]     | 0.1.0 | Include wildcard pattern list.<br><br>**Default**: ["*.ksy"]                                                            |
| excludes        | String[]     | 0.1.0 | Exclude wildcard pattern list.<br><br>**Default**: []                                                                   |
| output          | java.io.File | 0.1.0 | Target directory for generated Java source files.<br><br>**Default**: `build/generated/kaitai`                          |
| packageName     | String       | 0.1.0 | Target package for generated Java source files.<br><br>**Default**: Trying to get project's group or `kaitai` otherwise |

# Developments
For debug on integration tests you must previously call some gradle tasks: `jar assemble pluginUnderTestMetadata`

Deploy new version manually:
1. Update `version` inside `build.gradle.kts` and `README.md` into releasing value and commit with `Prepare release 0.2.0`
1. Prepare `gradle.properties` from `gradle-template.properties`
1. Run `./gradlew clean build uploadArchives`
1. Open [Staging Repository](https://oss.sonatype.org/#stagingRepositories)
1. Search for `namevalery1707` and select founded
1. Check content on tab `Content`
1. Press `Close` with comment `Release version 0.2.0`
1. Wait for operation:
1. On failure: 
    * Check reason on tab `Activity`
    * Press `Drop` with some comment
1. Press `Release` with comment `Release version 0.2.0`
1. Update `version` into snapshot value and commit with `Prepare for next development iteration`
