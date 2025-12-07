package com.example.persona.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 简单的Markdown文本渲染组件
 * 支持：**粗体**、*斜体*、`代码`、# 标题等基础格式
 */
@Composable
fun MarkdownMessageText(
    markdown: String,
    modifier: Modifier = Modifier,
    isFromUser: Boolean = false
) {
    val color = if (isFromUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 简单的Markdown解析和渲染
    val lines = markdown.split("\n")
    Column(modifier = modifier) {
        lines.forEach { line ->
            when {
                // 代码块
                line.trim().startsWith("```") -> {
                    // 跳过代码块标记
                }
                // 标题
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.titleLarge,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // 列表项
                line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                    Row {
                        Text("• ", color = color)
                        Text(
                            text = line.trim().removePrefix("- ").removePrefix("* "),
                            color = color,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                // 普通文本（处理行内格式）
                else -> {
                    if (line.isNotBlank()) {
                        Text(
                            text = parseInlineMarkdown(line),
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * 解析行内Markdown格式（粗体、斜体、代码）
 * 改进版本：正确处理嵌套和多个格式标记
 */
private fun parseInlineMarkdown(text: String) = buildAnnotatedString {
    var i = 0
    val codeColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)

    while (i < text.length) {
        when {
            // `代码` - 优先处理，避免被其他规则干扰
            i < text.length && text[i] == '`' -> {
                val end = findClosingMark(text, i + 1, '`')
                if (end != -1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeColor.copy(alpha = 0.1f),
                            color = codeColor
                        )
                    ) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // **粗体** - 检查是否是双星号
            i + 1 < text.length && text[i] == '*' && text[i + 1] == '*' -> {
                val end = findClosingBold(text, i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // *斜体* - 单星号
            i < text.length && text[i] == '*' -> {
                val end = findClosingItalic(text, i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}

/**
 * 查找闭合的单字符标记（如 ` ）
 */
private fun findClosingMark(text: String, start: Int, mark: Char): Int {
    for (i in start until text.length) {
        if (text[i] == mark) {
            return i
        }
    }
    return -1
}

/**
 * 查找闭合的粗体标记 **
 */
private fun findClosingBold(text: String, start: Int): Int {
    var i = start
    while (i + 1 < text.length) {
        if (text[i] == '*' && text[i + 1] == '*') {
            return i
        }
        i++
    }
    return -1
}

/**
 * 查找闭合的斜体标记 *（确保不是 **）
 */
private fun findClosingItalic(text: String, start: Int): Int {
    var i = start
    while (i < text.length) {
        if (text[i] == '*') {
            // 确保不是双星号的一部分
            val prevIsBold = i > 0 && text[i - 1] == '*'
            val nextIsBold = i + 1 < text.length && text[i + 1] == '*'
            if (!prevIsBold && !nextIsBold) {
                return i
            }
        }
        i++
    }
    return -1
}

/**
 * 打字机效果的流式显示组件
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "正在输入$dots",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 流式文本显示（带打字机效果）
 * 在流式输出时显示原始文本（避免不完整的Markdown显示格式符号）
 * 完成后再渲染Markdown格式
 */
@Composable
fun StreamingMarkdownText(
    text: String,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
    isFromUser: Boolean = false
) {
    val color = if (isFromUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = modifier) {
        if (text.isNotEmpty()) {
            if (isComplete) {
                // 流式输出完成，使用 Markdown 渲染
                MarkdownMessageText(
                    markdown = text,
                    isFromUser = isFromUser
                )
            } else {
                // 流式输出中，使用普通文本显示（避免显示不完整的 Markdown 格式符号）
                Text(
                    text = text,
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                // 显示光标
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "▊",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

