package com.example.people.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val photoPath: String? = null,
    val howWeMet: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteracted: Long = System.currentTimeMillis()
)
