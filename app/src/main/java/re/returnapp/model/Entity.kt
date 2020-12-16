package re.returnapp.model

import android.content.Context
import re.returnapp.R
import java.text.DateFormat
import java.text.SimpleDateFormat

open class Entity (
    var id: Int,
    var type: Type,
    var created: Long = System.currentTimeMillis(),
    var updated: Long = System.currentTimeMillis(),
    var name: String = "",
    var description: String = "",
    var reviewed: Boolean = false,
    var importance: Int = 0
) {
    companion object {
        private val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }

    fun getDisplayName(context: Context): String {
        return when (type) {
            Type.SESSION -> "${dateFormat.format(created)} - $name ${context.getString(R.string.min)}"
            Type.DIARY -> dateFormat.format(created)
            else -> name
        }
    }

    val hasNameField: Boolean get() {
        return type != Type.DIARY
    }

    val reviewable: Boolean get() {
        return type == Type.PEOPLE
                || type == Type.PERIOD
                || type == Type.PLACE
                || type == Type.EVENT
    }
}
