import re.returnapp.model.Entity
import re.returnapp.model.Type
import java.util.*
import kotlin.collections.ArrayList

val ACTUAL_DATA_VERSION = 2

enum class SortMode {
    TIME,
    NAME,
    ORDER,
    IMPORTANCE
}

data class Data (
    var version: Int = 0,
    var people: MutableList<Entity> = ArrayList(),
    var periods: MutableList<Entity> = ArrayList(),
    var places: MutableList<Entity> = ArrayList(),
    var events: MutableList<Entity> = ArrayList(),
    var sessions: MutableList<Entity> = ArrayList(),
    val notes: MutableList<Entity> = ArrayList(),
    val practices: MutableList<Entity> = ArrayList(),
    val tags: MutableList<Entity> = ArrayList(),
    val diaryEntries: MutableList<Entity> = ArrayList(),
    val sortModes: MutableMap<Type, SortMode> = EnumMap(Type::class.java)
)