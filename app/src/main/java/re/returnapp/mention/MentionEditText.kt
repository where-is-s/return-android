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
import androidx.core.graphics.withTranslation

/**
 * A TextView that can draw rounded background to the portions of the text. See
 * [TextMentionHelper] for more information.
 *
 * See [TextMentionAttributeReader] for supported attributes.
 */
class MentionEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    enum class ArrangeDirection {
        BEGIN,
        NEAREST,
        END,
    }

    fun arrangeToSpan(cursor: Int, direction: ArrangeDirection): Int {
        val spans = (text as Spanned).getSpans(cursor, cursor, Annotation::class.java)
        val span = spans.firstOrNull() ?: return cursor
        return when (direction) {
            ArrangeDirection.BEGIN -> if (cursor < editableText.getSpanEnd(span)) {
                editableText.getSpanStart(span)
            } else {
                cursor
            }
            ArrangeDirection.NEAREST -> if (cursor < editableText.getSpanStart(span) / 2 + editableText.getSpanEnd(span) / 2) {
                editableText.getSpanStart(span)
            } else {
                editableText.getSpanEnd(span)
            }
            ArrangeDirection.END -> if (cursor > editableText.getSpanStart(span)) {
                editableText.getSpanEnd(span)
            } else {
                cursor
            }
        }
    }

    private val textMentionHelper: TextMentionHelper

    private val inputFilter = object : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            if (dest == null) {
                return null
            }
            val newStart = arrangeToSpan(dstart,
                ArrangeDirection.BEGIN
            )
            val newEnd = arrangeToSpan(dend,
                ArrangeDirection.END
            )
            if (newStart == dstart && newEnd == dend) { // not in span
                return null
            }
            return ""
        }
    }

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
        filters = arrayOf(inputFilter)
        addTextChangedListener(object: TextWatcher {
            private var fixRequired: Boolean = false
            private var deleteBefore: Int = 0
            private var deleteAfter: Int = 0

            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val newStart = arrangeToSpan(start,
                    ArrangeDirection.BEGIN
                )
                val newEnd = arrangeToSpan(start + count,
                    ArrangeDirection.END
                )
                if (newStart != start || newEnd != start + count) {
                    fixRequired = true
                    deleteBefore = start - newStart
                    deleteAfter = newEnd - (start + count)
                } else {
                    fixRequired = false
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!fixRequired) {
                    return
                }
                val builder = SpannableStringBuilder(s)
                if (deleteBefore > 0) {
                    builder.replace(start - deleteBefore, start, "")
                }
                if (deleteAfter > 0) {
                    builder.replace(start + count, start + count + deleteAfter, "")
                }
                moveCursor(start - deleteBefore, start - deleteBefore)
                text = builder
            }

        })
    }

    fun moveCursor(selStart: Int, selEnd: Int) {
        post { setSelection(selStart, selEnd) }
        // Stop cursor form jumping on move.
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (!isFocused) {
            return
        }
        val newStart = arrangeToSpan(selStart,
            ArrangeDirection.NEAREST
        )
        val newEnd = arrangeToSpan(selEnd,
            ArrangeDirection.NEAREST
        )
        if (newStart != selStart || newEnd != selEnd) {
            moveCursor(newStart, newEnd)
        }
        super.onSelectionChanged(newStart, newStart)
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