<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Platform Plugin Template Changelog

## [Unreleased]
### Changed
- Simplify and optimize GitHub Actions
- Gradle Wrapper upgrade to v6.6

## [0.3.1]
### Added
- Better handling of the Gradle plugin description extraction from the README file
- GitHub Actions - cache Gradle Wrapper

### Changed
- Gradle - remove kotlin("stdlib-jdk8") dependency to decrease the plugin artifact size
- Dependencies - bump ktlint to 9.3.0
- GitHub Actions - make *Update Changelog* job dependent on the *Publish Plugin*

### Fixed
- Resolve ktlint reports

## [0.3.1]
### Changed
- GitHub Actions - run plugin verifier against 2019.3 2020.1 2020.2

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
- Update platformVersion to 2020.1.2

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
