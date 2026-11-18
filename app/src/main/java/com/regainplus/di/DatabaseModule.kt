package com.regainplus.di

import android.content.Context
import androidx.room.Room
import com.regainplus.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "regainplus.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun studyDao(db: AppDatabase) = db.studyDao()
    @Provides fun taskDao(db: AppDatabase) = db.taskDao()
    @Provides fun habitDao(db: AppDatabase) = db.habitDao()
    @Provides fun blockedDao(db: AppDatabase) = db.blockedDao()
    @Provides fun gymDao(db: AppDatabase) = db.gymDao()
    @Provides fun tipDao(db: AppDatabase) = db.tipDao()
    @Provides fun moodDao(db: AppDatabase) = db.moodDao()
    @Provides fun wardrobeDao(db: AppDatabase) = db.wardrobeDao()
    @Provides fun outfitDao(db: AppDatabase) = db.outfitDao()
    @Provides fun skillDao(db: AppDatabase) = db.skillDao()
    @Provides fun calorieDao(db: AppDatabase) = db.calorieDao()
    @Provides fun bodyMetricDao(db: AppDatabase) = db.bodyMetricDao()
    @Provides fun waterDao(db: AppDatabase) = db.waterDao()
    @Provides fun flashcardDao(db: AppDatabase) = db.flashcardDao()
}