import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.4.18"
}

val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val ideaVersion: String by project
val ideaType: String by project
val sources: String by project

group = pluginGroup
version = pluginVersion

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}
listOf("compileKotlin", "compileTestKotlin").forEach {
    tasks.getByName<KotlinCompile>(it) {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

// https://github.com/JetBrains/gradle-intellij-plugin

intellij {
    pluginName = pluginName
    version = ideaVersion
    type = ideaType
    updateSinceUntilBuild = false
    downloadSources = sources.toBoolean()
    setPlugins("java")
}

tasks {
    patchPluginXml {
        version(pluginVersion)
//        changeNotes("")
//    sinceBuild sinceBuild
    }

//    publishPlugin {
//        token("ssdfhasdfASDaq23jhnasdkjh")
//        channels("nightly")
//    }
}
