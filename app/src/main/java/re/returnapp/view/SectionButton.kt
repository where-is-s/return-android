package re.returnapp.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import kotlinx.android.synthetic.main.view_section_button.view.*
import re.returnapp.R
import re.returnapp.dpToPx


class SectionButton : FrameLayout {

    private lateinit var layout: View

    private var ownClickListener: OnClickListener? = null

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        layout = LayoutInflater.from(context).inflate(R.layout.view_section_button, this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.SectionButton, 0, 0)

        try {
            setText(array.getString(R.styleable.SectionButton_text) ?: "")
            setImage(array.getResourceId(R.styleable.SectionButton_image, 0))
            setCount(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        array.recycle()

        super.setOnClickListener {
            isSelected = true
            ownClickListener?.onClick(it)
        }
        isSelected = false
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        val color = resources.getColor(if (selected) R.color.bottom_bar_main else R.color.bottom_bar_unselected)
        text.setTextColor(color)
        text.visibility = if (selected) View.VISIBLE else View.GONE
        typeView.imageTintList = ColorStateList.valueOf(color)
        if (layoutParams != null) {
            val params = (layoutParams as LinearLayout.LayoutParams)
            params.weight = if (selected) 1.7f else 1f
            layoutParams = params
        }
        typeView.layoutParams.width = if (selected) 28.dpToPx else 20.dpToPx
        layout.setBackgroundResource(if (selected) R.drawable.bkgrd_selection else 0)
        if (selected) {
            for (child in (parent as ViewGroup).children) {
                if (child is SectionButton && child != this) {
                    child.isSelected = false
                }
            }
        }
    }

    fun setText(str: String) {
        text.text = str
    }

    fun setImage(id: Int) {
        typeView.setImageResource(id)
    }

    fun setCount(count: Int) {
        if (count <= 0) {
            countText.visibility = View.GONE
        } else {
            countText.visibility = View.VISIBLE
            countText.text = count.toString()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        ownClickListener = l
    }

}