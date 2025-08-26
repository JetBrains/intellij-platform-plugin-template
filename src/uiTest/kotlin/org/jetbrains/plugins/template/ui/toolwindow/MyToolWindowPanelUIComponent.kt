package org.jetbrains.plugins.template.ui.toolwindow

import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.*
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.xQuery
import com.intellij.ide.starter.report.AllureHelper.step
import org.intellij.lang.annotations.Language
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * UI component definitions for testing the MyToolWindow custom tool window.
 *
 * This file demonstrates how to create custom UI component definitions for
 * IntelliJ Platform plugin UI testing using the Driver SDK.
 *
 * ## Architecture:
 * - Extension functions on `Finder` provide DSL-style access to UI components
 * - Custom component classes (like `MyToolWindowPanel`) encapsulate complex UI structures
 * - Helper functions simplify common UI element queries
 *
 * ## Key Concepts:
 * 1. **Finder**: The main entry point for finding UI components in the IDE
 * 2. **XPath Queries**: Used to locate specific UI elements by their properties
 * 3. **Component Classes**: Custom classes that represent your plugin's UI components
 * 4. **Allure Steps**: Optional test reporting integration for better test visibility
 *
 * @see [Driver SDK Documentation](https://github.com/JetBrains/intellij-ide-starter)
 */

/**
 * Finds a JLabel component using an XPath query.
 *
 * This helper function show how you can extend the finding  components.
 * In this example we find a label component in the UI.
 * If no XPath is provided, it finds any JLabel in the current context.
 *
 * ## Usage Examples:
 * ```kotlin
 * // Find a specific label by text
 * jlabel("//div[@visible_text='My Label Text']")
 *
 * // Find any JLabel in context
 * jlabel()
 * ```
 *
 * @param xpath Optional XPath query to locate a specific label.
 *              The @Language annotation enables IDE support for XPath syntax.
 * @return A JLabelUiComponent for further interaction
 */
fun Finder.jlabel(@Language("xpath") xpath: String? = null) =
    x(xpath ?: xQuery { byType(javax.swing.JLabel::class.java) }, JLabelUiComponent::class.java)

/**
 * Opens the MyToolWindow tool window from the left toolbar.
 *
 * This function navigates to the tool window stripe button and opens it.
 * It's a prerequisite for interacting with the tool window contents.
 *
 * ## How it works:
 * 1. Finds the left toolbar component
 * 2. Locates the MyToolWindow stripe button by its accessible name
 * 3. Opens the tool window if it's not already visible
 *
 * ## Customization:
 * - Replace "MyToolWindow" with your tool window's ID
 * - Adjust the toolbar location if your tool window is on a different side
 */
fun Finder.showMyToolWindow() = with(x(ToolWindowLeftToolbarUi::class.java) { byClass("ToolWindowLeftToolbar") }) {
    val myToolWindowButton = x(StripeButtonUi::class.java) { byAccessibleName("MyToolWindow") }
    myToolWindowButton.open()
}

/**
 * Provides a scoped context for interacting with the MyToolWindow panel.
 *
 * This is the main entry point for tool window UI testing. It:
 * 1. Opens the tool window if needed
 * 2. Waits for the panel to be available
 * 3. Executes test code within the panel's context
 *
 * ## Usage in Tests:
 * ```kotlin
 * ideFrame {
 *     myToolWindow {
 *         // Your test interactions here
 *         button("Click Me").click()
 *         jlabel("//div[@text='Result']").shouldBe { present() }
 *     }
 * }
 * ```
 *
 * ## Parameters:
 * @param timeout Maximum time to wait for the tool window to appear (default: 20 seconds)
 * @param block The test code to execute within the tool window context
 *
 * ## Allure Integration:
 * The `step` wrapper creates a named step in Allure test reports,
 * making it easier to understand test flow and debug failures.
 */
fun Finder.myToolWindow(timeout: Duration = 20.seconds, block: MyToolWindowPanel.() -> Unit) {
    step("My Tool Window Panel") {
        showMyToolWindow()

        // Wait for the tool window's internal decorator to appear
        // InternalDecoratorImpl is the container class for tool window content
        x("//div[@class='InternalDecoratorImpl']", MyToolWindowPanel::class.java)
            .waitFound(timeout)
            .apply(block)
    }
}

/**
 * Custom UI component class representing the MyToolWindow panel.
 *
 * This class serves as a container for tool-window-specific UI interactions.
 * Extend this class to add methods for interacting with your tool window's components.
 *
 * ## Adding Custom Methods:
 * ```kotlin
 * class MyToolWindowPanel(data: ComponentData) : UiComponent(data) {
 *     // Find a button by its text
 *     fun button(text: String) = x("//div[@defaultbutton and @text='$text']")
 *
 *     // Find an input field
 *     fun inputField() = x("//div[@class='JTextField']")
 *
 *     // Custom interaction method
 *     fun performSearch(query: String) {
 *         inputField().text = query
 *         button("Search").click()
 *     }
 * }
 * ```
 *
 * ## Best Practices:
 * - Keep methods focused on single UI elements or actions
 * - Use descriptive names that match your plugin's terminology
 * - Document complex XPath queries or interaction patterns
 * - Consider adding validation methods for common assertions
 */
class MyToolWindowPanel(data: ComponentData) : UiComponent(data) 


