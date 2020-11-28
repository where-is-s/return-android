package re.returnapp.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_type.*
import kotlinx.android.synthetic.main.view_type_item.view.*
import re.returnapp.R
import re.returnapp.model.Type
import re.returnapp.toDrawable

class ChooseTypeDialog(context: Context?) : AppCompatDialog(context) {

    inner class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View?
            get() = itemView

        fun bind(type: Type) {
            itemView.typeView.setImageResource(type.toDrawable())
            itemView.name.text = when (type) {
                Type.PEOPLE -> "Человек"
                Type.PERIOD -> "Период"
                Type.PLACE -> "Место"
                Type.EVENT -> "Событие"
                Type.SESSION -> "Сессия"
                Type.NOTE -> "Заметка"
                Type.PRACTICE -> "Практики"
                Type.DIARY -> "Дневник"
                Type.TAG -> "Теги"
            }
            itemView.setOnClickListener {
                onItemSelected(type)
            }
        }
    }

    inner class TypeAdapter : RecyclerView.Adapter<TypeViewHolder>() {
        var types: Array<Type> = arrayOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.view_type_item, parent, false)
            return TypeViewHolder(view)
        }

        override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
            holder.bind(types[position])
        }

        override fun getItemCount(): Int {
            return types.size
        }
    }

    interface SelectListener {
        fun onItemSelected(type: Type)
    }

    var selectListener: SelectListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_type)

        recyclerView.layoutManager = LinearLayoutManager(context)
        val typeAdapter = TypeAdapter()
        typeAdapter.types = arrayOf(Type.PEOPLE, Type.PERIOD, Type.PLACE, Type.EVENT, Type.SESSION, Type.NOTE)
        recyclerView.adapter = typeAdapter
    }

    private fun onItemSelected(type: Type) {
        selectListener?.onItemSelected(type)
        dismiss()
    }

}