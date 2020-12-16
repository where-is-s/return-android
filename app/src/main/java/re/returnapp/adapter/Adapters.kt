package re.returnapp.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_standard_item.view.*
import re.returnapp.Collection
import re.returnapp.R
import re.returnapp.model.Entity
import re.returnapp.toDrawable

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

    override val containerView: View?
        get() = itemView

    fun bind(
        entity: Entity,
        clickListener: BaseEntityAdapter.ClickListener?,
        selected: Boolean
    ) {
        itemView.backgroundLayout.setBackgroundResource(if (selected) R.drawable.bkgrd_edit_dark else R.drawable.bkgrd_edit)
        itemView.headerText.text = entity.getDisplayName(itemView.context)
        var paintFlags = itemView.headerText.getPaintFlags()
        paintFlags = if (entity.reviewed) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else
            paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        itemView.headerText.setPaintFlags(paintFlags)
        itemView.descriptionText.setTextWithMentions(entity.description)
        itemView.typeView.setImageResource(entity.type.toDrawable())
        itemView.typeView.importance = entity.importance
        itemView.descriptionText.maxLines = 2;
        itemView.actionLayout.visibility = if (selected) View.VISIBLE else View.GONE
        val reviewInfoVisible = !selected && entity.reviewable
        if (reviewInfoVisible) {
            val sessionTime = Collection.findSessionTime(entity)
            itemView.sessionTimeText.text = "${sessionTime.sessionCount} (${sessionTime.totalTime} ${itemView.context.getString(R.string.min)})"
            itemView.infoLayout.visibility = if (sessionTime.sessionCount > 0) View.VISIBLE else View.GONE
        } else {
            itemView.infoLayout.visibility = View.GONE
        }
        itemView.descriptionText.maxLines = if (selected) Int.MAX_VALUE else 2
        itemView.setOnClickListener {
            clickListener?.onItemClicked(entity)
        }
        itemView.actionLayout.deleteButton.setOnClickListener {
            clickListener?.onDeleteAction(entity)
        }
        itemView.actionLayout.editButton.setOnClickListener {
            clickListener?.onEditAction(entity)
        }
        itemView.actionLayout.searchButton.setOnClickListener {
            clickListener?.onSearchAction(entity)
        }
        itemView.descriptionText.visibility = if (entity.description.isNotEmpty()) View.VISIBLE else View.GONE
    }
}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
}

class SimpleItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(
            dragFlags,
            swipeFlags
        )
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
    }

}

class SimpleTouchHelper(private val adapter: ItemTouchHelperAdapter,
                        callback: SimpleItemTouchHelperCallback = SimpleItemTouchHelperCallback(adapter)) : ItemTouchHelper(callback) {
}

class BaseEntityAdapter(private val entities: List<Entity>) : RecyclerView.Adapter<ItemViewHolder>(), ItemTouchHelperAdapter {

    interface ClickListener {
        fun onItemClicked(entity: Entity): Boolean
        fun onDeleteAction(entity: Entity)
        fun onEditAction(entity: Entity)
        fun onSearchAction(entity: Entity)
    }

    var externalClickListener: ClickListener? = null
    var selectedId = -1
    private val clickListener = object : ClickListener {
        override fun onItemClicked(entity: Entity): Boolean {
            if (externalClickListener == null) {
                return true
            }
            if (externalClickListener!!.onItemClicked(entity)) {
                return true
            }
            selectedId = if (selectedId == entity.id) -1 else entity.id
            notifyDataSetChanged()
            return true
        }

        override fun onDeleteAction(entity: Entity) {
            externalClickListener?.onDeleteAction(entity)
        }

        override fun onEditAction(entity: Entity) {
            externalClickListener?.onEditAction(entity)
        }

        override fun onSearchAction(entity: Entity) {
            externalClickListener?.onSearchAction(entity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_standard_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return entities.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(entities[position], clickListener, entities[position].id == selectedId)
    }

    override fun onItemDismiss(position: Int) {
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collection.swap(entities[i], entities[i + 1])
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collection.swap(entities[i], entities[i - 1])
            }
        }
        Collection.save()
        notifyItemMoved(fromPosition, toPosition)
    }

}
