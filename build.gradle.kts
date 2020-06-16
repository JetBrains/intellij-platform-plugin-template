import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.closure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.4.21"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "0.3.0"
    // detekt linter - read more: https://detekt.github.io/detekt/kotlindsl.html
    id("io.gitlab.arturbosch.detekt") version "1.10.0-RC1"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val ideaVersion: String by project
val ideaType: String by project
val sources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.10.0-RC1")
}

// Configure gradle-intellij-plugin plugin. Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = pluginName
    version = ideaVersion
    type = ideaType
    updateSinceUntilBuild = true
    downloadSources = sources.toBoolean()
    setPlugins("java")
}

// Configure detekt plugin. Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

tasks {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    withType<Detekt> {
        jvmTarget = "1.8"
    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)
        changeNotes(closure {
            changelog.getUnreleased().toHTML()
        })
    }
}
