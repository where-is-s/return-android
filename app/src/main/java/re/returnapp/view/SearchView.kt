package re.returnapp.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_search.view.*
import re.returnapp.R

class SearchView : FrameLayout {

    private lateinit var layout: View

    var searchListener: SearchListener? = null

    var text: String
        get() {
            return searchEdit.getTextWithMentions()
        }
        set(value) {
            searchEdit.setTextWithMentions(value)
        }

    var title: String = ""
        set(value) {
            searchEdit.hint = value
            field = value
        }

    interface SearchListener {
        fun searchChanged()
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
        layout = LayoutInflater.from(context).inflate(R.layout.view_search, this)

        searchEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchListener?.searchChanged()
                updateUI()
            }
        })

        clearButton.setOnClickListener {
            searchEdit.setText("")
        }

        updateUI()
    }

    private fun updateUI() {
        clearButton.visibility = if (searchEdit.text.isNullOrEmpty()) GONE else VISIBLE
    }

    fun showClearButton() {
        clearButton.visibility = View.VISIBLE
    }

}