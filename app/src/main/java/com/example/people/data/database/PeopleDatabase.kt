package com.example.people.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.people.data.model.CustomField
import com.example.people.data.model.Interaction
import com.example.people.data.model.Person

@Database(
    entities = [Person::class, CustomField::class, Interaction::class],
    version = 1,
    exportSchema = false
)
abstract class PeopleDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao

    companion object {
        @Volatile
        private var INSTANCE: PeopleDatabase? = null

        fun getDatabase(context: Context): PeopleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PeopleDatabase::class.java,
                    "people_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
