# IntelliJ Platform Plugin Template

[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:confluence-on-gh]
[![Twitter Follow](https://img.shields.io/twitter/follow/JBPlatform?style=flat)][jb:twitter]
[![Build](https://github.com/JetBrains/intellij-platform-plugin-template/workflows/Build/badge.svg)][gh:build]
[![Slack](https://img.shields.io/badge/Slack-%23intellij--platform--plugin--template-blue)][jb:slack]

> **TL;DR:** Click the <kbd>Use this template</kbd> button and clone it in IntelliJ IDEA.

<!-- Plugin description -->
**IntelliJ Platform Plugin Template** is a repository that provides a pure boilerplate template to make it easier to create a new plugin project (check the [Creating a repository from a template][gh:template] article).

The main goal of this template is to speed up the setup phase of plugin development for both new and experienced developers by preconfiguring the project scaffold and CI, linking to the proper documentation pages, and keeping everything organized.

[gh:template]: https://help.github.com/en/enterprise/2.20/user/github/creating-cloning-and-archiving-repositories/creating-a-repository-from-a-template
<!-- Plugin description end -->

If you're still not quite sure what this is all about, read our introduction: [What is the IntelliJ Platform?][docs:intro]

> **TIP**: Click the <kbd>Watch</kbd> button on the top to be notified about releases containing new features and fixes.

### Table of contents

In this README, we will highlight the following elements of template-project creation:

- [Getting started](#getting-started)
- [Gradle configuration](#gradle-configuration)
- [Plugin template structure](#plugin-template-structure)
  - [Dependency on the Kotlin standard library](#dependency-on-the-kotlin-standard-library)
- [Plugin configuration file](#plugin-configuration-file)
- [Sample code](#sample-code):
  - listeners – project and dynamic plugin lifecycle
  - services – project-related and application-related services
  - actions – basic action with shortcut binding
- [Predefined Run/Debug configurations](#predefined-rundebug-configurations)
- [Continuous integration](#continuous-integration) based on GitHub Actions
  - [Dependencies management](#dependencies-management) with Dependabot
  - [Changelog maintenance](#changelog-maintenance) with the Gradle Changelog Plugin
  - [Release flow](#release-flow) using GitHub Releases
  - [Plugin signing](#plugin-signing) with your private certificate
  - [Publishing the plugin](#publishing-the-plugin) with the Gradle IntelliJ Plugin
- [FAQ](#faq)
- [Useful links](#useful-links)


## Getting started

Before we dive into plugin development and everything related to it, it's worth mentioning the benefits of using GitHub Templates.
By creating a new project using the current template, you start with no history and no reference to this repository.
This allows you to create a new repository easily without copying and pasting previous content, clone repositories, or clearing the history manually.

All you have to do is click the <kbd>Use this template</kbd> button.

![Use this template][file:use-this-template.png]

After using the template to create your blank project, the [Template Cleanup][file:template_cleanup.yml] workflow will be triggered to override or remove any template-specific configurations, such as the plugin name, current changelog, etc.
Once this is complete, the project is ready to be cloned to your local environment and opened with [IntelliJ IDEA][jb:download-ij].

For the last step, you have to manually review the configuration variables described in the [`gradle.properties`][file:gradle.properties] file and *optionally* move sources from the *com.github.username.repository* package to the one that works best for you.
Then you can get to work implementing your ideas.

> **TIP:** To use Java in your plugin, create the `/src/main/java` directory.


## Gradle configuration

The recommended method for plugin development involves using the [Gradle][gradle] setup with the [gradle-intellij-plugin][gh:gradle-intellij-plugin] installed.
The `gradle-intellij-plugin` makes it possible to run the IDE with your plugin and publish your plugin to JetBrains Marketplace Repository.

A project built using the IntelliJ Platform Plugin Template includes a Gradle configuration that's already been set up.
Feel free to read through the [Using Gradle][docs:using-gradle] articles to understand your build better and learn how to customize it.

The most significant parts of the current configuration are:
- Configuration written with [Gradle Kotlin DSL][gradle-kotlin-dsl].
- Support for Kotlin and Java implementation.
- Integration with the [gradle-changelog-plugin][gh:gradle-changelog-plugin], which automatically patches the change notes and description based on the `CHANGELOG.md` and `README.md` files.
- Integration with the [gradle-intellij-plugin][gh:gradle-intellij-plugin] for smoother development.
- [Plugin publishing][docs:publishing] using the token.

For more details regarding Kotlin integration, please see [Kotlin for Plugin Developers][kotlin-for-plugin-developers] section in the IntelliJ Platform Plugin SDK documentation.

### Gradle properties

The project-specific configuration file [gradle.properties][file:gradle.properties] contains:

| Property name               | Description                                                                                                |
| --------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `pluginGroup`               | Package name - after *using* the template, this will be set to `com.github.username.repo`.                 |
| `pluginName`                | Plugin name displayed in the JetBrains Marketplace and the Plugins Repository.                             |
| `pluginVersion`             | The current version of the plugin.                                                                         |
| `pluginSinceBuild`          | The `since-build` attribute of the <idea-version> tag.                                                     |
| `pluginUntilBuild`          | The `until-build` attribute of the <idea-version> tag.                                                     |
| `pluginVerifierIdeVersions` | Versions of the IDEs that will be used with Plugin Verifier.                                               |
| `platformType`              | The type of IDE distribution.                                                                              |
| `platformVersion`           | The version of the IntelliJ Platform IDE will be used to build the plugin.                                 |
| `platformDownloadSources`   | IDE sources downloaded while initializing the Gradle build.                                                |
| `platformPlugins`           | Comma-separated list of dependencies to the bundled IDE plugins and plugins from the Plugin Repositories.  |
| `javaVersion`               | Java language level used to compile sources and generate the files for - Java 11 is required since 2020.3. |
| `gradleVersion`             | Version of  Gradle used for plugin development.                                                            |

The properties listed define the plugin itself or configure the [gradle-intellij-plugin][gh:gradle-intellij-plugin] – check its documentation for more details.

### Environment variables

Some values used for the Gradle configuration shouldn't be stored in files to avoid publishing them to the Version Control System.

To avoid that, environment variables are introduced, which can be provided within the *Run/Debug Configuration* within the IDE, or on the CI – like for GitHub: `⚙️ Settings > Secrets`.

Environment variables used by the current project are related to the [plugin signing](#plugin-signing) and [publishing](#publishing-the-plugin).

| Environment variable name | Description                                                                                                                                 |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `PRIVATE_KEY`             | Certificate private key, should contain: <code>-----BEGIN ENCRYPTED PRIVATE KEY-----<br/>...<br/>-----END ENCRYPTED PRIVATE KEY-----</code> |
| `PRIVATE_KEY_PASSWORD`    | Password used for encrypting the certificate file.                                                                                          |
| `CERTIFICATE_CHAIN`       | Certificate chain, should contain: <code>-----BEGIN CERTIFICATE-----<br/>...<br/>-----END CERTIFICATE----</code>                            |
| `PUBLISH_TOKEN`           | Publishing token generated in your JetBrains Marketplace profile dashboard.                                                                 |

For more details on how to generate proper values, check the relevant sections mentioned above.

To configure GitHub secret environment variables, go to the `⚙️ Settings > Secrets` section of your project repository:

![Settings > Secrets][file:settings-secrets.png]

There's a `Publish Plugin` preconfigured *Run/Debug Configuration* available within the project, so you can also run the publishing flow from your local environment.
To provide all required secrets, go to the `Publish Plugin` settings and edit the `Environment variables` section:

![Settings > Secrets][file:run-debug-env.png]

> **IMPORTANT:**
> Git tracks changes in the `Publish Plugin` configuration file.
> To ignore changes (tokens, certificates), run:
> ```bash
> git update-index --assume-unchanged ".run/Publish Plugin.run.xml"
> ```

## Plugin template structure

A generated IntelliJ Platform Plugin Template repository contains the following content structure:

```
.
├── .github                 GitHub Actions workflows and Dependabot configuration files
├── .run                    Predefined Run/Debug Configurations
├── gradle
│   └── wrapper/            Gradle Wrapper
├── build/                  Output build directory
├── src                     Plugin sources
│   └── main
│       ├── kotlin/         Kotlin source files
│       └── resources/      Resources - plugin.xml, icons, messages
├── .gitignore              Git ignoring rules
├── build.gradle.kts        Gradle configuration
├── CHANGELOG.md            Full change history.
├── gradle.properties       Gradle configuration properties
├── gradlew                 *nix Gradle Wrapper binary
├── gradlew.bat             Windows Gradle Wrapper binary
├── LICENSE                 License, MIT by default
├── README.md               README
└── settings.gradle.kts     Grade project settings
```

In addition to the configuration files, the most crucial part is the `src` directory, which contains our implementation and the manifest for our plugin – [plugin.xml][file:plugin.xml].

> **TIP:** To use Java in your plugin, create the `/src/main/java` directory.


## Plugin configuration file

The plugin configuration file is a [plugin.xml][file:plugin.xml] file located in the `src/main/resources/META-INF` directory.
It provides general information about the plugin, its dependencies, extensions, and listeners.

```xml
<idea-plugin>
    <id>org.jetbrains.plugins.template</id>
    <name>Template</name>
    <vendor>JetBrains</vendor>
    <depends>com.intellij.modules.platform</depends>

    <extensions defaultExtensionNs="com.intellij">
        <applicationService serviceImplementation="..."/>
        <projectService serviceImplementation="..."/>
    </extensions>

    <projectListeners>
        <listener class="..." topic="..."/>
    </projectListeners>
</idea-plugin>
```

You can read more about this file in the [Plugin Configuration File][docs:plugin.xml] section of our documentation.


## Sample code

The prepared template provides as little code as possible because it is impossible for a general scaffold to fulfill all the specific requirements for all types of plugins (language support, build tools, VCS related tools).
Therefore, the template contains only the following files:

```
.
├── MyBundle.kt                         Bundle class providing access to the resources messages
├── listeners
│   └── MyProjectManagerListener.kt     Project Manager listener - handles project lifecycle
└── services
    ├── MyApplicationService.kt         Application-level service available for all projects
    └── MyProjectService.kt             Project level service
```

These files are located in `src/main/kotlin`.
This location indicates the language being used.
So if you decide to use Java instead (or in addition to Kotlin), these sources should be located in the `src/main/java` directory.

To start with the actual implementation, you may check our [IntelliJ Platform SDK DevGuide][docs], which contains an introduction to the essential areas of the plugin development together with dedicated tutorials.

For those, who value example codes the most, there are also available [IntelliJ SDK Code Samples][gh:code-samples] and [IntelliJ Platform Explorer][jb:ipe] – a search tool for browsing Extension Points inside existing implementations of open-source IntelliJ Platform plugins.


## Predefined Run/Debug configurations

Within the default project structure, there is a `.run` directory provided containing three predefined *Run/Debug configurations* that expose corresponding Gradle tasks:

![Run/Debug configurations][file:run-debug-configurations.png]

| Configuration name | Description                                                                                                                                                            |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Run Plugin         | Runs [`:runIde`][gh:gradle-intellij-plugin-running-dsl] Gradle IntelliJ Plugin task. Use the *Debug* icon for plugin debugging.                                        |
| Run Tests          | Runs [`:test`][gradle-lifecycle-tasks] Gradle task.                                                                                                                    |
| Run Verifications  | Runs [`:runPluginVerifier`][gh:gradle-intellij-plugin-verifier-dsl] Gradle IntelliJ Plugin task to check the plugin compatibility against the specified IntelliJ IDEs. |
| Publish Plugin     | Runs `signPlugin` and `publishPlugin` Gradle tasks. Check [Environment variables](#environment-variables) section for more details on how to configure it.             |


> **TIP:** You can find the logs from the running task in the `idea.log` tab.
>
> ![Run/Debug configuration logs][file:run-logs.png]


## Continuous integration

Continuous integration depends on [GitHub Actions][gh:actions], a set of workflows that make it possible to automate your testing and release process.
Thanks to such automation, you can delegate the testing and verification phases to the Continuous Integration (CI) and instead focus on development (and writing more tests).

In the `.github/workflows` directory, you can find definitions for the following GitHub Actions workflows:

- [Build](.github/workflows/build.yml)
  - Triggered on `push` and `pull_request` events.
  - Runs the *Gradle Wrapper Validation Action* to verify the wrapper's checksum.
  - Runs the `verifyPlugin` and `test` Gradle tasks.
  - Builds the plugin with the `buildPlugin` Gradle task and provides the artifact for the next jobs in the workflow.
  - Verifies the plugin using the *IntelliJ Plugin Verifier* tool.
  - Prepares a draft release of the GitHub Releases page for manual verification.
- [Release](.github/workflows/release.yml)
  - Triggered on `released` event.
  - Signs the plugin with a provided certificate before publishing.
  - Publishes the plugin to JetBrains Marketplace using the provided `PUBLISH_TOKEN`.
  - Sets publish channel depending on the plugin version, i.e. `1.0.0-beta` -> `beta` channel.
  - Patches the Changelog and commits.
- [Template Cleanup](.github/workflows/template-cleanup.yml)
  - Triggered once on the `push` event when a new template-based repository has been created.
  - Overrides the scaffold with files from the `.github/template-cleanup` directory.
  - Overrides JetBrains-specific sentences or package names with ones specific to the target repository.
  - Removes redundant files.

All the workflow files have accurate documentation, so it's a good idea to take a look through their sources.

### Dependencies management

This Template project depends on Gradle plugins and external libraries – and during the development, you will add more of them.

Keeping the project in good shape and having all the dependencies up-to-date requires time and effort, but it is possible to automate that process using [Dependabot][gh:dependabot].

Dependabot is a bot provided by GitHub for checking the build configuration files and reviewing any outdated or insecure dependencies of yours – in case if any update is available, it creates a new pull request providing [the proper change][gh:dependabot-pr].

> **Note:** Dependabot doesn't yet support checking of the Gradle Wrapper.
> Check the [Gradle Releases][gradle-releases] page and update your `gradle.properties` file with:
> ```properties
> gradleVersion = 7.1.1
> ```
> and run
> ```bash
> ./gradlew wrapper
> ```

### Changelog maintenance

When releasing an update, it is essential to let your users know what the new version offers.
The best way to do this is to provide release notes.

The changelog is a curated list that contains information about any new features, fixes, and deprecations.
When they are provided, these lists are available in a few different places:
- the [CHANGELOG.md](./CHANGELOG.md) file,
- the [Releases page][gh:releases],
- the *What's new* section of JetBrains Marketplace Plugin page,
- and inside the Plugin Manager's item details.

There are many methods for handling the project's changelog.
The one used in the current template project is the [Keep a Changelog][keep-a-changelog] approach.

The [Gradle Changelog Plugin][gh:gradle-changelog-plugin] takes care of propagating information provided within the [CHANGELOG.md](./CHANGELOG.md) to the [Gradle IntelliJ Plugin][gh:gradle-intellij-plugin].
The only thing you have to take care of is writing down the actual changes in proper sections of the `[Unreleased]` section.

You start with an almost empty changelog:

```
# YourPlugin Changelog

## [Unreleased]
### Added
- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
```

Now proceed with providing more entries to the `Added` group, or any other one that suits your change the most (see [How do I make a good changelog?][keep-a-changelog-how] for more details).

When releasing a plugin update, you don't have to care about bumping the `[Unreleased]` header to the upcoming version – it will be handled automatically on the Continuous Integration (CI) after you publish your plugin.
GitHub Actions will swap it and provide you an empty section for the next release so that you can proceed with your development:

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

To configure how the Changelog plugin behaves, i.e., to create headers with the release date, see [Gradle Changelog Plugin][gh:gradle-changelog-plugin] README file.

### Release flow

The release process depends on the workflows already described above.
When your main branch receives a new pull request or a direct push, the [Build](.github/workflows/build.yml) workflow runs multiple tests on your plugin and prepares a draft release.

![Release draft][file:draft-release.png]

The draft release is a working copy of a release, which you can review before publishing.
It includes a predefined title and git tag, the current version of the plugin, for example, `v0.0.1`.
The changelog is provided automatically using the [gradle-changelog-plugin][gh:gradle-changelog-plugin].
An artifact file is also built with the plugin attached.
Every new Build overrides the previous draft to keep your *Releases* page clean.

When you edit the draft and use the <kbd>Publish release</kbd> button, GitHub will tag your repository with the given version and add a new entry to the Releases tab.
Next, it will notify users that are *watching* the repository, and it will trigger the final [Release](.github/workflows/release.yml) workflow.

### Plugin signing

Plugin Signing is a mechanism introduced in the 2021.2 release cycle to increase security in [JetBrains Marketplace](https://plugins.jetbrains.com) and all of our IntelliJ-based IDEs.

JetBrains Marketplace signing is designed to ensure that plugins are not modified over the course of the publishing and delivery pipeline.

The current project provides a predefined plugin signing configuration that lets you sign and publish your plugin from the Continuous Integration (CI) and local environments.
All the configuration related to the signing should be provided using [environment variables](#environment-variables).

To find out how to generate signing certificates, check the [Plugin Signing][docs:plugin-signing] section in the IntelliJ Platform Plugin SDK documentation.

### Publishing the plugin

Releasing a plugin to JetBrains Marketplace is a straightforward operation that uses the `publishPlugin` Gradle task provided by the [gradle-intellij-plugin][gh:gradle-intellij-plugin].
In addition, the [Release](.github/workflows/release.yml) workflow automates this process by running the task when a new release appears in the GitHub Releases section.

> **TIP**: Set a suffix to the plugin version to publish it in the custom repository channel, i.e. `v1.0.0-beta` will push your plugin to the `beta` [release channel][docs:release-channel].

The authorization process relies on the `PUBLISH_TOKEN` secret environment variable, which must be specified in the _Secrets_ section of the repository _Settings_.

You can get that token in the [My Tokens][jb:my-tokens] tab within your JetBrains Marketplace profile dashboard.

> **Important:**
> Before using the automated deployment process, it is necessary to manually create a new plugin in JetBrains Marketplace to specify options like the license, repository URL, etc.
> Please follow the [Publishing a Plugin][docs:publishing] instructions.


## FAQ

### How to use Java in my project?

Java language is supported by default along with Kotlin.
Initially, the `/src/main/kotlin` directory is available with minimal examples.
You can still replace it or add the `/src/main/java` directory to start working with Java language instead.

### How to disable *tests* or *build* job using the `[skip ci]` commit message?

Since the February 2021, GitHub Actions [support the skip CI feature][github-actions-skip-ci].
If the message contains one of the following strings: `[skip ci]`, `[ci skip]`, `[no ci]`, `[skip actions]`, or `[actions skip]` – workflows will not be triggered.


## Useful links

- [IntelliJ Platform SDK DevGuide][docs]
- [IntelliJ Platform Explorer][jb:ipe]
- [Marketplace Quality Guidelines][jb:quality-guidelines]
- [IntelliJ Platform UI Guidelines][jb:ui-guidelines]
- [Marketplace Paid Plugins][jb:paid-plugins]
- [Kotlin UI DSL][docs:kotlin-ui-dsl]
- [IntelliJ SDK Code Samples][gh:code-samples]
- [JetBrains Platform Slack][jb:slack]
- [JetBrains Platform Twitter][jb:twitter]
- [IntelliJ IDEA Open API and Plugin Development Forum][jb:forum]
- [Keep a Changelog][keep-a-changelog]
- [GitHub Actions][gh:actions]

[docs]: https://plugins.jetbrains.com/docs/intellij?from=IJPluginTemplate
[docs:intro]: https://plugins.jetbrains.com/docs/intellij/intellij-platform.html?from=IJPluginTemplate
[docs:kotlin-ui-dsl]: https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl.html?from=IJPluginTemplate
[docs:plugin.xml]: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html?from=IJPluginTemplate
[docs:publishing]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate
[docs:release-channel]: https://plugins.jetbrains.com/docs/intellij/deployment.html?from=IJPluginTemplate#specifying-a-release-channel
[docs:using-gradle]: https://plugins.jetbrains.com/docs/intellij/gradle-build-system.html?from=IJPluginTemplate
[docs:plugin-signing]: https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate

[file:use-this-template.png]: .github/readme/use-this-template.png
[file:draft-release.png]: .github/readme/draft-release.png
[file:gradle.properties]: ./gradle.properties
[file:run-logs.png]: .github/readme/run-logs.png
[file:plugin.xml]: ./src/main/resources/META-INF/plugin.xml
[file:run-debug-configurations.png]: .github/readme/run-debug-configurations.png
[file:settings-secrets.png]: .github/readme/settings-secrets.png
[file:run-debug-env.png]: .github/readme/run-debug-env.png
[file:template_cleanup.yml]: ./.github/workflows/template-cleanup.yml

[gh:actions]: https://help.github.com/en/actions
[gh:dependabot]: https://docs.github.com/en/free-pro-team@latest/github/administering-a-repository/keeping-your-dependencies-updated-automatically
[gh:code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[gh:gradle-changelog-plugin]: https://github.com/JetBrains/gradle-changelog-plugin
[gh:gradle-intellij-plugin]: https://github.com/JetBrains/gradle-intellij-plugin
[gh:gradle-intellij-plugin-running-dsl]: https://github.com/JetBrains/gradle-intellij-plugin#running-dsl
[gh:gradle-intellij-plugin-verifier-dsl]: https://github.com/JetBrains/gradle-intellij-plugin#plugin-verifier-dsl
[gh:releases]: https://github.com/JetBrains/intellij-platform-plugin-template/releases
[gh:build]: https://github.com/JetBrains/intellij-platform-plugin-template/actions?query=workflow%3ABuild
[gh:dependabot-pr]: https://github.com/JetBrains/intellij-platform-plugin-template/pull/73

[jb:confluence-on-gh]: https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub
[jb:download-ij]: https://www.jetbrains.com/idea/download
[jb:forum]: https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development
[jb:ipe]: https://plugins.jetbrains.com/intellij-platform-explorer
[jb:my-tokens]: https://plugins.jetbrains.com/author/me/tokens
[jb:paid-plugins]: https://plugins.jetbrains.com/docs/marketplace/paid-plugins-marketplace.html
[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html
[jb:slack]: https://plugins.jetbrains.com/slack
[jb:twitter]: https://twitter.com/JBPlatform
[jb:ui-guidelines]: https://jetbrains.github.io/ui

[keep-a-changelog]: https://keepachangelog.com
[keep-a-changelog-how]: https://keepachangelog.com/en/1.0.0/#how
[github-actions-skip-ci]: https://github.blog/changelog/2021-02-08-github-actions-skip-pull-request-and-push-workflows-with-skip-ci/
[gradle]: https://gradle.org
[gradle-releases]: https://gradle.org/releases
[gradle-kotlin-dsl]: https://docs.gradle.org/current/userguide/kotlin_dsl.html
[gradle-lifecycle-tasks]: https://docs.gradle.org/current/userguide/java_plugin.html#lifecycle_tasks
[kotlin-for-plugin-developers]: https://plugins.jetbrains.com/docs/intellij/kotlin.html#adding-kotlin-support
