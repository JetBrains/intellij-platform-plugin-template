<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Platform Plugin Template Changelog

## [Unreleased]

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
