package com.example.deadlyai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        NoteEntity::class,
        ReminderEntity::class,
        UserMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DeadlyDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: DeadlyDatabase? = null

        fun getInstance(context: Context): DeadlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeadlyDatabase::class.java,
                    "deadly_ai_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
