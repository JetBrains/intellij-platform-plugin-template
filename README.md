# IntelliJ Plugin Template

[![official JetBrains project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
![Build](https://github.com/JetBrains/intellij-plugin-template/workflows/Build/badge.svg)
[![Slack](https://img.shields.io/badge/Slack-%23intellij--plugin--template-blue)](https://plugins.jetbrains.com/slack)

<!-- Plugin description -->
**IntelliJ Plugin Template** is a repository that provides a pure boilerplate for creating a plugin project with ease
designed as a **GitHub Template Repository** (check the [Creating a repository from a template][gh-template] article).
The main goal for this Template is to speed up the setup phase of the plugin development for the new as well as existing
developers by preconfiguring the project scaffold, CI and linking to the proper documentation pages and keeping
everything in the most straightforward manner.
<!-- Plugin description end -->

If you're still not sure, what is this about - read our introduction of [What is the IntelliJ Platform?][docs:intro]

We can highlight here the following parts:

- Gradle configuration
- Sample code:
  - listeners - project and dynamic plugin lifecycle
  - services - project- and application-related services 
  - actions - basic action with shortcut binding
- Plugin Manifest file
- README Template
- CI based on GitHub actions
- Release and changelog maintenance flow

## Getting Started

Before diving into the plugin development and everything that happens around, it is worth mentioning the fundamental
idea behind the GitHub Templates: by creating a new project using the current template, you start with no history
and no reference to this repository - it is the cut corner for creating a new repository with copy-pasting the content
or cloning repositories and clearing the history by your own.

The only thing that you have to do is clicking the <kbd>Use this template</kbd> button.

![Use this template][file:getting-started_use-this-template.png]

After creating your blank project from the template, there will be the [Template Cleanup][file:template_cleanup.yml]
workflow triggered to override or remove the template-specific configuration, like plugin name, current changelog, etc.
When done, project is ready to be cloned on your local environment and opened with the [IntelliJ IDEA][download-ij].

As the last step, you have to manually review the configuration variables described in the
[gradle.properties][file:gradle.properties] file, *optionally* move sources from the *com.github.username.repository*
package to the one that fits you the most and start implementing your ideas.

## Gradle

The recommended way of the plugin development is using the [Gradle][gradle] setup with
[gradle-intellij-plugin][gradle-intellij-plugin] installed. The gradle-intellij-plugin provides tasks to run the IDE
with your plugin and to publish your plugin to the Marketplace Repository. 

IntelliJ Plugin Template project provides already preconfigured Gradle configuration, however feel free to follow
the [Using Gradle][docs:using-gradle] articles for better understanding and customisation of your build.

The most significant parts of the current configuration are:
- Configuration written with [Gradle Kotlin DSL][gradle-kotlin-dsl]
- Kotlin support with possibility to write Java code
- Plugin XML patching with the latest *change notes* from the `CHANGELOG.md` file
- Plugin XML patching with the *description* from the `README.md` file
- Code linting with [detekt][detekt]
- [Plugin publishing][docs:publishing] using token

Project specific configuration file - [gradle.properties][file:gradle.properties] - contains:

```properties
pluginGroup = org.jetbrains.plugins.template
pluginName = Template
pluginVersion = 0.0.2
pluginSinceBuild = 193
pluginUntilBuild = 202

platformType = IC
platformVersion = 2020.1
platformDownloadSources = true
```



| Property name | Description |
| ------------- | ----------- |
| `pluginGroup` | Package name - after *using* the template, will be set to `com.gtihub.username.repo`. |
| `pluginName`  | Name of the plugin displayed in the Marketplace and Plugins Repository.                           |
| `pluginVersion` | Current version of the plugin. |
| `pluginSinceBuild` | `since-build` attribute of the <idea-version> tag. |
| `pluginUntilBuild` | `until-build` attribute of the <idea-version> tag. |
| `platformType` | The type of IDE distribution. |
| `platformVersion` |  |
| `platformDownloadSources` |  |


## Sample Code

TODO

## Plugin Manifest File

TODO

## Continuous Integration

Unit tests
Detekt
verifyPlugin
intellij-plugin-verifier

## Release Flow

### Changelog

When delivering a new release, it is essential to let your audience know what the updated version offering is.
The best way of handling that is to attach the changelog.

The changelog is a curated list containing information of any new features, fixes, deprecations.
If provided, such list would be available in a couple of places: [CHANGELOG.md](./CHANGELOG.md) file,
[Releases page][releases], [What's new][marketplace-plugin-page] section in Marketplace's Plugin page
and inside of the Plugin Manager's item details. 
There are many different methods of handling the project's changelog. One of them, used in the current template project,
is the [Keep a Changelog][keep-a-changelog] approach.

# Publishing Plugin

Cannot find org.jetbrains.plugins.template. Note that you need to upload the plugin to the repository at least once manually (to specify options like the license, repository URL etc.). Follow the instructions: https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html

## Useful Links

- [IntelliJ Platform SDK DevGuide][docs]
- [IntelliJ SDK Code Samples][code-samples]
- [JetBrains Platform Slack][slack]
- [IntelliJ IDEA Open API and Plugin Development][forum]
- [Keep a Changelog][keep-a-changelog]
- [GitHub Actions][gh-actions]

[gh-template]: https://help.github.com/en/enterprise/2.20/user/github/creating-cloning-and-archiving-repositories/creating-a-repository-from-a-template
[gh-actions]: https://help.github.com/en/actions
[code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[gradle-intellij-plugin]: https://github.com/JetBrains/gradle-intellij-plugin
[releases]: https://github.com/JetBrains/intellij-plugin-template/releases
[marketplace-plugin-page]: https://plugins.jetbrains.com/plugin/0-TODO
[slack]: https://plugins.jetbrains.com/slack
[forum]: https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development
[keep-a-changelog]: https://keepachangelog.com
[detekt]: https://detekt.github.io/detekt
[download-ij]: https://www.jetbrains.com/idea/download
[gradle]: https://gradle.org
[gradle-kotlin-dsl]: https://docs.gradle.org/current/userguide/kotlin_dsl.html

[docs]: https://www.jetbrains.org/intellij/sdk/docs
[docs:intro]: https://www.jetbrains.org/intellij/sdk/docs/intro/intellij_platform.html
[docs:using-gradle]: https://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system.html
[docs:publishing]: https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html

[file:getting-started_use-this-template.png]: ./.github/readme/getting-started_use-this-template.png
[file:gradle.properties]: ./gradle.properties
[file:template_cleanup.yml]: ./.github/workflows/template-cleanup.yml
