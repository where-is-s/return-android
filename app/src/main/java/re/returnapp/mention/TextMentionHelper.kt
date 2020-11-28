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

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.*
import android.text.Annotation
import android.text.style.StrikethroughSpan
import kotlinx.android.synthetic.main.activity_entity.*
import re.returnapp.Collection
import re.returnapp.toIdStr

/**
 * Helper class to draw multi-line rounded background to certain parts of a text. The start/end
 * positions of the backgrounds are annotated with [android.text.Annotation] class. Each annotation
 * should have the annotation key set to **rounded**.
 *
 * i.e.:
 * ```
 *    <!--without the quotes at the begining and end Android strips the whitespace and also starts
 *        the annotation at the wrong position-->
 *    <string name="ltr">"this is <annotation key="rounded">a regular</annotation> paragraph."</string>
 * ```
 *
 * **Note:** BiDi text is not supported.
 *
 * @param horizontalPadding the padding to be applied to left & right of the background
 * @param verticalPadding the padding to be applied to top & bottom of the background
 * @param drawable the drawable used to draw the background
 * @param drawableLeft the drawable used to draw left edge of the background
 * @param drawableMid the drawable used to draw for whole line
 * @param drawableRight the drawable used to draw right edge of the background
 */
class TextMentionHelper(
    val horizontalPadding: Int,
    verticalPadding: Int,
    drawable: Drawable,
    drawableLeft: Drawable,
    drawableMid: Drawable,
    drawableRight: Drawable
) {

    private val singleLineRenderer: TextRoundedBgRenderer by lazy {
        SingleLineRenderer(
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            drawable = drawable
        )
    }

    private val multiLineRenderer: TextRoundedBgRenderer by lazy {
        MultiLineRenderer(
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            drawableLeft = drawableLeft,
            drawableMid = drawableMid,
            drawableRight = drawableRight
        )
    }

    /**
     * Call this function during onDraw of another widget such as TextView.
     *
     * @param canvas Canvas to draw onto
     * @param text
     * @param layout Layout that contains the text
     */
    fun draw(canvas: Canvas, text: Spanned, layout: Layout, maxLines: Int) {
        // ideally the calculations here should be cached since they are not cheap. However, proper
        // invalidation of the cache is required whenever anything related to text has changed.
        val spans = text.getSpans(0, text.length, Annotation::class.java)
        spans.forEach { span ->
            if (span.key == "entityId") {
                val spanStart = text.getSpanStart(span)
                val spanEnd = text.getSpanEnd(span)
                val startLine = layout.getLineForOffset(spanStart)
                val endLine = layout.getLineForOffset(spanEnd)

                if (startLine >= maxLines) {
                    return@forEach
                }

                // start can be on the left or on the right depending on the language direction.
                val startOffset = (layout.getPrimaryHorizontal(spanStart)
                    + -1 * layout.getParagraphDirection(startLine) * horizontalPadding).toInt()
                // end can be on the left or on the right depending on the language direction.
                val endOffset = (layout.getPrimaryHorizontal(spanEnd)
                    + layout.getParagraphDirection(endLine) * horizontalPadding).toInt()

                val renderer = if (startLine == endLine) singleLineRenderer else multiLineRenderer
                renderer.draw(canvas, layout, startLine, endLine, startOffset, endOffset, maxLines)
            }
        }
    }

    fun parseMentions(source: String): SpannableStringBuilder {
        val text = SpannableStringBuilder(source)
        var mentionIndex: Int
        while (text.indexOf("~@") > -1) {
            mentionIndex = text.indexOf("~@");
            val idStart = mentionIndex + 2
            val idEnd = text.indexOf('@', mentionIndex + 2)
            if (idEnd == -1) {
                return SpannableStringBuilder(source)
            }
            val id = text.substring(idStart, idEnd).toIntOrNull()
            val mentionedEntity = id?.let { Collection.getEntityById(it) }
            if (mentionedEntity == null) {
                text.replace(mentionIndex, idEnd, "")
                continue
            }
            val nameStr = '\uFEFF' + mentionedEntity.displayName + '\uFEFF'
            text.replace(mentionIndex, idEnd + 1, nameStr)
            text.setSpan(Annotation("entityId", id.toString()),
                mentionIndex,
                mentionIndex + nameStr.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (mentionedEntity.reviewed) {
                text.setSpan(
                    StrikethroughSpan(),
                    mentionIndex,
                    mentionIndex + nameStr.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return text
    }

    fun insertMentions(source: CharSequence?): String {
        if (source.isNullOrEmpty()) {
            return String()
        }
        val text = SpannableStringBuilder(source)
        val spans = text.getSpans(0, text.length, Annotation::class.java)
        val result = StringBuilder(text.toString())
        for (annotation in spans.sortedByDescending { span -> text.getSpanStart(span) }) {
            result.replace(text.getSpanStart(annotation), text.getSpanEnd(annotation), annotation.value.toInt().toIdStr())
        }
        return result.toString()
    }
}