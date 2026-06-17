package com.example.people

import android.app.Application
import com.example.people.data.database.PeopleDatabase
import com.example.people.data.repository.PeopleRepository

class PeopleApplication : Application() {
    val database by lazy { PeopleDatabase.getDatabase(this) }
    val repository by lazy { PeopleRepository(database.personDao()) }
}
