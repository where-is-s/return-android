package re.returnapp.dialog

import re.returnapp.model.Entity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_find_entity.recyclerView
import kotlinx.android.synthetic.main.dialog_find_entity.searchView
import re.returnapp.Collection
import re.returnapp.R
import re.returnapp.adapter.BaseEntityAdapter
import re.returnapp.dpToPx
import re.returnapp.view.SearchView

class FindEntityDialog(context: Context?) : AppCompatDialog(context) {

    companion object {
        var lastSearch: String = ""
    }

    interface SelectListener {
        fun onItemSelected(entity: Entity)
    }

    var selectListener: SelectListener? = null
    private var title: String = ""

    constructor(context: Context?, title: String) : this(context) {
        this.title = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_find_entity)

        window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        window!!.setLayout(320.dpToPx, ViewGroup.LayoutParams.MATCH_PARENT)

        recyclerView.layoutManager = LinearLayoutManager(context)
        searchView.title = title
        searchView.requestFocus()

        searchView.searchListener = object: SearchView.SearchListener {
            override fun searchChanged() {
                lastSearch = searchView.text
                update(searchView.text)
            }
        }

        searchView.showClearButton()
        update(lastSearch)
    }

    private fun update(search: String) {
        val adapter = BaseEntityAdapter(Collection.search(search))
        recyclerView.adapter = adapter
        adapter.externalClickListener = object : BaseEntityAdapter.ClickListener {
            override fun onItemClicked(entity: Entity): Boolean {
                selectListener?.onItemSelected(entity)
                dismiss()
                return true
            }

            override fun onEditAction(entity: Entity) {
            }

            override fun onDeleteAction(entity: Entity) {
            }

            override fun onSearchAction(entity: Entity) {
            }
        }
    }

}