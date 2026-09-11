package com.localone.journal.ui.editor.components

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.AztecTextFormat

@Composable
fun AztecRichEditor(
    initialHtml: String,
    onContentChanged: (String) -> Unit,
    onEditorReady: (AztecText) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()

    AndroidView<AztecText>(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AztecText(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(textColor)
                setHintTextColor(AndroidColor.GRAY)
                hint = "书写你的今天..."
                textSize = 16f
                typeface = Typeface.SERIF
                setLineSpacing(12f, 1.2f)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                setPadding(0, 16, 0, 80)
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

                onEditorReady(this)
            }
        },
        update = { view ->
            view.setTextColor(textColor)
        }
    )
}
