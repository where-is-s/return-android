/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package re.returnapp.mention

import android.content.Context
import android.graphics.Canvas
import android.text.*
import android.text.Annotation
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation

/**
 * A TextView that can draw rounded background to the portions of the text. See
 * [TextMentionHelper] for more information.
 *
 * See [TextMentionAttributeReader] for supported attributes.
 */
class MentionTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val textMentionHelper: TextMentionHelper

    init {
        val attributeReader =
            TextMentionAttributeReader(
                context,
                attrs
            )
        textMentionHelper =
            TextMentionHelper(
                horizontalPadding = attributeReader.horizontalPadding,
                verticalPadding = attributeReader.verticalPadding,
                drawable = attributeReader.drawable,
                drawableLeft = attributeReader.drawableLeft,
                drawableMid = attributeReader.drawableMid,
                drawableRight = attributeReader.drawableRight
            )
    }

    override fun onDraw(canvas: Canvas) {
        // need to draw bg first so that text can be on top during super.onDraw()
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                textMentionHelper.draw(canvas, text as Spanned, layout, maxLines)
            }
        }
        super.onDraw(canvas)
    }

    fun setTextWithMentions(text: String) {
        setText(textMentionHelper.parseMentions(text, context))
    }

    fun getTextWithMentions(): String {
        return textMentionHelper.insertMentions(text)
    }
}