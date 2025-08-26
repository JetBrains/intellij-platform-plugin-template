package org.jetbrains.plugins.template.ui


import com.intellij.driver.sdk.ui.components.button
import com.intellij.driver.sdk.ui.components.ideFrame
import com.intellij.driver.sdk.ui.components.welcomeScreen
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.wait
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.BackgroundRun
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.config.UseLatestDownloadedIdeBuild
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.ui.toolwindow.jlabel
import org.jetbrains.plugins.template.ui.toolwindow.myToolWindow
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

/**
 * Example UI test demonstrating how to test IntelliJ Platform plugins using the IDE Starter framework.
 *
 * This test class showcases:
 * - Setting up and tearing down an IDE instance for testing
 * - Creating a new project through the IDE's welcome screen
 * - Interacting with custom plugin UI components (tool windows)
 * - Using the Driver SDK for UI automation
 *
 * ## Test Lifecycle:
 * 1. `@BeforeEach` - Starts a new IDE instance with the plugin installed
 * 2. Test method executes - Interacts with IDE UI components
 * 3. `@AfterEach` - Closes the IDE instance
 * 4. `@AfterAll` - Cleans up test artifacts
 *
 * ## Key Annotations:
 * - `@Tag("ui")` - Marks this as a UI test (excluded from regular test runs)
 * - `@TestInstance(PER_METHOD)` - New instance for each test method
 * - `@ExtendWith(UseLatestDownloadedIdeBuild)` - Uses the IDE version from gradle.properties
 *
 * @see Setup for test configuration details
 * @see MyToolWindowPanelUIComponent for custom UI component definitions
 */
@Tag("ui")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(UseLatestDownloadedIdeBuild::class)
class MyProjectUITest {

    companion object {
        // Generate a unique folder name for the test project to avoid conflicts
        val testProjectFolder = "my-test-project-${System.currentTimeMillis()}"

        /**
         * Cleanup method that runs after all tests in this class.
         * Removes the test project folder created during testing to keep the system clean.
         */
        @JvmStatic
        @AfterAll
        fun cleanUpTestFolder() {
            val projectPath = Paths.get(System.getProperty("user.home"), "IdeaProjects", testProjectFolder)
            val projectFile = projectPath.toFile()
            if (projectFile.exists()) {
                projectFile.deleteRecursively()
                println("Successfully deleted test folder: $projectPath")
            } else {
                println("Test folder does not exist, skipping cleanup: $projectPath")
            }
        }
    }

    /**
     * The IDE instance running in the background.
     * Initialized in @BeforeEach and closed in @AfterEach.
     */
    private lateinit var run: BackgroundRun

    /**
     * Sets up the test environment before each test method.
     *
     * This method:
     * 1. Creates a test context using the Setup class
     * 2. Starts an IDE instance with the plugin installed
     * 3. Prepares the IDE for UI interaction through the Driver SDK
     */
    @BeforeEach
    fun initContext() {
        println("Initializing test context")
        println("Test project will be created as: $testProjectFolder")
        run = Setup.setupTestContext("MyProjectUITest").runIdeWithDriver()
    }

    /**
     * Tears down the test environment after each test method.
     *
     * Safely closes the IDE instance if it was successfully started.
     * The initialization check prevents errors if the setup failed.
     */
    @AfterEach
    fun closeIde() {
        if (::run.isInitialized) {
            println("Closing IDE")
            run.closeIdeAndWait()
        } else {
            println("IDE was not started, skipping close")
        }
    }

    /**
     * Example test that demonstrates creating a project and interacting with a custom tool window.
     *
     * ## Test Flow:
     * 1. Opens the IDE welcome screen
     * 2. Creates a new Java project
     * 3. Waits for IDE indexing to complete
     * 4. Interacts with the plugin's custom tool window
     * 5. Verifies UI components are present and functional
     *
     * ## How to Write Your Own Tests:
     * - Use `welcomeScreen { }` to interact with the welcome dialog
     * - Use `ideFrame { }` to interact with the main IDE window
     * - Use `driver.waitForIndicators()` to wait for background tasks
     * - Create custom UI component definitions (see MyToolWindowPanelUIComponent)
     * - Use `shouldBe { }` for assertions on UI elements
     */
    @Test
    fun myProjectWithToolWindowTest() {
        println("Starting my project test")

        run.driver.withContext {
            // === Step 1: Create a new project from the welcome screen ===
            welcomeScreen {
                println("Creating the new project from welcome screen")
                createNewProjectButton.click()

                newProjectDialog {
                    // Wait for the dialog to fully load
                    wait(1.seconds)

                    // Select project type - adjust based on your needs
                    chooseProjectType("Java")

                    // Verify UI state
                    sampleCodeLabel.isEnabled()

                    // Set project name to our test folder
                    setProjectName(testProjectFolder)
                    println("Set project name to: $testProjectFolder")

                    // Create the project
                    createButton.click()
                    println("Clicked create button")
                }
            }

            // === Step 2: Interact with the IDE and your plugin ===
            ideFrame {
                // Wait for indexing and other background tasks to complete
                driver.waitForIndicators(180.seconds)

                // Example: Interact with the custom tool window from this template
                // Replace this with your own plugin's UI components
                myToolWindow {
                    // Find and verify a label component
                    jlabel("//div[@visible_text='${MyBundle.message("randomLabel", "?")}']").shouldBe {
                        present()
                    }

                    // Find and click a button
                    button(MyBundle.message("shuffle")).shouldBe {
                        present() && isEnabled()
                    }.click()
                }

                // Add more interactions with your plugin's UI here
                // Example patterns:
                // - Open menus: actionMenu("Tools").click()
                // - Open dialogs: invokeAction("YourAction")
                // - Verify notifications: notification { text.contains("Expected message") }
            }
        }
    }
}