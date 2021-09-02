package org.jetbrains.plugins.template

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

class MyPluginTest : BasePlatformTestCase() {

    fun `test created XML file`() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, psiFile.virtualFile))

        psiFile as XmlFile
        assertNotNull(psiFile.rootTag)

        psiFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    override fun getTestDataPath() = "src/test/testData/rename"

    fun `test renaming XML tag`() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }
}
