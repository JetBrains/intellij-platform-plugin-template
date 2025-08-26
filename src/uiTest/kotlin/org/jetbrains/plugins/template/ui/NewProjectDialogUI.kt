package org.jetbrains.plugins.template.ui

import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.checkBox
import com.intellij.driver.sdk.ui.components.textField
import com.intellij.driver.sdk.ui.pasteText
import com.intellij.driver.sdk.ui.ui

/**
 * This code is  taken from the new version of: com.intellij.driver.sdk.ui.components.common.dialogs
 * Available from version 2025.1 - so it can be remove once the pluginSinceBuild is increased.
 */

fun Finder.newProjectDialog(action: NewProjectDialogUI.() -> Unit) {
    x("//div[@title='New Project']", NewProjectDialogUI::class.java).action()
}

open class NewProjectDialogUI(data: ComponentData) : UiComponent(data) {
    fun setProjectName(text: String) {
        nameTextField.doubleClick()
        keyboard {
            backspace()
            driver.ui.pasteText(text)
        }
    }

    fun chooseProjectType(projectType: String) {
        projectTypeList.waitOneText(projectType).click()
    }

    val nameTextField = textField("//div[@accessiblename='Name:' and @class='JBTextField']")
    open val createButton = x("//div[@text='Create']")
    private val projectTypeList = x("//div[@class='JBList']")
    val sampleCodeLabel = checkBox { byText("Add sample code") }
}