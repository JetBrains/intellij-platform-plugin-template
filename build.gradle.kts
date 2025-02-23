import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import java.net.URI

plugins {
    `maven-publish`
    id("java") // Java support
    alias(libs.plugins.kotlin) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}


group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// pawrequest custom github repo/dependency adder ASSUMES HTTPS://REPO_URL/.../VENDOR/ASSET
val thisArtifactID = providers.gradleProperty("pluginRepositoryUrl").get().substringAfterLast("/")
val thisVendorName = providers.gradleProperty("pluginRepositoryUrl").get().substringBeforeLast("/").substringAfterLast("/")
val theseCustomDependencies = providers.gradleProperty("customDependencies")
    .orNull // Returns null if the property is missing
    ?.split(",") // Split only if the property is present
    ?.filter { it.isNotBlank() } // Filter out empty strings
    ?: emptyList() // Provide an empty list if the property is missing

fun githubPackageUri(vendor: String = thisVendorName, artifactID: String = thisArtifactID): URI {
    return URI.create("https://maven.pkg.github.com/$vendor/$artifactID")
}

fun addRepoUri(repositoryHandler: RepositoryHandler, uri: URI) {
    repositoryHandler.maven {
        url = uri
        name = "GitHubPackages"

        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("PUBLISH_TOKEN")
        }
    }
}

fun addCustomRepos(repositoryHandler: RepositoryHandler) {
    println("Custom Repos: $theseCustomDependencies")
    for (dep in theseCustomDependencies) {
        println("dep: $theseCustomDependencies")

        val depVals = dep.split(" ")
        val repoUri = githubPackageUri(depVals[0], depVals[1])
        addRepoUri(repositoryHandler, repoUri)
    }
}



fun addCustomDependencies(dependencyHandler: DependencyHandler) {
    println("Custom Dependencies: $theseCustomDependencies")

    for (dep in theseCustomDependencies) {
        println("dep: $dep")

        val depVals = dep.split(" ")
        val imp = "${depVals[2]}:${depVals[1]}:${depVals[3]}"
        dependencyHandler.implementation(imp)
    }
}


fun addPublication(publicationContainer: PublicationContainer) {
    publicationContainer.create<MavenPublication>("mavenJava") {
        from(components["java"])
        groupId = providers.gradleProperty("pluginGroup").get()
        artifactId = thisArtifactID
        version = providers.gradleProperty("pluginVersion").get()
    }
}





// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure project's dependencies
repositories {
    mavenCentral()
    addCustomRepos(this)
    intellijPlatform {
        defaultRepositories()
    }
}


//// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    testImplementation(libs.junit)
    addCustomDependencies(this)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

//        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}




publishing {
    repositories {
        addRepoUri(this, githubPackageUri())
    }
    publications {
        addPublication(this)
    }
}


// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
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


        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}


