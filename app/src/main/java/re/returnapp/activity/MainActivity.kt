package re.returnapp.activity

import re.returnapp.model.Entity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import re.returnapp.Collection
import re.returnapp.R
import re.returnapp.adapter.BaseEntityAdapter
import re.returnapp.adapter.SimpleTouchHelper
import re.returnapp.dialog.FindEntityDialog
import re.returnapp.model.Type
import re.returnapp.toIdStr
import re.returnapp.view.SearchView
import re.returnapp.view.SectionButton
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private var touchHelper: SimpleTouchHelper? = null
    private lateinit var currentType: Type
    private val typeButtons: MutableMap<Type, SectionButton> = EnumMap(Type::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (!Collection.load()) {
            for (i in 0..10) {
                val test = Entity(i, Type.PEOPLE)
                test.name = "Одноклассник " + i
                test.description = "Мы вместе учились в школе номер " + i
                Collection.addEntity(test)
            }
            Collection.save()
        }
        typeButtons[Type.PEOPLE] = peopleButton
        typeButtons[Type.PERIOD] = periodsButton
        typeButtons[Type.PLACE] = placesButton
        typeButtons[Type.EVENT] = eventsButton
        typeButtons[Type.SESSION] = sessionsButton
        typeButtons[Type.NOTE] = notesButton
        typeButtons[Type.PRACTICE] = practicesButton
        typeButtons[Type.DIARY] = diaryButton
        typeButtons[Type.TAG] = tagsButton
        for (type in Type.values()) {
            typeButtons[type]!!.setOnClickListener { switchToType(type) }
        }
        typeButtons[Type.PEOPLE]!!.isSelected = true
        switchToType(Type.PEOPLE)

        addButton.setOnClickListener {
            if (currentType == Type.SESSION) {
                SessionActivity.start(this)
            } else {
                EntityActivity.start(this, currentType)
            }
        }

        sortModeImage.setOnClickListener {
            val sortMode = when (Collection.getSortMode(currentType)) {
                SortMode.NAME -> SortMode.TIME
                SortMode.TIME -> SortMode.IMPORTANCE
                SortMode.IMPORTANCE -> SortMode.ORDER
                SortMode.ORDER -> SortMode.NAME
            }
            Collection.setSortMode(currentType, sortMode)
            updateSortImage()
            refreshAdapter()
        }

        searchView.searchListener = object : SearchView.SearchListener {
            override fun searchChanged() {
                refreshAdapter()
            }
        }

        showReviewedCheck.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
    }

    private fun updateSortImage() {
        sortModeImage.setImageResource(when (Collection.getSortMode(currentType)) {
            SortMode.TIME -> R.drawable.ic_period
            SortMode.NAME -> R.drawable.ic_alphabet_sort
            SortMode.ORDER -> R.drawable.ic_order_sort
            SortMode.IMPORTANCE -> R.drawable.ic_importance_sort
        })
    }

    private fun refreshAdapter() {
        var entities = if (searchView.text.isEmpty()) {
            Collection.findEntitiesByType(currentType)
        } else {
            Collection.search(searchView.text)
        }
        val counts: MutableMap<Type, Int> = EnumMap(Type::class.java)
        for (entity in entities) {
            counts[entity.type] = (counts[entity.type] ?: 0) + 1
        }
        entities = entities.filter { it.type == currentType }
        for (type in Type.values()) {
            typeButtons[type]?.setCount(counts[type] ?: 0)
        }
        if (!showReviewedCheck.isChecked) {
            entities = entities.filter { !it.reviewable || !it.reviewed }
        }
        val adapter = BaseEntityAdapter(entities)
        recyclerView.adapter = adapter
        if (touchHelper != null) {
            touchHelper!!.attachToRecyclerView(null)
            touchHelper = null
        }
        if (Collection.getSortMode(currentType) == SortMode.ORDER) {
            touchHelper = SimpleTouchHelper(adapter)
            touchHelper!!.attachToRecyclerView(recyclerView)
        }
        adapter.externalClickListener = object : BaseEntityAdapter.ClickListener {
            override fun onItemClicked(entity: Entity): Boolean {
                return false
            }

            override fun onDeleteAction(entity: Entity) {
                val mergeDialog = FindEntityDialog(this@MainActivity, "Заменить на...")
                mergeDialog.selectListener = object : FindEntityDialog.SelectListener {
                    override fun onItemSelected(mergeIntoEntity: Entity) {
                        mergeDialog.dismiss()
                        val alertDialog = AlertDialog.Builder(this@MainActivity)
                        alertDialog.setTitle("Удаление");
                        alertDialog.setMessage("Вы уверены, что хотите удалить " + entity.name + ", заменив его вхождения на " + mergeIntoEntity.name + "?")
                        alertDialog.setPositiveButton("Да") { _, _ ->
                            Collection.merge(entity, mergeIntoEntity)
                            refreshAdapter()
                        }
                        alertDialog.setNegativeButton("Нет", null)
                        alertDialog.show()
                    }
                }
                mergeDialog.show()
            }

            override fun onEditAction(entity: Entity) {
                EntityActivity.start(this@MainActivity, entity.id)
            }

            override fun onSearchAction(entity: Entity) {
                searchView.text = entity.id.toIdStr()
            }
        }
    }

    private fun switchToType(type: Type) {
        currentType = type
        updateSortImage()
        refreshAdapter()
    }

    override fun onResume() {
        super.onResume()
        refreshAdapter()
    }

}