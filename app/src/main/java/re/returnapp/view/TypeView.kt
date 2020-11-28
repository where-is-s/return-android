package re.returnapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.android.synthetic.main.view_search.view.*
import re.returnapp.R
import re.returnapp.dpToPx
import kotlin.math.max
import kotlin.math.min

class TypeView : AppCompatImageView {

    private val paint = Paint()
    var importance = -1
        set(value) {
            field = value
            invalidate()
        }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint.isAntiAlias = true
        paint.color = Color.RED
        paint.textSize = 8.dpToPx.toFloat()
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (importance <= 0 || importance > 10 || canvas == null) {
            return
        }

        val cx = 3 * width / 4F
        val cy = 2 * height / 3F
        paint.color = resources.getColor(when (importance) {
            1 -> R.color.importance_1
            2 -> R.color.importance_2
            3 -> R.color.importance_3
            4 -> R.color.importance_4
            5 -> R.color.importance_5
            6 -> R.color.importance_6
            7 -> R.color.importance_7
            8 -> R.color.importance_8
            9 -> R.color.importance_9
            10 -> R.color.importance_10
            else -> R.color.importance_0
        })
        canvas.drawCircle(cx, cy, paint.textSize * 0.8F, paint)

        paint.color = Color.BLACK
        canvas.drawText(importance.toString(), cx, cy + paint.textSize / 3, paint)
    }

}