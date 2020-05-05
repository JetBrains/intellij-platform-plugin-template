import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.4.18"
    // detekt linter - read more: https://detekt.github.io/detekt/kotlindsl.html
    id("io.gitlab.arturbosch.detekt") version "1.8.0"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val ideaVersion: String by project
val ideaType: String by project
val sources: String by project

group = pluginGroup
version = pluginVersion

// Set the compatibility versions to 1.8
tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}
listOf("compileKotlin", "compileTestKotlin").forEach {
    tasks.getByName<KotlinCompile>(it) {
        kotlinOptions.jvmTarget = "1.8"
    }
}

// Configure project's dependencies
repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.8.0")
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
}

tasks {
    patchPluginXml {
        version(pluginVersion)
        sinceBuild(ideaVersion)
//        changeNotes("")
    }

//    publishPlugin {
//        token("ssdfhasdfASDaq23jhnasdkjh")
//        channels("nightly")
//    }
}
