import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
    idea // IntelliJ IDEA support
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure project's dependencies
repositories {
    mavenCentral()
    maven("https://cache-redirector.jetbrains.com/packages.jetbrains.team/maven/p/ij/intellij-ide-starter")

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}




sourceSets {
    create("uiTest", Action<SourceSet> {
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    })
}

// Configure IntelliJ IDEA to recognize uiTest as test sources
idea {
    module {
        testSources.from(sourceSets["uiTest"].kotlin.srcDirs)
        testResources.from(sourceSets["uiTest"].resources.srcDirs)
    }
}


val uiTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val uiTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        val platformVersion = providers.gradleProperty("platformVersion")
        val platformType = providers.gradleProperty("platformType")
        create(platformType, platformVersion) {
            useInstaller = false
        }
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        // Module Dependencies. Uses `platformBundledModules` property from the gradle.properties file for bundled IntelliJ Platform modules.
        bundledModules(providers.gradleProperty("platformBundledModules").map { it.split(',') })

        // Test framework dependencies
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)

        // UI Test framework dependencies
        testFramework(TestFrameworkType.Starter, configurationName = "uiTestImplementation")
        testFramework(TestFrameworkType.JUnit5, configurationName = "uiTestImplementation")
    }

    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)

    // UI Test dependencies
    uiTestImplementation("org.kodein.di:kodein-di-jvm:7.26.1")
    uiTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // JUnit 5 is required for UI tests
    uiTestImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    uiTestRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }

    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion")
            .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }


    runIde {
        systemProperties(
            "ide.native.launcher" to true,
            "ide.show.tips.on.startup.default.value" to false,
            "jb.consents.confirmation.enabled" to false
        )
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    test {
        useJUnitPlatform {
            excludeTags("ui")
        }

        // Enable process-level parallelism (safer than method-level parallelism)
        maxParallelForks = minOf(Runtime.getRuntime().availableProcessors() / 2, 3)

        // Keep JUnit execution sequential within each process for stability
        systemProperty("junit.jupiter.execution.parallel.enabled", "false")


        systemProperty("idea.home.path", prepareTestSandbox.get().getDestinationDir().parentFile.absolutePath)
        systemProperty("idea.force.use.core.classloader", "true")

        jvmArgs = listOf(
            "-Didea.trust.all.projects=true",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.desktop/javax.swing=ALL-UNNAMED"
        )

        dependsOn("buildPlugin")
    }

    register<Test>("uiTest") {
        description = "Runs only the UI tests that start the IDE"
        group = "verification"

        testClassesDirs = sourceSets["uiTest"].output.classesDirs
        classpath = sourceSets["uiTest"].runtimeClasspath

        useJUnitPlatform {
            includeTags("ui")
        }

        // UI tests should run sequentially (not in parallel) to avoid conflicts
        maxParallelForks = 1

        // Increase memory for UI tests
        minHeapSize = "1g"
        maxHeapSize = "4g"

        systemProperty("path.to.build.plugin", buildPlugin.get().archiveFile.get().asFile.absolutePath)
        systemProperty("idea.home.path", prepareTestSandbox.get().getDestinationDir().parentFile.absolutePath)
        systemProperty(
            "allure.results.directory", project.layout.buildDirectory.get().asFile.absolutePath + "/allure-results"
        )
        systemProperty("uiPlatformBuildVersion", providers.gradleProperty("uiPlatformBuildVersion").get())

        // Disable IntelliJ test listener that conflicts with standard JUnit
        systemProperty("idea.test.cyclic.buffer.size", "0")

        // Add required JVM arguments
        jvmArgumentProviders += CommandLineArgumentProvider {
            mutableListOf(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.desktop/javax.swing=ALL-UNNAMED"
            )
        }

        dependsOn(buildPlugin)
    }
}


