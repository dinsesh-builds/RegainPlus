package com.regainplus.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.regainplus.data.dao.BlockedDao
import com.regainplus.data.dao.BodyMetricDao
import com.regainplus.data.dao.FlashcardDao
import com.regainplus.data.dao.CalorieDao
import com.regainplus.data.dao.WaterDao
import com.regainplus.data.dao.GymDao
import com.regainplus.data.dao.HabitDao
import com.regainplus.data.dao.MoodDao
import com.regainplus.data.dao.SkillDao
import com.regainplus.data.dao.StudyDao
import com.regainplus.data.dao.TaskDao
import com.regainplus.data.dao.TipDao
import com.regainplus.data.dao.WardrobeDao
import com.regainplus.data.dao.OutfitDao
import com.regainplus.data.entity.BlockedItem
import com.regainplus.data.entity.BodyMetric
import com.regainplus.data.entity.CalorieLog
import com.regainplus.data.entity.Flashcard
import com.regainplus.data.entity.WaterLog
import com.regainplus.data.entity.GymLog
import com.regainplus.data.entity.HabitLog
import com.regainplus.data.entity.MoodLog
import com.regainplus.data.entity.Skill
import com.regainplus.data.entity.SkillSession
import com.regainplus.data.entity.StudySession
import com.regainplus.data.entity.Task
import com.regainplus.data.entity.Tip
import com.regainplus.data.entity.WardrobeItem
import com.regainplus.data.entity.Outfit

@Database(
    entities = [StudySession::class, Task::class, HabitLog::class, BlockedItem::class, GymLog::class, Tip::class, MoodLog::class, WardrobeItem::class, Skill::class, SkillSession::class, CalorieLog::class, BodyMetric::class, WaterLog::class, Flashcard::class, Outfit::class],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "regainplus.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }

    abstract fun studyDao(): StudyDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun blockedDao(): BlockedDao
    abstract fun gymDao(): GymDao
    abstract fun tipDao(): TipDao
    abstract fun moodDao(): MoodDao
    abstract fun wardrobeDao(): WardrobeDao
    abstract fun outfitDao(): OutfitDao
    abstract fun skillDao(): SkillDao
    abstract fun calorieDao(): CalorieDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun waterDao(): WaterDao
    abstract fun flashcardDao(): FlashcardDao
}