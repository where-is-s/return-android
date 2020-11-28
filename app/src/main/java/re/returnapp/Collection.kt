package re.returnapp

import Data
import SortMode
import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import re.returnapp.model.Entity
import re.returnapp.model.Type
import java.util.regex.Pattern
import kotlin.math.max

object Collection {
    private const val POST_JSON_URL = ""

    private var data = Data()
    var newId: Int = 0
        private set

    private lateinit var queue: RequestQueue
    private lateinit var context: Context
    private var lastUploadTime: Long = 0
    private val cache = ArrayList<Entity>()

    fun init(context: Context) {
        this.context = context
        queue = Volley.newRequestQueue(context)
    }

    fun search(text: String): List<Entity> {
        val results = mutableListOf<Entity>()
        results.addAll(getCache().filter { e -> e.name.contains(text, ignoreCase = true) })
        results.addAll(getCache().filter { e -> !results.contains(e) && e.description.contains(text, ignoreCase = true) })
        return results
    }

    fun getEntityById(id: Int): Entity? {
        return getCache().firstOrNull { e -> id == e.id }
    }

    fun getEntitiesByType(type: Type): MutableList<Entity> {
        return when (type) {
            Type.PEOPLE -> data.people
            Type.PERIOD -> data.periods
            Type.PLACE -> data.places
            Type.EVENT -> data.events
            Type.SESSION -> data.sessions
            Type.NOTE -> data.notes
            Type.PRACTICE -> data.practices
            Type.DIARY -> data.diaryEntries
            Type.TAG -> data.tags
        }
    }

    fun findEntitiesByType(type: Type): List<Entity> {
        val entities = getEntitiesByType(type)
        return when (getSortMode(type)) {
            SortMode.TIME -> entities.sortedByDescending { it.created }
            SortMode.NAME -> entities.sortedBy { it.name }
            SortMode.IMPORTANCE -> entities.sortedWith(compareByDescending<Entity> { it.importance }.thenByDescending { it.created })
            SortMode.ORDER -> entities
        }
    }

    fun addEntity(entity: Entity) {
        newId = max(newId, entity.id) + 1
        getEntitiesByType(entity.type).add(entity)
        invalidateCache()
    }

    private fun deserialize(file: String) {
        this.data = Gson().fromJson(file, Data::class.java)
    }

    private fun serialize(): String {
        return Gson().toJson(this.data);
    }

    fun load(): Boolean {
        val collectionStr = context.getSharedPreferences("Global", Context.MODE_PRIVATE)
            .getString("Collection", null) ?: return false
        deserialize(collectionStr)
        getCache().forEach { newId = max(newId, it.id + 1) }
        return getCache().isNotEmpty()
    }

    fun save() {
        context.getSharedPreferences("Global", Context.MODE_PRIVATE)
            .edit().putString("Collection", serialize()).apply()
        upload()
    }

    fun merge(deleteEntity: Entity, mergeIntoEntity: Entity) {
        val pattern = Pattern.compile(deleteEntity.id.toIdStr())
        getEntitiesByType(deleteEntity.type).remove(deleteEntity)
        invalidateCache()
        for (entity in getCache()) {
            var matcher = pattern.matcher(entity.description)
            entity.description = matcher.replaceAll(mergeIntoEntity.id.toIdStr())
        }
        save()
    }

    fun findMentionsOf(entity: Entity, type: Type? = null): List<Entity> {
        val pattern = Pattern.compile(entity.id.toIdStr())
        val result = ArrayList<Entity>()
        val set = if (type == null) getCache() else getEntitiesByType(type)
        for (e in set) {
            if (pattern.matcher(e.description).find()) {
                result.add(e)
            }
        }
        return result
    }

    fun findMentionsIn(entity: Entity): List<Entity> {
        val result = ArrayList<Entity>()
        val text = entity.description
        var currentIndex = 0
        while (text.indexOf("~@", startIndex = currentIndex) > -1) {
            val mentionIndex = text.indexOf("~@", startIndex = currentIndex);
            val idStart = mentionIndex + 2
            val idEnd = text.indexOf('@', mentionIndex + 2)
            currentIndex = idEnd + 1
            if (idEnd == -1) {
                break
            }
            val id = text.substring(idStart, idEnd).toIntOrNull()
            val mentionedEntity = id?.let { getEntityById(it) } ?: continue
            result.add(mentionedEntity)
        }
        return result
    }

    fun findSessionTime(entity: Entity): SessionTime {
        val sessions = findMentionsOf(entity, Type.SESSION)
        var totalTime = 0f
        for (s in sessions) {
            val innerMentions = findMentionsIn(s).filter { p -> p.reviewable } // do not take non-reviewable entities into account
            if (innerMentions.isEmpty()) {
                continue
            }
            val time = try {
                s.name.toInt()
            } catch (e: NumberFormatException) {
                0
            }
            totalTime += time.toFloat() / innerMentions.size
        }
        return SessionTime(sessions.size, totalTime.toInt())
    }

    private fun upload(force: Boolean = false) {
        if (POST_JSON_URL.isNullOrEmpty()) {
            return
        }
        if (!force && System.currentTimeMillis() < lastUploadTime + 60000) {
            return
        }
        lastUploadTime = System.currentTimeMillis()
        val stringRequest = object : StringRequest(
            Method.POST,
            POST_JSON_URL,
            Response.Listener { /* nothing to do */ },
            Response.ErrorListener { error -> error.printStackTrace() }) {
            override fun getBody(): ByteArray {
                return serialize().toByteArray()
            }
        }
        queue.add(stringRequest)
    }

    fun applyChanges() {
        invalidateCache()
    }

    private fun invalidateCache() {
        cache.clear()
    }

    private fun getCache(): List<Entity> {
        if (cache.isNotEmpty()) {
            return cache
        }
        Type.values().forEach { cache.addAll(findEntitiesByType(it)) }
        return cache
    }

    fun swap(entity1: Entity, entity2: Entity) {
        if (entity1.type != entity2.type) {
            throw IllegalArgumentException()
        }
        val entities = getEntitiesByType(entity1.type)
        val idx1 = entities.indexOf(entity1)
        val idx2 = entities.indexOf(entity2)
        entities[idx2] = entity1
        entities[idx1] = entity2
        invalidateCache()
    }

    fun setSortMode(type: Type, sortMode: SortMode) {
        data.sortModes[type] = sortMode
        invalidateCache()
        save() // TODO ?
    }

    fun getSortMode(type: Type): SortMode {
        return data.sortModes[type] ?: SortMode.NAME
    }
}