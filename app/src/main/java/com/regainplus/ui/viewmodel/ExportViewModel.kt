package com.regainplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.*
import com.regainplus.data.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val studyDao: StudyDao, private val taskDao: TaskDao,
    private val habitDao: HabitDao, private val blockedDao: BlockedDao,
    private val gymDao: GymDao, private val bodyMetricDao: BodyMetricDao,
    private val calorieDao: CalorieDao, private val waterDao: WaterDao,
    private val moodDao: MoodDao, private val tipDao: TipDao,
    private val wardrobeDao: WardrobeDao, private val outfitDao: OutfitDao,
    private val skillDao: SkillDao, private val flashcardDao: FlashcardDao
) : ViewModel() {
    var status by mutableStateOf("")
        private set

    fun export() {
        viewModelScope.launch {
            try {
                val o = JSONObject()
                fun arr(key: String, list: List<JSONObject>) = o.put(key, JSONArray(list))
                arr("study", studyDao.all().first().map { JSONObject().put("subject", it.subject).put("notes", it.notes).put("start", it.start).put("end", it.end).put("minutes", it.minutes) })
                arr("tasks", taskDao.all().first().map { JSONObject().put("title", it.title).put("done", it.done).put("dueDate", it.dueDate).put("priority", it.priority) })
                arr("habits", habitDao.activeDates().first().map { JSONObject().put("date", it) })
                arr("gym", gymDao.all().first().map { JSONObject().put("exercise", it.exercise).put("sets", it.sets).put("reps", it.reps).put("weight", it.weight).put("date", it.date) })
                arr("blocked", blockedDao.all().first().map { JSONObject().put("kind", it.kind).put("name", it.name) })
                arr("mood", moodDao.all().first().map { JSONObject().put("date", it.date).put("mood", it.mood).put("note", it.note) })
                arr("wardrobe", wardrobeDao.all().first().map { JSONObject().put("name", it.name).put("type", it.type).put("color", it.color).put("season", it.season).put("lastWorn", it.lastWorn).put("photoUri", it.photoUri) })
                arr("outfits", outfitDao.all().first().map { JSONObject().put("name", it.name).put("itemIds", it.itemIds).put("occasion", it.occasion).put("season", it.season).put("lastWorn", it.lastWorn) })
                arr("skills", skillDao.skills().first().map { JSONObject().put("name", it.name).put("category", it.category).put("active", it.active) })
                arr("calories", calorieDao.all().first().map { JSONObject().put("meal", it.meal).put("calories", it.calories).put("protein", it.protein).put("carbs", it.carbs).put("fat", it.fat).put("date", it.date) })
                arr("body", bodyMetricDao.all().first().map { JSONObject().put("date", it.date).put("weightKg", it.weightKg).put("note", it.note).put("photoUri", it.photoUri) })
                arr("water", waterDao.all().first().map { JSONObject().put("date", it.date).put("ml", it.ml) })
                arr("flashcards", flashcardDao.all().first().map { JSONObject().put("deck", it.deck).put("front", it.front).put("back", it.back).put("ease", it.ease.toDouble()).put("interval", it.interval).put("nextReview", it.nextReview).put("reviews", it.reviews) })
                arr("tips", tipDao.all().first().map { JSONObject().put("category", it.category).put("title", it.title).put("body", it.body).put("tier", it.tier).put("shownDate", it.shownDate) })
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "regainplus_export.json")
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.Downloads.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = ctx.contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    ctx.contentResolver.openOutputStream(uri)?.use { it.write(o.toString(2).toByteArray()) }
                    status = "Exported all data to Downloads/regainplus_export.json"
                } else {
                    val f = File(ctx.filesDir, "regainplus_export.json"); f.writeText(o.toString(2)); status = "Exported to ${f.absolutePath}"
                }
            } catch (e: Exception) { status = "Export failed: ${e.message}" }
        }
    }

    fun importData(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                val o = JSONObject(text)
                var n = 0
                suspend fun each(key: String, block: suspend (JSONObject) -> Unit) {
                    o.optJSONArray(key)?.let { a -> for (i in 0 until a.length()) { block(a.getJSONObject(i)); n++ } }
                }
                each("study") { studyDao.insert(StudySession(uuid(), it.optString("subject"), it.optString("notes"), it.optLong("start"), it.optLong("end"), it.optInt("minutes"))) }
                each("tasks") { taskDao.insert(Task(uuid(), it.optString("title"), it.optBoolean("done"), it.optLong("dueDate"), it.optInt("priority", 1))) }
                each("gym") { gymDao.insert(GymLog(uuid(), it.optString("exercise"), it.optInt("sets", 1), it.optInt("reps"), it.optDouble("weight"), it.optLong("date"))) }
                each("blocked") { blockedDao.insert(BlockedItem(uuid(), it.optString("kind"), it.optString("name"), System.currentTimeMillis())) }
                each("mood") { moodDao.insert(MoodLog(uuid(), it.optString("date"), it.optInt("mood", 3), it.optString("note"))) }
                each("wardrobe") { wardrobeDao.insert(WardrobeItem(uuid(), it.optString("name"), it.optString("type"), it.optString("color"), it.optString("season"), it.optLong("lastWorn").takeIf { it > 0 }, it.optString("photoUri").ifBlank { null })) }
                each("outfits") { outfitDao.insert(Outfit(uuid(), it.optString("name"), it.optString("itemIds"), it.optString("occasion"), it.optString("season"), it.optLong("lastWorn").takeIf { it > 0 })) }
                each("skills") { skillDao.insertSkill(Skill(uuid(), it.optString("name"), it.optString("category"), it.optBoolean("active", true))) }
                each("calories") { calorieDao.insert(CalorieLog(uuid(), it.optString("meal"), it.optInt("calories"), it.optInt("protein"), it.optInt("carbs"), it.optInt("fat"), it.optLong("date"))) }
                each("body") { bodyMetricDao.insert(BodyMetric(uuid(), it.optLong("date"), it.optDouble("weightKg"), it.optString("note"), it.optString("photoUri").ifBlank { null })) }
                each("water") { waterDao.insert(WaterLog(uuid(), it.optLong("date"), it.optInt("ml"))) }
                each("flashcards") { flashcardDao.insert(Flashcard(uuid(), it.optString("deck"), it.optString("front"), it.optString("back"), it.optDouble("ease", 2.5).toFloat(), it.optInt("interval", 1), it.optLong("nextReview"), it.optInt("reviews"))) }
                each("habits") { habitDao.upsert(HabitLog(it.optString("date"), "focus", true, it.optInt("minutes"))) }
                each("tips") { tipDao.insert(Tip(uuid(), it.optString("category"), it.optString("title"), it.optString("body"), it.optString("tier"), it.optString("shownDate").ifBlank { null })) }
                status = "Imported $n records"
            } catch (e: Exception) { status = "Import failed: ${e.message}" }
        }
    }

    private fun uuid() = java.util.UUID.randomUUID().toString()
}
