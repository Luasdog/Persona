package com.example.persona.ui.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight

object MarkdownUtils {

    fun toAnnotatedString(markdown: String): AnnotatedString {
        // 简单实现：支持 **bold**, *italic*, `inline code`, [text](url), ```code blocks``` 和 blockquote
        // 这不是完整解析器，但对常见用例有效
        val sb = buildAnnotatedString {
            var i = 0
            while (i < markdown.length) {
                when {
                    markdown.startsWith("```", i) -> {
                        val end = markdown.indexOf("```", i + 3)
                        val code = if (end >= 0) markdown.substring(i + 3, end) else markdown.substring(i + 3)
                        withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)) {
                            append(code)
                        }
                        i = if (end >= 0) end + 3 else markdown.length
                    }
                    markdown.startsWith("**", i) -> {
                        val end = markdown.indexOf("**", i + 2)
                        if (end >= 0) {
                            val content = markdown.substring(i + 2, end)
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(content) }
                            i = end + 2
                        } else {
                            append(markdown[i]); i++
                        }
                    }
                    markdown.startsWith("*", i) -> {
                        val end = markdown.indexOf("*", i + 1)
                        if (end >= 0) {
                            val content = markdown.substring(i + 1, end)
                            withStyle(style = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) { append(content) }
                            i = end + 1
                        } else { append(markdown[i]); i++ }
                    }
                    markdown.startsWith("`", i) -> {
                        val end = markdown.indexOf("`", i + 1)
                        if (end >= 0) {
                            val content = markdown.substring(i + 1, end)
                            withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace)) { append(content) }
                            i = end + 1
                        } else { append(markdown[i]); i++ }
                    }
                    markdown.startsWith("[", i) -> {
                        val endText = markdown.indexOf("](", i + 1)
                        val endUrl = if (endText >= 0) markdown.indexOf(")", endText + 2) else -1
                        if (endText >= 0 && endUrl >= 0) {
                            val text = markdown.substring(i + 1, endText)
                            val url = markdown.substring(endText + 2, endUrl)
                            // add annotation for link
                            val start = this.length
                            withStyle(style = SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF1E88E5), textDecoration = TextDecoration.Underline)) {
                                append(text)
                            }
                            addStringAnnotation(tag = "LINK", annotation = url, start = start, end = start + text.length)
                            i = endUrl + 1
                        } else { append(markdown[i]); i++ }
                    }
                    markdown.startsWith(">", i) -> {
                        // blockquote: consume until line end
                        val endln = markdown.indexOf('\n', i)
                        val content = if (endln >= 0) markdown.substring(i + 1, endln) else markdown.substring(i + 1)
                        withStyle(style = SpanStyle(color = androidx.compose.ui.graphics.Color.Gray)) {
                            append("${content.trim()}\n")
                        }
                        i = if (endln >= 0) endln + 1 else markdown.length
                    }
                    else -> {
                        append(markdown[i])
                        i++
                    }
                }
            }
        }
        return sb
    }
}

