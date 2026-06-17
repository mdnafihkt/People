package com.example.people.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "interactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class Interaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String
)
