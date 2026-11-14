# IntelliJ Platform Plugin Template

[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:github]
[![Twitter Follow](https://img.shields.io/badge/follow-%40JBPlatform-1DA1F2?logo=twitter)](https://x.com/JBPlatform)
[![Build](https://github.com/JetBrains/intellij-platform-plugin-template/workflows/Build/badge.svg)][gh:build]

![IntelliJ Platform Plugin Template][file:intellij-platform-plugin-template-dark]
![IntelliJ Platform Plugin Template][file:intellij-platform-plugin-template-light]

> [!NOTE]
> Click the <kbd>Use this template</kbd> button and clone it in IntelliJ IDEA.

**IntelliJ Platform Plugin Template** is a repository that provides a pure template to make it easier to create a new plugin project (check the [Creating a repository from a template][gh:template] article).

The main goal of this template is to speed up the setup phase of plugin development for both new and experienced developers by preconfiguring the project scaffold and CI, linking to the proper documentation pages, and keeping everything organized.

[gh:template]: https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template

If you're still not quite sure what this is all about, read our introduction: [What is the IntelliJ Platform?][docs:intro]

> [!NOTE]
> Click the <kbd>Watch</kbd> button on the top to be notified about releases containing new features and fixes.

### Table of contents

In this README, we will highlight the following elements of template-project creation:

- [Getting started](#getting-started)
- [Gradle configuration](#gradle-configuration)
- [Plugin template structure](#plugin-template-structure)
- [Plugin configuration file](#plugin-configuration-file)
- [Sample code](#sample-code):
  - startup activity – project-open sample
  - services – project-level service
  - tool window – sample UI entry point
- [Testing](#testing)
  - [Functional tests](#functional-tests)
  - [UI tests](#ui-tests)
- [Predefined Run/Debug configurations](#predefined-rundebug-configurations)
- [Continuous integration](#continuous-integration) based on GitHub Actions
  - [Dependencies management](#dependencies-management) with Dependabot
  - [Changelog maintenance](#changelog-maintenance) with the Gradle Changelog Plugin
  - [Release flow](#release-flow) using GitHub Releases
  - [Plugin signing](#plugin-signing) with Marketplace signing
  - [Publishing the plugin](#publishing-the-plugin) with the IntelliJ Platform Gradle Plugin
- [FAQ](#faq)
- [Useful links](#useful-links)


## Getting started

Before we dive into plugin development and everything related to it, it's worth mentioning the benefits of using GitHub Templates.
By creating a new project using the current template, you start with no history or reference to this repository.
This allows you to create a new repository easily without copying and pasting previous content, cloning repositories, or clearing the history manually.

All you have to do is click the <kbd>Use this template</kbd> button (you must be logged in with your GitHub account).

![Use this template][file:use-this-template.png]

After using the template to create your blank project, the [Template Cleanup][file:template_cleanup.yml] workflow will be triggered to override or remove any template-specific configurations, such as the plugin name, current changelog, etc.
Once this is complete, open the newly created project's _Settings | Actions | General_ page and enable option _Allow GitHub Actions to create and approve pull requests_.

Now the project is ready to be cloned to your local environment and opened with [IntelliJ IDEA][jb:download-ij].

The most convenient way for getting your new project from GitHub is the <kbd>Get from VCS</kbd> action available on the Welcome Screen, where you can filter your GitHub repository by its name.

![Get from Version Control][file:get-from-version-control]

The next step, after opening your project in IntelliJ IDEA, is to set the proper <kbd>SDK</kbd> to Java in version `21` within the [Project Structure settings][docs:project-structure-settings].

![Project Structure — SDK][file:project-structure-sdk.png]

For the last step, review the project metadata in [`gradle.properties`][file:gradle.properties] and [`plugin.xml`][file:plugin.xml], then *optionally* move sources from the generated package to the one that works best for you.
Then you can get to work implementing your ideas.

> [!NOTE]
> To use Java in your plugin, create the `/src/main/java` directory.


## Gradle configuration

The recommended method for plugin development involves using the [Gradle][gradle] setup with the [intellij-platform-gradle-plugin][gh:intellij-platform-gradle-plugin] installed.
The IntelliJ Platform Gradle Plugin makes it possible to run the IDE with your plugin and publish your plugin to JetBrains Marketplace.

> [!NOTE]
> Make sure to always upgrade to the latest version of IntelliJ Platform Gradle Plugin.

A project built using the IntelliJ Platform Plugin Template includes a Gradle configuration already set up.
Feel free to read through the [Using Gradle][docs:using-gradle] articles to understand your build better and learn how to customize it.

The most significant parts of the current configuration are:
- Integration with the [intellij-platform-gradle-plugin][gh:intellij-platform-gradle-plugin] for smoother development.
- Repository configuration moved to [`settings.gradle.kts`][file:settings.gradle.kts] using the IntelliJ Platform repositories extension.
- Configuration written with [Gradle Kotlin DSL][gradle:kotlin-dsl].
- Support for Kotlin and Java implementation.
- Plugin and dependency versions are declared directly in the Gradle build files.
- Integration with the [gradle-changelog-plugin][gh:gradle-changelog-plugin], which automatically patches the change notes based on the `CHANGELOG.md` file.
- [Plugin publishing][docs:publishing] through the `publishPlugin` task and GitHub Actions workflows.

For more details regarding Kotlin integration, please see [Kotlin for Plugin Developers][docs:kotlin] in the IntelliJ Platform Plugin SDK documentation.

The current target is IntelliJ IDEA 2026.2, with minimum platform build `262` and Java 25 required for compilation and testing.
The IntelliJ Platform Gradle Plugin provisions the matching toolchain; Gradle itself can still run on Java 21, as it does in CI.

### Gradle properties

The project-specific configuration file [`gradle.properties`][file:gradle.properties] starts with values expected to vary between repositories created from this template:

| Property name         | Description                                                                                          |
|-----------------------|------------------------------------------------------------------------------------------------------|
| `group`               | Project group and default base package for the sample sources.                                      |
| `version`             | Current plugin version in [SemVer][semver] format.                                                   |
| `pluginRepositoryUrl` | Repository URL used for generating URLs by the [Gradle Changelog Plugin][gh:gradle-changelog-plugin] |

The remaining plugin metadata lives closer to where it is used:
- [`settings.gradle.kts`][file:settings.gradle.kts] declares Gradle plugin versions and repository management.
- [`build.gradle.kts`][file:build.gradle.kts] declares the target IntelliJ Platform version and project dependencies.
- [`plugin.xml`][file:plugin.xml] contains the plugin `id`, `name`, `vendor`, `description`, and extension registrations.

It also configures Gradle build behavior flags, such as:

| Property name                                    | Value   | Description                                                                                    |
|--------------------------------------------------|---------|------------------------------------------------------------------------------------------------|
| `kotlin.stdlib.default.dependency`               | `false` | Opt-out flag for bundling [Kotlin standard library][docs:kotlin-stdlib]                        |
| `org.gradle.configuration-cache`                 | `true`  | Enable [Gradle Configuration Cache][gradle:configuration-cache]                                |
| `org.gradle.caching`                             | `true`  | Enable [Gradle Build Cache][gradle:build-cache]                                                |

### Environment variables

Some values used during signing and publishing should not be stored in project files.

Provide them through local *Run/Debug Configurations* or on CI, for example in GitHub under `Settings > Secrets and variables > Actions`.

The current template uses the following variables for the [plugin signing](#plugin-signing) and [publishing](#publishing-the-plugin) flow:

| Environment variable name | Description                                                                                                  |
|---------------------------|--------------------------------------------------------------------------------------------------------------|
| `PRIVATE_KEY`             | Certificate private key, should contain: `-----BEGIN RSA PRIVATE KEY----- ... -----END RSA PRIVATE KEY-----` |
| `PRIVATE_KEY_PASSWORD`    | Password used for encrypting the certificate file.                                                           |
| `CERTIFICATE_CHAIN`       | Certificate chain, should contain: `-----BEGIN CERTIFICATE----- ... -----END CERTIFICATE----`                |
| `PUBLISH_TOKEN`           | Publishing token generated in your JetBrains Marketplace profile dashboard.                                  |

For more details on how to generate proper values, check the relevant sections mentioned above.

To configure GitHub secret environment variables, go to the `⚙️ Settings > Secrets and variables > Actions` section of your project repository:

![Settings > Secrets][file:settings-secrets.png]

## Plugin template structure

A generated IntelliJ Platform Plugin Template repository contains the following content structure:

```
.
├── .github/                GitHub Actions workflows and Dependabot configuration files
├── .run/                   Predefined Run/Debug Configurations
├── gradle
│   └── wrapper/            Gradle Wrapper
├── src                     Plugin sources
│   ├── main
│   │   ├── kotlin/         Kotlin production sources
│   │   └── resources/      Resources - plugin.xml, icons, messages
│   └── test
│       ├── kotlin/         Kotlin test sources
│       └── testData/       Test data used by tests
├── .gitignore              Git ignoring rules
├── build.gradle.kts        Gradle configuration
├── CHANGELOG.md            Full change history
├── gradle.properties       Gradle configuration properties
├── gradlew                 *nix Gradle Wrapper script
├── gradlew.bat             Windows Gradle Wrapper script
├── LICENSE                 License, MIT by default
├── README.md               README
└── settings.gradle.kts     Gradle project settings and repositories
```

In addition to the configuration files, the most crucial part is the `src` directory, which contains our implementation and the manifest for our plugin – [plugin.xml][file:plugin.xml].

> [!NOTE]
> To use Java in your plugin, create the `/src/main/java` directory.


## Plugin configuration file

The plugin configuration file is a [plugin.xml][file:plugin.xml] file located in the `src/main/resources/META-INF` directory.
It provides general information about the plugin, its dependencies, and its extensions.
Maintain the plugin description directly in this file, using HTML wrapped in CDATA when needed.

> [!NOTE]
> When using this template for a real plugin, replace the placeholder description with content that describes the final plugin.

```xml
<idea-plugin>
  <id>org.jetbrains.plugins.template</id>
  <name>IntelliJ Platform Plugin Template</name>
  <vendor>JetBrains</vendor>
  <description><![CDATA[
    <p>Plugin description in HTML.</p>
  ]]></description>

  <depends>com.intellij.modules.platform</depends>

  <resource-bundle>messages.MyBundle</resource-bundle>

  <extensions defaultExtensionNs="com.intellij">
    <toolWindow factoryClass="..." id="..."/>
    <postStartupActivity implementation="..."/>
  </extensions>
</idea-plugin>
```

You can read more about this file in the [Plugin Configuration File][docs:plugin.xml] section of our documentation.


## Sample code

The prepared template provides as little code as possible because it is impossible for a general scaffold to fulfill all the specific requirements for all types of plugins (language support, build tools, VCS related tools).
Therefore, the template contains only the following files:

```
.
├── startup
│   └── MyProjectActivity.kt                Project startup activity
├── services
│   └── MyProjectService.kt                 Project-level service
├── toolWindow
│   └── MyToolWindowFactory.kt              Tool window factory — creates tool window content
└── MyBundle.kt                             Bundle class providing access to the resources messages
```

These files are located in `src/main/kotlin`.
This location indicates the language being used.
So if you decide to use Java instead (or in addition to Kotlin), these sources should be located in the `src/main/java` directory.

> [!TIP]
> It is possible to use the [IntelliJ Platform Icons](https://jb.gg/new-ui-icons) in your plugin.

To start with the actual implementation, you may check our [IntelliJ Platform SDK DevGuide][docs], which contains an introduction to the essential areas of the plugin development together with dedicated tutorials.

> [!WARNING]
> Remember to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.

For those who value example codes the most, there are also available [IntelliJ SDK Code Samples][gh:code-samples] and [IntelliJ Platform Explorer][jb:ipe] – a search tool for browsing Extension Points inside existing implementations of open-source IntelliJ Platform plugins.

## Testing

[Testing plugins][docs:testing-plugins] is an essential part of the plugin development to make sure that everything works as expected between IDE releases and plugin refactorings.
The IntelliJ Platform Plugin Template project ships with functional test examples and leaves UI testing setup to the plugin author.

### Functional tests

Most of the IntelliJ Platform codebase tests are model-level, run in a headless environment using an actual IDE instance.
The tests usually test a feature as a whole rather than individual functions that comprise its implementation, like in unit tests.

In `src/test/kotlin`, you will find a basic `MyPluginTest` test that utilizes `BasePlatformTestCase` and runs a few checks against the XML files to indicate an example operation of creating files on the fly or reading them from `src/test/testData/rename` test resources.

> [!NOTE]
> Run your tests using predefined *Run Tests* configuration or by invoking the `./gradlew check` Gradle task.

### UI tests

If your plugin provides complex user interfaces, you should consider covering them with tests and the functionality they use.

The template does not wire UI testing into the Gradle build by default anymore.
If you need UI coverage, start with the IntelliJ Platform SDK guides for [Integration Tests][docs:integration-tests] and [Integration Tests: UI Testing][docs:integration-tests-ui], then add your own test source set, Gradle tasks, and CI workflow for the operating systems you support.


## Predefined Run/Debug configurations

Within the default project structure, there is a `.run` directory provided containing predefined *Run/Debug configurations* that expose corresponding Gradle tasks:

![Run/Debug configurations][file:run-debug-configurations.png]

| Configuration name | Description                                                                                                                                                                         |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Run Plugin         | Runs [`:runIde`][gh:intellij-platform-gradle-plugin-runIde] IntelliJ Platform Gradle Plugin task. Use the *Debug* icon for plugin debugging.                                        |
| Run Tests          | Runs [`:check`][gradle:lifecycle-tasks] Gradle task.                                                                                                                                |
| Run Verifications  | Runs [`:verifyPlugin`][gh:intellij-platform-gradle-plugin-verifyPlugin] IntelliJ Platform Gradle Plugin task to check the plugin compatibility against the specified IntelliJ IDEs. |

> [!NOTE]
> You can find the logs from the running task in the `idea.log` tab.
>
> ![Run/Debug configuration logs][file:run-logs.png]


## Continuous integration

Continuous integration depends on [GitHub Actions][gh:actions], a set of workflows that make it possible to automate your testing and release process.
Thanks to such automation, you can delegate the testing and verification phases to the Continuous Integration (CI) and instead focus on development (and writing more tests).

> [!NOTE]
> To ensure the "Create Pull Request" step functions correctly in the "Publish Plugin" job, make sure to enable "Read and write permissions" for actions by navigating to `⚙️ Settings > Actions > General > Workflow permissions`.


In the `.github/workflows` directory, you can find definitions for the following GitHub Actions workflows:

- [Build](.github/workflows/build.yml)
  - Triggered on `push` and `pull_request` events.
  - Builds the plugin with the `buildPlugin` Gradle task and uploads the plugin ZIP as a workflow artifact.
  - Runs the `check` Gradle task in a dedicated test job.
  - Runs the `verifyPlugin` Gradle task in a dedicated verification job.
  - Prepares a draft release of the GitHub Releases page for manual verification.
- [Release](.github/workflows/release.yml)
  - Triggered on `prereleased` and `released` events.
  - Updates `CHANGELOG.md` with the published release notes when a release body is provided.
  - Signs the plugin with a provided certificate before publishing.
  - Publishes the plugin to JetBrains Marketplace using the provided `PUBLISH_TOKEN`.
  - Uploads the built plugin distribution as a GitHub release asset.
  - Creates a pull request with the patched changelog when needed.
- [Template Cleanup](.github/workflows/template-cleanup.yml)
  - Triggered once on the `push` event when a new template-based repository has been created.
  - Overrides the scaffold with files from the `.github/template-cleanup` directory.
  - Overrides JetBrains-specific sentences or package names with ones specific to the target repository.
  - Removes redundant files.

All the workflow files include inline documentation, so it's a good idea to take a look through their sources.

### Dependencies management

This template keeps dependency management explicit and local to the Gradle files that use it:
- [`settings.gradle.kts`][file:settings.gradle.kts] declares Gradle plugin versions and repositories.
- [`build.gradle.kts`][file:build.gradle.kts] declares the target IntelliJ Platform version and project dependencies.
- [`gradle.properties`][file:gradle.properties] stores repository-specific metadata shared by the build and CI workflows.

> [!NOTE]
> To add a regular library dependency, declare it directly in the `dependencies { ... }` block:
>
> ```kotlin
> dependencies {
>   implementation("group:artifact:version")
> }
> ```
>
> Add IntelliJ Platform plugin or module dependencies through the `intellijPlatform { ... }` dependency extension described in the [IntelliJ Platform Gradle Plugin documentation][gh:intellij-platform-gradle-plugin-docs].

Keeping the project in good shape and having all the dependencies up to date requires time and effort, but it is possible to automate that process using [Dependabot][gh:dependabot].

Dependabot is a bot provided by GitHub that checks build configuration files for outdated or insecure dependencies.
When an update is available, it creates a new pull request providing [the proper change][gh:dependabot-pr].

> [!NOTE]
> Dependabot supports [Gradle Wrapper updates][gh:dependabot-supported-ecosystems].
> To update Gradle manually, check the [Gradle Releases][gradle:releases] page and run
> ```bash
> ./gradlew wrapper --gradle-version <version> && ./gradlew wrapper
> ```

### Changelog maintenance

When releasing an update, it is essential to let your users know what the new version offers.
The best way to do this is to provide release notes.

The changelog is a curated list that contains information about any new features, fixes, and deprecations.
When they're provided, these lists are available in a few different places:
- the [CHANGELOG.md](./CHANGELOG.md) file,
- the [Releases page][gh:releases],
- the *What's new* section of the JetBrains Marketplace Plugin page,
- and inside the Plugin Manager's item details.

There are many methods for handling the project's changelog.
The one used in the current template project is the [Keep a Changelog][keep-a-changelog] approach.

The [Gradle Changelog Plugin][gh:gradle-changelog-plugin] takes care of propagating information provided within the [CHANGELOG.md](./CHANGELOG.md) to the [IntelliJ Platform Gradle Plugin][gh:intellij-platform-gradle-plugin].
You only have to take care of writing down the actual changes in proper sections of the `[Unreleased]` section.

You start with an almost empty changelog:

```
# YourPlugin Changelog

## [Unreleased]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
```

Now proceed with providing more entries to the `Added` group, or any other one that suits your change the most (see [How do I make a good changelog?][keep-a-changelog-how] for more details).

When releasing a plugin update, you don't have to care about bumping the `[Unreleased]` header to the upcoming version – it will be handled automatically on the Continuous Integration (CI) after you publish your plugin.
GitHub Actions will swap it and provide you with an empty section for the next release so that you can proceed with your development:

```
# YourPlugin Changelog

## [Unreleased]

## [0.0.1]
### Added
- An awesome feature
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

### Fixed
- One annoying bug
```

To configure how the Changelog plugin behaves, i.e., to create headers with the release date, see the [Gradle Changelog Plugin][gh:gradle-changelog-plugin] README file.

### Release flow

The release process depends on the workflows already described above.
When your main branch receives a new pull request or a direct push, the [Build](.github/workflows/build.yml) workflow runs multiple tests on your plugin and prepares a draft release.

![Release draft][file:draft-release.png]

The draft release is a working copy of a release, which you can review before publishing.
It uses the current plugin version from [`gradle.properties`][file:gradle.properties] as both the title and git tag, for example, `0.0.1`.
The changelog is provided automatically using the [gradle-changelog-plugin][gh:gradle-changelog-plugin].
The built plugin archive is uploaded as a workflow artifact during the Build run rather than attached to the draft release itself.
Every new Build overrides the previous draft to keep your *Releases* page clean.

When you edit the draft and use the <kbd>Publish release</kbd> button, GitHub will tag your repository with the given version and add a new entry to the Releases tab.
Next, it will notify users who are *watching* the repository, triggering the final [Release](.github/workflows/release.yml) workflow.

### Plugin signing

Plugin Signing is a mechanism introduced in the 2021.2 release cycle to increase security in [JetBrains Marketplace](https://plugins.jetbrains.com) and all of our IntelliJ-based IDEs.

JetBrains Marketplace signing is designed to ensure that plugins aren't modified over the course of the publishing and delivery pipeline.

The current template keeps signing configuration out of `build.gradle.kts` and relies on the standard environment variables consumed by the IntelliJ Platform Gradle Plugin.
That allows you to sign and publish your plugin from both the Continuous Integration (CI) and local environments without checking secrets into VCS.

To find out how to generate signing certificates, check the [Plugin Signing][docs:plugin-signing] section in the IntelliJ Platform Plugin SDK documentation.

### Publishing the plugin

> [!TIP]
> Make sure to follow all guidelines listed in [Publishing a Plugin][docs:publishing] to follow all recommended and required steps.

Releasing a plugin to [JetBrains Marketplace](https://plugins.jetbrains.com) is a straightforward operation that uses the `publishPlugin` Gradle task provided by the [intellij-platform-gradle-plugin][gh:intellij-platform-gradle-plugin-docs].
In addition, the [Release](.github/workflows/release.yml) workflow automates this process by running the task when a new release appears in the GitHub Releases section.

> [!NOTE]
> If you need custom Marketplace channels or additional publishing options, add explicit publishing configuration to [`build.gradle.kts`][file:build.gradle.kts] as described in the [publishing documentation][docs:publishing].

The authorization process relies on the `PUBLISH_TOKEN` secret environment variable, specified in the `⚙️ Settings > Secrets and variables > Actions` section of your project repository.

You can get that token in your JetBrains Marketplace profile dashboard in the [My Tokens][jb:my-tokens] tab.

> [!WARNING]
> Before using the automated deployment process, it is necessary to manually create a new plugin in JetBrains Marketplace to specify options like the license, repository URL, etc.
> Please follow the [Publishing a Plugin][docs:publishing] instructions.


## FAQ

### How to use Java in my project?

Java language is supported by default along with Kotlin.
Initially, the `/src/main/kotlin` directory is available with minimal examples.
You can still replace it or add the `/src/main/java` directory to start working with Java language instead.

### How to disable *tests* or *build* job using the `[skip ci]` commit message?

Since February 2021, GitHub Actions [has supported the skip CI feature][github-actions-skip-ci].
If the message contains one of the following strings: `[skip ci]`, `[ci skip]`, `[no ci]`, `[skip actions]`, or `[actions skip]` – workflows will not be triggered.

### Why does the draft release no longer contain a built plugin artifact?

All binaries created by the workflows are still available as workflow artifacts together with failed test reports or Plugin Verifier results.
That approach gives more possibilities for testing and debugging pre-releases, for example, in your local environment.

## Useful links

- [IntelliJ Platform Plugin SDK][docs]
- [IntelliJ Platform Gradle Plugin Documentation][gh:intellij-platform-gradle-plugin-docs]
- [IntelliJ Platform Explorer][jb:ipe]
- [JetBrains Marketplace Quality Guidelines][jb:quality-guidelines]
- [IntelliJ Platform UI Guidelines][jb:ui-guidelines]
- [JetBrains Marketplace Paid Plugins][jb:paid-plugins]
- [Kotlin UI DSL][docs:kotlin-ui-dsl]
- [IntelliJ SDK Code Samples][gh:code-samples]
- [JetBrains Platform Slack][jb:slack]
- [JetBrains Platform Twitter][jb:twitter]
- [IntelliJ IDEA Open API and Plugin Development Forum][jb:forum]
- [Keep a Changelog][keep-a-changelog]
- [GitHub Actions][gh:actions]

[docs]: https://plugins.jetbrains.com/docs/intellij?from=IJPluginTemplate
[docs:intro]: https://plugins.jetbrains.com/docs/intellij/intellij-platform.html?from=IJPluginTemplate
[docs:kotlin-ui-dsl]: https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html?from=IJPluginTemplate
[docs:kotlin]: https://plugins.jetbrains.com/docs/intellij/using-kotlin.html?from=IJPluginTemplate
[docs:kotlin-stdlib]: https://plugins.jetbrains.com/docs/intellij/using-kotlin.html?from=IJPluginTemplate#kotlin-standard-library
[docs:plugin.xml]: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html?from=IJPluginTemplate
[docs:publishing]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate
[docs:using-gradle]: https://plugins.jetbrains.com/docs/intellij/developing-plugins.html?from=IJPluginTemplate
[docs:plugin-signing]: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate
[docs:project-structure-settings]: https://www.jetbrains.com/help/idea/project-settings-and-structure.html
[docs:testing-plugins]: https://plugins.jetbrains.com/docs/intellij/testing-plugins.html?from=IJPluginTemplate
[docs:integration-tests]: https://plugins.jetbrains.com/docs/intellij/integration-tests.html?from=IJPluginTemplate
[docs:integration-tests-ui]: https://plugins.jetbrains.com/docs/intellij/integration-tests-ui.html?from=IJPluginTemplate

[file:build.gradle.kts]: ./build.gradle.kts
[file:draft-release.png]: ./.github/readme/draft-release.png
[file:get-from-version-control]: ./.github/readme/get-from-version-control.png
[file:gradle.properties]: ./gradle.properties
[file:intellij-platform-plugin-template-dark]: ./.github/readme/intellij-platform-plugin-template-dark.svg#gh-dark-mode-only
[file:intellij-platform-plugin-template-light]: ./.github/readme/intellij-platform-plugin-template-light.svg#gh-light-mode-only
[file:project-structure-sdk.png]: ./.github/readme/project-structure-sdk.png
[file:plugin.xml]: ./src/main/resources/META-INF/plugin.xml
[file:run-debug-configurations.png]: ./.github/readme/run-debug-configurations.png
[file:run-logs.png]: ./.github/readme/run-logs.png
[file:settings-secrets.png]: ./.github/readme/settings-secrets.png
[file:settings.gradle.kts]: ./settings.gradle.kts
[file:template_cleanup.yml]: ./.github/workflows/template-cleanup.yml
[file:use-this-template.png]: ./.github/readme/use-this-template.png

[gh:actions]: https://docs.github.com/actions
[gh:build]: https://github.com/JetBrains/intellij-platform-plugin-template/actions?query=workflow%3ABuild
[gh:code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[gh:dependabot]: https://docs.github.com/en/code-security/dependabot/dependabot-version-updates
[gh:dependabot-pr]: https://github.com/JetBrains/intellij-platform-plugin-template/pull/73
[gh:dependabot-supported-ecosystems]: https://docs.github.com/en/code-security/dependabot/ecosystems-supported-by-dependabot/supported-ecosystems-and-repositories#gradle
[gh:gradle-changelog-plugin]: https://github.com/JetBrains/gradle-changelog-plugin
[gh:intellij-platform-gradle-plugin]: https://github.com/JetBrains/intellij-platform-gradle-plugin
[gh:intellij-platform-gradle-plugin-docs]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
[gh:intellij-platform-gradle-plugin-runIde]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#runIde
[gh:intellij-platform-gradle-plugin-verifyPlugin]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#verifyPlugin
[gh:releases]: https://github.com/JetBrains/intellij-platform-plugin-template/releases

[gradle]: https://gradle.org
[gradle:build-cache]: https://docs.gradle.org/current/userguide/build_cache.html
[gradle:configuration-cache]: https://docs.gradle.org/current/userguide/configuration_cache.html
[gradle:kotlin-dsl]: https://docs.gradle.org/current/userguide/kotlin_dsl.html
[gradle:lifecycle-tasks]: https://docs.gradle.org/current/userguide/java_plugin.html#lifecycle_tasks
[gradle:releases]: https://gradle.org/releases

[jb:github]: https://github.com/JetBrains/.github/blob/main/profile/README.md
[jb:download-ij]: https://www.jetbrains.com/idea/download
[jb:forum]: https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development
[jb:ipe]: https://jb.gg/ipe
[jb:my-tokens]: https://plugins.jetbrains.com/author/me/tokens
[jb:paid-plugins]: https://plugins.jetbrains.com/docs/marketplace/paid-plugins-marketplace.html
[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html
[jb:slack]: https://plugins.jetbrains.com/slack
[jb:twitter]: https://twitter.com/JBPlatform
[jb:ui-guidelines]: https://jetbrains.github.io/ui

[github-actions-skip-ci]: https://github.blog/changelog/2021-02-08-github-actions-skip-pull-request-and-push-workflows-with-skip-ci/
[keep-a-changelog]: https://keepachangelog.com
[keep-a-changelog-how]: https://keepachangelog.com/en/1.0.0/#how
[semver]: https://semver.org
