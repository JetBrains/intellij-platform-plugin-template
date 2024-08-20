package org.jetbrains.plugins.template

import com.intellij.facet.FacetManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findOrCreateDirectory
import com.intellij.psi.PsiFile
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.math.max


class AndroidInstrumentationTestGenerator : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // 현재 보고 있는 파일 가져오기
        val psiFile: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)
        psiFile ?: return run {
            println("No file selected")
        }

        val (classPath, className) = getFileInfo(psiFile)
        val (currentModule, androidModule) = getModuleInfo(psiFile)
        val newClassPath = androidModule + classPath.substring(currentModule.length)

        // 패널 생성
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // 첫 번째 입력 필드 (기본값 설정)
        val label1 = JLabel("Enter your class path(from: ${currentModule}):")
        val textField1 = JTextField("${newClassPath}/$className", 20) // 기본값 설정
        panel.add(label1)
        panel.add(textField1)

        // 입력 폼 띄우기
        val result = JOptionPane.showConfirmDialog(null, panel, "Class Path", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)

        // OK 버튼이 눌렸을 때 입력 처리
        if (result == JOptionPane.OK_OPTION) {
            val path = textField1.text
            val fileContent = createFileContent(psiFile)
            createKotlinFile(psiFile, path, className, fileContent)
            println("Path: $path")
        } else {
            println("User canceled the input")
        }
    }

    private fun createKotlinFile(psiFile: PsiFile, classPath: String, fileName: String, fileContent: String) {
        try {
            // 현재 프로젝트의 기본 디렉토리 가져오기
            var baseDir = LocalFileSystem.getInstance().findFileByPath(psiFile.project.basePath.orEmpty()) ?: psiFile.project.baseDir

            var newFile: VirtualFile? = null
            WriteCommandAction.runWriteCommandAction(psiFile.project) {
                baseDir = baseDir.findOrCreateDirectory(
                    classPath.substring(
                        0,
                        classPath.lastIndexOf("/")
                    )
                )

                // 파일을 생성할 경로 설정
                newFile = baseDir.createChildData(
                    this,
                    fileName.takeIf { it.endsWith(".kt") } ?: "$fileName.kt"
                )

                // 파일에 내용 쓰기
                VfsUtil.saveText(newFile!!, fileContent)

                // 파일 시스템 갱신
                VirtualFileManager.getInstance().syncRefresh()

                // 생성된 파일을 열기
                FileEditorManager.getInstance(psiFile.project).openFile(newFile!!, true)
            }
        } catch (ex: Exception) {
            Messages.showErrorDialog("Failed to create file: ${ex.message}", "Error")
        }

    }

    private fun getFileInfo(psiFile: PsiFile): Pair<String, String> {
        val packagePath = psiFile.virtualFile.path.run {
            substring(psiFile.project.basePath!!.length + 1)
        }.run {
            val prefix = substring(0, indexOf("/src/") + "/src/".length)
            val suffix = substring(prefix.length).run { substring(indexOf("/") + 1) }
            "${prefix}androidTest/$suffix"
        }.run {
            takeIf { !endsWith(".kt") } ?: substring(0, lastIndexOf("/"))
        }
        val testFileName = psiFile.name.run {
            if (endsWith(".kt")) {
                substring(0, length - 3) + "Test"
            } else {
                this + "Test"
            }
        }
        return packagePath to testFileName
    }

    private fun getModuleInfo(psiFile: PsiFile): Pair<String, String> {
        val currentModule = psiFile.virtualFile.path.run {
            substring(psiFile.project.basePath!!.length + 1, indexOf("/src"))
        }
        val androidModule = ModuleManager.getInstance(psiFile.project).modules.find {
            FacetManager.getInstance(it).allFacets.any { facts ->
                facts.type.toString().contains("AndroidFacetType")
            } && hasMainSourceSet(it)
        }?.run {
            name.substring(max(0, name.indexOf(".") + 1)).run {
                takeIf { !it.contains(".") } ?: substring(0, indexOf("."))
            }
        } ?: currentModule
        return currentModule to androidModule
    }

    private fun createFileContent(psiFile: PsiFile): String {
        val (classPath, className) = getFileInfo(psiFile)
        return """
            package ${classPath.asPackage()}

            @Suppress("NonAsciiCharacters", "TestFunctionName")
            class ${className.replace(".kt", "")} {
                
            }
        """.trimIndent()
    }

    private fun String.asPackage() = substring(indexOf("/src/") + 1).run {
        if (contains("/kotlin/")) {
            substring(indexOf("kotlin/") + "kotlin/".length)
        } else {
            substring(indexOf("java/") + "java/".length)
        }
    }.replace("/", ".")

    private fun hasMainSourceSet(module: Module): Boolean {
        // 모듈의 소스 루트 가져오기
        val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots

        // sourceRoots에서 "main" 디렉토리를 포함하는지 확인
        return sourceRoots.any { sourceRoot ->
            val path = sourceRoot.path
            path.contains("/src/main/java") || path.contains("/src/main/kotlin")
        }
    }
}