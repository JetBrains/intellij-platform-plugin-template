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

The only thing that you have to do is clicking the **Use this template** button.

![Use this template][file:getting-started_use-this-template.png]

After creating your blank project from the template, there will be the [Template Cleanup][file:template_cleanup.yml]
workflow triggered to override or remove template-specific configuration, like plugin name, current changelog, etc.

As a last step, it is required to manually specify the `pluginName` and `pluginGroup` in the `gradle.properties` file.

## Gradle

TODO

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

- [IntelliJ Platform SDK DevGuide][sdk-docs]
- [IntelliJ SDK Code Samples][code-samples]
- [JetBrains Platform Slack][slack]
- [IntelliJ IDEA Open API and Plugin Development][forum]
- [Keep a Changelog][keep-a-changelog]
- [GitHub Actions][gh-actions]

[gh-template]: https://help.github.com/en/enterprise/2.20/user/github/creating-cloning-and-archiving-repositories/creating-a-repository-from-a-template
[gh-actions]: https://help.github.com/en/actions
[sdk-docs]: https://www.jetbrains.org/intellij/sdk/docs
[code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples
[releases]: https://github.com/JetBrains/intellij-plugin-template/releases
[marketplace-plugin-page]: https://plugins.jetbrains.com/plugin/0-TODO
[slack]: https://plugins.jetbrains.com/slack
[forum]: https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development
[keep-a-changelog]: https://keepachangelog.com

[file:getting-started_use-this-template.png]: ./.github/readme/getting-started_use-this-template.png
[file:template_cleanup.yml]: ./.github/workflows/template-cleanup.yml
