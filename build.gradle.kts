import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testUiRuntimeOnly("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2026.2.5")
        testFramework(TestFrameworkType.Platform)
    }
}

tasks.named<Test>("testUi") {
    systemProperty("ui.test.screenshots.path", layout.buildDirectory.dir("reports/ui-test-screenshots").get().asFile.absolutePath)

    failOnNoDiscoveredTests = true
    filter {
        isFailOnNoMatchingTests = true
    }
    reports {
        html.required = true
        junitXml.required = true
    }
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
