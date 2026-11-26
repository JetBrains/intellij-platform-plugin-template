package org.jetbrains.plugins.template

import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.toolWindow
import com.intellij.driver.sdk.ui.components.elements.JLabelUiComponent
import com.intellij.driver.sdk.ui.components.elements.button
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes

class MyToolWindowTest {

    @TempDir
    lateinit var projectDir: Path

    @Test
    fun testShuffleDisplaysRandomNumber() {
        Starter.newContext(
            testName = "myToolWindowShuffle",
            TestCase(IdeInfo.IdeaUltimate, LocalProjectInfo(projectDir))
                .withVersion(checkNotNull(System.getProperty("ide.test.version")))
        ).apply {
            val pluginPath = checkNotNull(System.getProperty("path.to.build.plugin"))
            PluginConfigurator(this).installPluginFromPath(Path.of(pluginPath))
        }.runIdeWithDriver().useDriverAndCloseIde {
            waitForIndicators(1.minutes)
            invokeAction("ActivateMyToolWindowToolWindow")

            ideFrame {
                toolWindow("MyToolWindow") {
                    val randomLabel = x(JLabelUiComponent::class.java) {
                        contains(byVisibleText("The random number is:"))
                    }
                    randomLabel.shouldBe("The initial random number should be unknown") {
                        getText() == "The random number is: ?"
                    }

                    button("Shuffle").click()

                    randomLabel.shouldBe("Shuffle should display a number from 1 to 100") {
                        getText().matches(Regex("The random number is: ([1-9][0-9]?|100)"))
                    }
                }
            }
        }
    }
}