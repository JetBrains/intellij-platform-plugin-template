<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Platform Plugin Template Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security
## [0.8.2]
### Changed
- Use `-bin` distribution of the Gradle Wrapper
- Upgrade Gradle Wrapper to `6.8.2`
- Update `pluginVerifierIdeVersions` in `gradle.properties` files
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.4.30`
- Dependencies - upgrade `org.jetbrains.changelog` to `1.1.1`
- Configure the `changelog` Gradle plugin

## [0.8.1]
### Added
- README: Dependencies management section

### Changed
- Upgrade Gradle Wrapper to `6.8`
- Dependencies - upgrade `org.jetbrains.changelog` to `1.0.0`

### Fixed
- Template Cleanup: Escape GitHub username to avoid incorrect characters in class package name
- Template Cleanup: Run `ktlintFormat` task to fix imports order
- GitHub Actions: Use the correct property in the "Upload artifact" step

## [0.8.0]
### Added
- Dependabot integration
- Show `idea.log` logs of the run IDE in the Run console
- README: FAQ section

### Changed
- `build.gradle.kts`: simpler syntax for configuring `KotlinCompile`
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.4.21`
- Dependencies - upgrade `detekt-formatting` to `1.15.0`
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.15.0`
- README: Clarify the Java usage in the project
- `pluginVerifierIdeVersions` - upgrade to `2020.1.4, 2020.2.3, 2020.3.1`

### Fixed
- Return `Supplier<@Nls String>` instead of `String` in `MyBundle.messagePointer`

## [0.7.1]
### Changed
- Upgrade Gradle Wrapper to `6.7.1`
- Dependencies - upgrade `org.jetbrains.intellij` to `0.6.5`
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.4.20`
- Update the base platform version to 2020.1
- Change since/until build to `201-203.*`

## [0.7.0]
### Added
- Predefined Run/Debug Configurations
- Project icon for development purposes

### Changed
- Dependencies - upgrade `org.jetbrains.intellij` to `0.6.3`

## [0.6.1]
### Added
- GitHub Actions - use hash based on `pluginVerifierIdeVersions` in `Setup Plugin Verifier IDEs Cache` step

### Changed
- Use [Kotlin extension function](https://plugins.jetbrains.com/docs/intellij/plugin-services.html#retrieving-a-service) to retrieve the `MyProjectService` in the `MyProjectManagerListener`
- Dependencies - upgrade `org.jetbrains.intellij` to `0.6.2`
- Update `pluginVerifierIdeVersions` in the `gradle.properties` files

## [0.6.0]
### Added
- Integration with [IntelliJ Plugin Verifier](https://github.com/JetBrains/intellij-plugin-verifier) through the [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin#plugin-verifier-dsl) `runPluginVerifier` task
- Cache downloaded IDEs used by Plugin Verifier for the verification

### Changed
- Switch Gradle Wrapper to `-all` to improve the IntelliSense
- Update detekt config to be in line with IJ settings
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.14.2`
- Dependencies - upgrade `org.jetbrains.intellij` to `0.6.1`
- GitHub Actions - `gradleValidation` update to `gradle/wrapper-validation-action@v1.0.3`
- GitHub Actions - `releaseDraft` update to `actions/download-artifact@v2`

### Removed
- Remove Third-party IntelliJ Plugin Verifier GitHub Action

## [0.5.1]
### Added
- Missing properties in the `gradle.properties` template file

### Changed
- Upgrade Gradle Wrapper to `6.7`
- Dependencies - upgrade `org.jetbrains.changelog` to `0.6.2`

## [0.5.0]
### Added
- Introduced `platformPlugins` property in `gradle.properties` for configuring dependencies to bundled/external plugins

### Changed
- Disable "Release Draft" job for pull requests in the "Build" GitHub Actions Workflow
- Dependencies - upgrade `org.jetbrains.intellij` to `0.5.0`
- Dependencies - upgrade `org.jetbrains.changelog` to `0.6.1`
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.14.1`
- Dependencies - upgrade `org.jlleitschuh.gradle.ktlint` to `9.4.1`
- Remove LICENSE file during the Template Cleanup workflow

## [0.4.0]
### Added
- Fix default to opt-out of bundling Kotlin standard library in plugin distribution

### Changed
- GitHub Actions: allow releasing plugin even for the base project
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.4.10`
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.13.1`

### Fixed
- `pluginName` variable name collision with `intellij` closure getter in Gradle configuration #29

## [0.3.2]
### Changed
- Simplify and optimize GitHub Actions
- Gradle Wrapper upgrade to `6.6.1`
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.4.0`
- Dependencies - upgrade `org.jetbrains.intellij` to `0.4.22`
- Dependencies - upgrade `org.jetbrains.changelog` to `0.5.0`
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.12.0`
- Dependencies - upgrade `org.jlleitschuh.gradle.ktlint` to `9.4.0`
- Rename `master` branch to `main`

### Fixed
- GitHub Actions - cache Gradle dependencies and wrapper separately

## [0.3.1]
### Added
- Better handling of the Gradle plugin description extraction from the README file
- GitHub Actions - cache Gradle Wrapper

### Changed
- Gradle - remove kotlin("stdlib-jdk8") dependency to decrease the plugin artifact size
- Dependencies - bump ktlint to `9.3.0`
- GitHub Actions - make *Update Changelog* job dependent on the *Publish Plugin*

### Fixed
- Resolve ktlint reports

## [0.3.1]
### Changed
- GitHub Actions - run plugin verifier against `2019.3` `2020.1` `2020.2`

### Fixed
- GitHub Actions - Plugin Verifier broken for artifacts with whitespaces in name

## [0.3.0]
### Added
- Set publish channel depending on the plugin version, i.e. `1.0.0-beta` -> `beta` channel

### Changed
- Update `org.jetbrains.changelog` dependency to `v0.3.3`
- Update Gradle Wrapper to `v6.5.1`
- Run GitHub Actions Release workflow on `prereleased` event
- GitHub Actions - Release - separate changelog related job from the release

### Fixed
- Remove vendor website from `plugin.xml`
- Update Template Cleanup workflow test to avoid running it on forks

## [0.2.0]
### Added
- JetBrains Plugin badges and TODO list for the end users
- `ktlint` integration

### Changed
- `pluginUntilBuild` set to the correct format: `201.*`
- Bump detekt dependency to `1.10.0`

### Fixed
- GitHub Actions - Template Cleanup - fixed adding files to git
- Update Template plugin name on cleanup
- Set `buildUponDefaultConfig = true` in detekt configuration

## [0.1.0]
### Added
- `settings.gradle.kts` for the [performance purposes](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#always_define_a_settings_file)
- `#REMOVE-ON-CLEANUP#` token to mark content to be removed with **Template Cleanup** workflow

### Changed
- README proofreading
- GitHub Actions - Update IDE versions for the Plugin Verifier
- Update platformVersion to `2020.1.2`

## [0.0.2]
### Added
- [Gradle Changelog Plugin](https://github.com/JetBrains/gradle-changelog-plugin) integration

### Changed
- Bump Detekt version
- Change pluginSinceBuild to 193

## [0.0.1]
### Added
- Initial project scaffold
- GitHub Actions to automate testing and deployment
- Kotlin support
