package com.example.aichat

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

private val MOCK_RESPONSES = listOf(
    "Конечно! Вот что я могу предложить по этому вопросу...",
    "Интересный вопрос. Давайте разберём по шагам:\n\n1. Сначала определите требования\n2. Выберите подходящий подход\n3. Реализуйте и протестируйте",
    "Рекомендую обратить внимание на документацию IntelliJ Platform SDK — там есть отличные примеры для вашего случая.",
    "Хорошая идея! Это можно реализовать через `ToolWindowFactory` + сервис на уровне проекта.",
    "Понял вас. Предлагаю использовать корутины Kotlin для асинхронных операций — это хорошо интегрируется с IntelliJ Platform.",
    "Это стандартная задача. В IntelliJ Platform есть готовые компоненты для этого — посмотрите на `ApplicationManager.getApplication().invokeLater {}`.",
    "Отличный вопрос! Если коротко: используйте `ReadAction` для чтения PSI-дерева и `WriteAction` для изменений.",
)

class ChatPanel(private val project: Project) {

    val component: JPanel = JBPanel<JBPanel<*>>(BorderLayout())

    private val chatPane = JTextPane().apply {
        isEditable = false
        isOpaque = true
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
    }

    private val inputField = JBTextField().apply {
        emptyText.text = "Введите сообщение и нажмите Enter..."
        font = font.deriveFont(13f)
    }

    private val sendButton = JButton("Отправить").apply {
        font = font.deriveFont(Font.BOLD, 13f)
    }

    private val statusLabel = JLabel("AI Chat Demo").apply {
        horizontalAlignment = SwingConstants.CENTER
        border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        font = font.deriveFont(Font.BOLD, 11f)
        foreground = JBColor.GRAY
    }

    init {
        val scrollPane = JBScrollPane(chatPane).apply {
            border = BorderFactory.createEmptyBorder()
        }

        val headerPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.LIGHT_GRAY)
            add(statusLabel, BorderLayout.CENTER)
        }

        val inputPanel = JBPanel<JBPanel<*>>(BorderLayout(6, 0)).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            )
            add(inputField, BorderLayout.CENTER)
            add(sendButton, BorderLayout.EAST)
        }

        component.add(headerPanel, BorderLayout.NORTH)
        component.add(scrollPane, BorderLayout.CENTER)
        component.add(inputPanel, BorderLayout.SOUTH)

        sendButton.addActionListener { sendMessage() }
        inputField.addActionListener { sendMessage() }

        appendMessage("AI", "Привет! Я AI-помощник. Задайте любой вопрос о разработке плагинов для IntelliJ IDEA.")
    }

    private fun sendMessage() {
        val text = inputField.text.trim()
        if (text.isEmpty()) return

        inputField.text = ""
        inputField.isEnabled = false
        sendButton.isEnabled = false
        appendMessage("Вы", text)
        appendMessage("AI", "печатает...")
        statusLabel.text = "AI печатает..."

        Timer(900) {
            replaceLastTypingIndicator(MOCK_RESPONSES.random())
            statusLabel.text = "AI Chat Demo"
            inputField.isEnabled = true
            sendButton.isEnabled = true
            inputField.requestFocus()
        }.apply { isRepeats = false; start() }
    }

    private fun appendMessage(sender: String, text: String) {
        val doc = chatPane.styledDocument

        val senderStyle = SimpleAttributeSet().apply {
            StyleConstants.setBold(this, true)
            StyleConstants.setForeground(this, if (sender == "AI") JBColor.BLUE else JBColor.foreground())
        }
        val textStyle = SimpleAttributeSet().apply {
            StyleConstants.setForeground(this, JBColor.foreground())
        }
        val spacerStyle = SimpleAttributeSet()

        if (doc.length > 0) {
            doc.insertString(doc.length, "\n\n", spacerStyle)
        }
        doc.insertString(doc.length, "$sender\n", senderStyle)
        doc.insertString(doc.length, text, textStyle)

        chatPane.caretPosition = doc.length
    }

    private fun replaceLastTypingIndicator(newText: String) {
        val doc = chatPane.styledDocument
        val fullText = doc.getText(0, doc.length)
        val lastAiIdx = fullText.lastIndexOf("AI\nпечатает...")
        if (lastAiIdx >= 0) {
            doc.remove(lastAiIdx, doc.length - lastAiIdx)
            val senderStyle = SimpleAttributeSet().apply {
                StyleConstants.setBold(this, true)
                StyleConstants.setForeground(this, JBColor.BLUE)
            }
            val textStyle = SimpleAttributeSet().apply {
                StyleConstants.setForeground(this, JBColor.foreground())
            }
            doc.insertString(doc.length, "AI\n", senderStyle)
            doc.insertString(doc.length, newText, textStyle)
            chatPane.caretPosition = doc.length
        }
    }
}
