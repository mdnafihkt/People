package com.example.people.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.people.data.model.CustomField
import com.example.people.data.model.Interaction
import com.example.people.data.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    // Person Queries
    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeopleByName(): Flow<List<Person>>

    @Query("SELECT * FROM people ORDER BY lastInteracted DESC")
    fun getAllPeopleByLastInteracted(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE id = :id")
    fun getPersonById(id: Int): Flow<Person?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    // Custom Field Queries
    @Query("SELECT * FROM custom_fields WHERE personId = :personId")
    fun getCustomFieldsForPerson(personId: Int): Flow<List<CustomField>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomField(customField: CustomField): Long

    @Query("DELETE FROM custom_fields WHERE personId = :personId")
    suspend fun deleteCustomFieldsForPerson(personId: Int)

    // Interaction Queries
    @Query("SELECT * FROM interactions WHERE personId = :personId ORDER BY timestamp DESC")
    fun getInteractionsForPerson(personId: Int): Flow<List<Interaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: Interaction): Long

    @Update
    suspend fun updateInteraction(interaction: Interaction)

    @Delete
    suspend fun deleteInteraction(interaction: Interaction)
}
