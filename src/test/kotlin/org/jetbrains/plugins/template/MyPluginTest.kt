package org.jetbrains.plugins.template

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

@TestDataPath("$PROJECT_ROOT/src/test/testData/rename")
class MyPluginTest : BasePlatformTestCase() {

    fun `test created XML file`() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    override fun getTestDataPath() = "src/test/testData/rename"

    fun `test renaming XML tag`() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }
}
