package com.localone.journal.ui.editor.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import com.localone.journal.R
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarItems

@Composable
fun AztecRichEditor(
    initialHtml: String,
    onContentChanged: (String) -> Unit,
    onAddPhotoClick: () -> Unit = {},
    onEditorReady: (AztecText) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.toArgb().let {
        // 计算亮度判断是否为暗色
        val r = (it shr 16) and 0xff
        val g = (it shr 8) and 0xff
        val b = it and 0xff
        (0.299 * r + 0.587 * g + 0.114 * b) < 128
    }

    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val toolbarBgColor = if (isDark) 0xFF1C1C1E.toInt() else 0xFFF2F2F7.toInt()
    val toolbarBorderColor = if (isDark) 0xFF2C2C2E.toInt() else 0xFFE5E5EA.toInt()
    val iconTint = if (isDark) 0xFFE5E5EA.toInt() else 0xFF3A3A3C.toInt()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val themedContext = ContextThemeWrapper(ctx, R.style.Theme_LocalOne)
            val rootLayout = LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // 1. 正文可滚动区域
            val scrollView = NestedScrollView(themedContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                isFillViewport = true
            }

            val aztecText = AztecText(themedContext).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(textColor)
                setHintTextColor(if (isDark) 0xFF636366.toInt() else 0xFF8E8E93.toInt())
                hint = "书写你的今天..."
                textSize = 17f
                typeface = Typeface.SERIF
                setLineSpacing(14f, 1.25f)
                background = null
                setPadding(48, 24, 48, 120)
                isFocusable = true
                isFocusableInTouchMode = true

                if (initialHtml.isNotEmpty()) {
                    fromHtml(initialHtml, false)
                }

                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        onContentChanged(toHtml(false))
                    }
                })
            }
            scrollView.addView(aztecText)
            rootLayout.addView(scrollView)

            // 2. 底部格式化工具栏容器
            val toolbarContainer = LinearLayout(themedContext).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (50 * ctx.resources.displayMetrics.density).toInt()
                )
                setBackgroundColor(toolbarBgColor)
                gravity = Gravity.CENTER_VERTICAL
            }

            // 格式化工具栏顶部分割线
            val topBorder = android.view.View(themedContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (1 * ctx.resources.displayMetrics.density).toInt()
                )
                setBackgroundColor(toolbarBorderColor)
            }
            rootLayout.addView(topBorder)

            val aztecToolbar = AztecToolbar(themedContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
                setBackgroundColor(toolbarBgColor)
            }

            // 配置原版 Aztec Toolbar Items
            val toolbarItems = ToolbarItems.BasicLayout(
                ToolbarAction.HEADING,
                ToolbarAction.BOLD,
                ToolbarAction.ITALIC,
                ToolbarAction.UNDERLINE,
                ToolbarAction.STRIKETHROUGH,
                ToolbarAction.TASK_LIST,
                ToolbarAction.UNORDERED_LIST,
                ToolbarAction.ORDERED_LIST,
                ToolbarAction.QUOTE,
                ToolbarAction.HORIZONTAL_RULE
            )
            aztecToolbar.setToolbarItems(toolbarItems)
            aztecToolbar.enableTaskList()

            // Aztec 双向无缝连接
            Aztec.with(aztecText, aztecToolbar, object : IAztecToolbarClickListener {
                override fun onToolbarCollapseButtonClicked() {}
                override fun onToolbarExpandButtonClicked() {}
                override fun onToolbarFormatButtonClicked(format: ITextFormat, isApplied: Boolean) {}
                override fun onToolbarHeadingButtonClicked() {}
                override fun onToolbarHtmlButtonClicked() {}
                override fun onToolbarListButtonClicked() {}
                override fun onToolbarMediaButtonClicked(): Boolean {
                    onAddPhotoClick()
                    return true
                }
            })

            toolbarContainer.addView(aztecToolbar)

            // 竖向分割线
            val divider = android.view.View(themedContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (1 * ctx.resources.displayMetrics.density).toInt(),
                    (28 * ctx.resources.displayMetrics.density).toInt()
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                setBackgroundColor(toolbarBorderColor)
            }
            toolbarContainer.addView(divider)

            // 收起键盘按钮
            val hideKeyboardBtn = ImageView(themedContext).apply {
                val btnSize = (48 * ctx.resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(btnSize, btnSize).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                val pad = (12 * ctx.resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                setImageResource(android.R.drawable.arrow_down_float)
                setColorFilter(iconTint)
                contentDescription = "收起键盘"
                isClickable = true
                isFocusable = false
                setBackgroundResource(android.R.drawable.list_selector_background)

                setOnClickListener {
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(aztecText.windowToken, 0)
                    aztecText.clearFocus()
                }
            }
            toolbarContainer.addView(hideKeyboardBtn)

            rootLayout.addView(toolbarContainer)

            onEditorReady(aztecText)
            rootLayout
        },
        update = { view ->
            // 可更新文本颜色等
        }
    )
}
