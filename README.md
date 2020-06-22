# IntelliJ Platform Plugin Template

[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:confluence-on-gh]
![Build](https://github.com/JetBrains/intellij-platform-plugin-template/workflows/Build/badge.svg)
[![Slack](https://img.shields.io/badge/Slack-%23intellij--platform--plugin--template-blue)][jb:slack]

> **TL;DR:** Click the <kbd>Use the template</kbd> button and clone it in the IntelliJ IDEA.

<!-- Plugin description -->
**IntelliJ Platform Plugin Template** is a repository that provides a pure boilerplate for creating a plugin project
with ease designed as a **GitHub Template Repository** (check the [Creating a repository from a template][gh:template]
article).

The main goal for this Template is to speed up the setup phase of the plugin development for the new as well as existing
developers by preconfiguring the project scaffold, CI and linking to the proper documentation pages and keeping
everything in the most straightforward manner.
<!-- Plugin description end -->

If you're still not sure, what is this about - read our introduction of [What is the IntelliJ Platform?][docs:intro]

We can highlight the following parts of the template project:

- [Gradle Configuration](#gradle-configuration)
- [Plugin Template Structure](#plugin-template-structure)
- [Plugin Configuration File](#plugin-configuration-file)
- [Sample Code](#sample-code):
    - listeners - project and dynamic plugin lifecycle
    - services - project- and application-related services 
    - actions - basic action with shortcut binding
- [Continuous Integration](#continuous-integration) based on the GitHub Actions
    - [Changelog Maintenance](#changelog-maintenance) with the Gradle Changelog Plugin
    - [Release Flow](#release-flow) using the GitHub Releases
    - [Publishing Plugin](#publishing-plugin) with the Gradle IntelliJ Plugin

## Getting Started

Before diving into the plugin development and everything that happens around, it is worth mentioning the fundamental
idea behind the GitHub Templates: by creating a new project using the current template, you start with no history
and no reference to this repository - it is the cut corner for creating a new repository with copy-pasting the content
or cloning repositories and clearing the history by your own.

The only thing that you have to do is clicking the <kbd>Use this template</kbd> button.

![Use this template][file:use-this-template.png]

After creating your blank project from the template, there will be the [Template Cleanup][file:template_cleanup.yml]
workflow triggered to override or remove the template-specific configuration, like plugin name, current changelog, etc.
When done, the project is ready to be cloned on your local environment and opened with
the [IntelliJ IDEA][jb:download-ij].

As the last step, you have to manually review the configuration variables described in the
[gradle.properties][file:gradle.properties] file, *optionally* move sources from the *com.github.username.repository*
package to the one that fits you the most, and start implementing your ideas.

## Gradle Configuration

The recommended way of the plugin development is using the [Gradle][gradle] setup with
[gradle-intellij-plugin][gh:gradle-intellij-plugin] installed. The gradle-intellij-plugin provides tasks to run
the IDE with your plugin and to publish your plugin to the Marketplace Repository. 

IntelliJ Platform Plugin Template project provides already preconfigured Gradle configuration - feel free to follow
the [Using Gradle][docs:using-gradle] articles for better understanding and customisation of your build.

The most significant parts of the current configuration are:
- Configuration is written with [Gradle Kotlin DSL][gradle-kotlin-dsl]
- Kotlin support with a possibility to write Java code
- Integration with [gradle-changelog-plugin][gh:gradle-changelog-plugin] for the automated patching of the change notes
  and description consumed from `CHANGELOG.md` and `README.md` files
- Integration with [gradle-intellij-plugin][gh:gradle-intellij-plugin] for better development impressions
- Code linting with [detekt][detekt]
- [Plugin publishing][docs:publishing] using the token

Project-specific configuration file - [gradle.properties][file:gradle.properties] - contains:

| Property name             | Description                                                                           |
| ------------------------- | ------------------------------------------------------------------------------------- |
| `pluginGroup`             | Package name - after *using* the template, will be set to `com.gtihub.username.repo`. |
| `pluginName`              | Name of the plugin displayed in the Marketplace and Plugins Repository.               |
| `pluginVersion`           | The current version of the plugin.                                                        |
| `pluginSinceBuild`        | `since-build` attribute of the <idea-version> tag.                                    |
| `pluginUntilBuild`        | `until-build` attribute of the <idea-version> tag.                                    |
| `platformType`            | The type of IDE distribution.                                                         |
| `platformVersion`         | The version of the IntelliJ Platform IDE that will be used to build the plugin.       |
| `platformDownloadSources` | Download IDE sources while initializing Gradle build.                                 |

Listed properties define the plugin itself or configure the [gradle-intellij-plugin][gh:gradle-intellij-plugin]
- check its documentation for more details. 

## Plugin Template Structure

Generated IntelliJ Template repository contains the following content structure:

```
.
├── CHANGELOG.md            Full changes history.
├── LICENSE                 License, MIT by default
├── README.md               README
├── build/                  Output build directory
├── build.gradle.kts        Gradle configuration
├── detekt-config.yml       Detekt configuration
├── gradle
│   └── wrapper/            Gradle Wrapper
├── gradle.properties       Gradle configuration properties
├── gradlew                 *nix Gradle Wrapper binary
├── gradlew.bat             Windows Gradle Wrapper binary
└── src                     Plugin sources
    └── main
        ├── kotlin/         Kotlin source files
        ├── java/           Java source files
        └── resources/      Resources - plugin.xml, icons, messages
```

Beside of the configuration files, the most important part is the `src` directory containing our implementation
and plugin's manifest - [plugin.xml][file:plugin.xml].

## Plugin Configuration File

Plugin Configuration File is a [plugin.xml][file:plugin.xml] file located in the `src/main/resources/META-INF`
directory. It describes the overall information about the plugin, its dependencies, extensions, and listeners.

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

You can read more about that file in [IntelliJ Platform SDK DevGuide][docs:plugin.xml].

## Sample Code

The prepared template is aiming to provide as less code as possible because it is barely possible to fulfil
the requirements of the various types of the plugins (language support, build tools, VCS related tools) with some
general scaffold. Having that in mind, it contains few following files:

```
.
├── MyBundle.kt                         Bundle class providing access to the resources messages
├── listeners
│   └── MyProjectManagerListener.kt     Project Manager listener - handles project lifecycle
└── services
    ├── MyApplicationService.kt         Application-level service available for all projects
    └── MyProjectService.kt             Project level service
```

Above files location is `src/main/kotlin`, which indicates the used language - if you will decide to use Java instead,
sources should be located in `src/main/java` directory. 

## Continuous Integration

Continuous Integration depends on the [GitHub Actions][gh:actions], which is a set of workflows that let to automate
your testing and releasing process. Thanks to such automation, you can delegate the testing and verification phases
to the CI and focus on the development (and writing more tests).

In `.github/workflows` directory you may find the following GitHub Actions workflows defined:

- [Build](.github/workflows/build.yml)
    - Triggered on `push` and `pull_request` events
    - Runs *Gradle Wrapper Validation Action* to verify the wrapper's checksum
    - Runs `verifyPlugin` and test Gradle tasks
    - Builds plugin with `buildPlugin` Gradle task and provide the artifact for the next workflow jobs
    - Verifies built plugin using *IntelliJ Plugin Verifier* tool
    - Prepares a draft release for GitHub Releases page for the manual verification 
- [Release](.github/workflows/release.yml)
    - Triggered on `released` event
    - Publishes the plugin to the Marketplace using `PUBLISH_TOKEN` provided token
    - Patches the Changelog and commits
- [Template Cleanup](.github/workflows/template-cleanup.yml) 
    - Triggered once on `push` event when a new template-based repository has been created
    - Overrides scaffold with files from `.github/template-cleanup` directory
    - Overrides JetBrains-specific sentences or package names with the ones specific to the target repository
    - Removes redundant files

Each workflow file has an accurate documentation provided, so don't hesitate to look through their sources.

### Changelog Maintenance

When delivering a new release, it is essential to let your audience know what the updated version is offering.
The best way of handling that is to attach the release note.

The changelog is a curated list containing information of any new features, fixes, deprecations.
If provided, such list would be available in a couple of places: [CHANGELOG.md](./CHANGELOG.md) file,
[Releases page][gh:releases], [What's new][jb:plugin-page] section in Marketplace's Plugin page
and inside of the Plugin Manager's item details.

There are many methods for handling the project's changelog. One of them, used in the current template project,
is the [Keep a Changelog][keep-a-changelog] approach, which brings the *Guiding Principles* and *Types of Changes*
that mey help you with the proper crafting of the change notes.

### Release Flow

Releasing process depends on the already described workflows - when your main branch receives a new Pull Request
or a regular push, [Build](.github/workflows/build.yml) workflow tests your plugin at different angles and prepares
a draft release.

![Release draft][file:draft-release.png]

The draft release is a working copy of a release, which you can review, before publishing. It has a predefined title
and git tag name, which is the current plugin's version - i.e. `v0.0.1`. The changelog is provided automatically using
the [gradle-changelog-plugin][gh:gradle-changelog-plugin]. There is also an artifact file with built plugin attached.
Every next *Build* overrides (or creates one if absent) such a draft to keep your *Releases* page clean.

By editing the draft and using the <kbd>Publish release</kbd> button, GitHub will tag your repository with the given
version and add a new entry to the Releases tab. In the next steps, it will notify users that are *watching* repository
and trigger the final [Release](.github/workflows/release.yml) workflow.

### Publishing Plugin

Releasing plugin to the Marketplace is a straightforward operation which uses `publishPlugin` Gradle task provided
by the [gradle-intellij-plugin][gh:gradle-intellij-plugin]. [Release](.github/workflows/release.yml) workflow automates
that process by running the task when a new release appears in the GitHub Releases section.

Authorization process relies on the `PUBLISH_TOKEN` secret environment variable, which has to be provided
in the repository Settings in the Secrets section.

![Settings > Secrets][file:settings-secrets.png]

You can find out how to get that token in the [Providing Your Hub Permanent Token to Gradle][docs:token] article.

> **Important:**
> Before using the automated deployment process, it is required to manually create a new plugin in the Marketplace
to specify options like the license, repository URL etc. Follow the [Publishing a Plugin][docs:publishing] instructions.

## Useful Links

- [IntelliJ Platform SDK DevGuide][docs]
- [IntelliJ Platform UI Guidelines][jb:ui-guidelines]
- [Kotlin UI DSL][docs:kotlin-ui-dsl]
- [IntelliJ SDK Code Samples][gh:code-samples]
- [JetBrains Platform Slack][jb:slack]
- [IntelliJ IDEA Open API and Plugin Development Forum][jb:forum]
- [Keep a Changelog][keep-a-changelog]
- [GitHub Actions][gh:actions]

[docs]: https://www.jetbrains.org/intellij/sdk/docs
[docs:intro]: https://www.jetbrains.org/intellij/sdk/docs/intro/intellij_platform.html
[docs:kotlin-ui-dsl]: https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/kotlin_ui_dsl.html
[docs:plugin.xml]: https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_configuration_file.html
[docs:publishing]: https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html
[docs:token]: https://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#providing-your-hub-permanent-token-to-gradle
[docs:using-gradle]: https://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system.html

[file:use-this-template.png]: .github/readme/use-this-template.png
[file:draft-release.png]: .github/readme/draft-release.png
[file:gradle.properties]: ./gradle.properties
[file:plugin.xml]: ./src/main/resources/META-INF/plugin.xml
[file:settings-secrets.png]: .github/readme/settings-secrets.png
[file:template_cleanup.yml]: ./.github/workflows/template-cleanup.yml

[gh:actions]: https://help.github.com/en/actions
[gh:code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[gh:gradle-changelog-plugin]: https://github.com/JetBrains/gradle-changelog-plugin
[gh:gradle-intellij-plugin]: https://github.com/JetBrains/gradle-intellij-plugin
[gh:releases]: https://github.com/JetBrains/intellij-platform-plugin-template/releases
[gh:template]: https://help.github.com/en/enterprise/2.20/user/github/creating-cloning-and-archiving-repositories/creating-a-repository-from-a-template

[jb:confluence-on-gh]: https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub
[jb:download-ij]: https://www.jetbrains.com/idea/download
[jb:forum]: https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development
[jb:plugin-page]: https://plugins.jetbrains.com/plugin/0-TODO
[jb:slack]: https://plugins.jetbrains.com/slack
[jb:ui-guidelines]: https://jetbrains.github.io/ui

[keep-a-changelog]: https://keepachangelog.com
[detekt]: https://detekt.github.io/detekt
[gradle]: https://gradle.org
[gradle-kotlin-dsl]: https://docs.gradle.org/current/userguide/kotlin_dsl.html
