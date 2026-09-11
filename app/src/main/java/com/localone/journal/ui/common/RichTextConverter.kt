package com.localone.journal.ui.common

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat

object RichTextConverter {

    /**
     * 判断文本是否包含 HTML 标签
     */
    fun isHtml(text: String): Boolean {
        return text.contains("<p>") ||
                text.contains("<div>") ||
                text.contains("<b>") ||
                text.contains("<strong>") ||
                text.contains("<i>") ||
                text.contains("<em>") ||
                text.contains("<h") ||
                text.contains("<ul") ||
                text.contains("<ol") ||
                text.contains("<li") ||
                text.contains("<br")
    }

    /**
     * 将 HTML (例如 Aztec 生成的内容) 转换为 Compose AnnotatedString，
     * 自动保留粗体、斜体、下划线、删除线等样式，同时将待办任务转换为 Unicode 符号。
     */
    fun htmlToAnnotatedString(html: String): AnnotatedString {
        if (html.isBlank()) return AnnotatedString("")

        // 规范化待办清单标签，避免渲染为无序列表点
        val processedHtml = html
            .replace(Regex("<li[^>]*data-checked=\"true\"[^>]*>"), "☑ ")
            .replace(Regex("<li[^>]*data-checked=\"false\"[^>]*>"), "☐ ")
            .replace("<li class=\"task-list-item checked\">", "☑ ")
            .replace("<li class=\"task-list-item\">", "☐ ")

        val spanned = try {
            HtmlCompat.fromHtml(processedHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
        } catch (e: Exception) {
            return AnnotatedString(html)
        }

        return spannedToAnnotatedString(spanned)
    }

    private fun spannedToAnnotatedString(spanned: Spanned): AnnotatedString {
        // 去除末尾冗余换行
        val rawText = spanned.toString().trimEnd()
        val length = rawText.length

        return buildAnnotatedString {
            append(rawText)
            val spans = spanned.getSpans(0, length, Any::class.java)
            for (span in spans) {
                val start = spanned.getSpanStart(span).coerceIn(0, length)
                val end = spanned.getSpanEnd(span).coerceIn(0, length)
                if (start >= end) continue

                when (span) {
                    is StyleSpan -> {
                        when (span.style) {
                            Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                            Typeface.BOLD_ITALIC -> addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                                start,
                                end
                            )
                        }
                    }
                    is UnderlineSpan -> {
                        addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    }
                    is StrikethroughSpan -> {
                        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                    }
                    is RelativeSizeSpan -> {
                        addStyle(SpanStyle(fontSize = (15 * span.sizeChange).sp), start, end)
                    }
                    is ForegroundColorSpan -> {
                        addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
                    }
                }
            }
        }
    }
}
