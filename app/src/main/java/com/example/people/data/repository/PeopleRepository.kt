package com.example.people.data.repository

import com.example.people.data.database.PersonDao
import com.example.people.data.model.CustomField
import com.example.people.data.model.Interaction
import com.example.people.data.model.Person
import com.example.people.data.model.PersonDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class PeopleRepository(private val personDao: PersonDao) {

    fun getAllPeopleByName(): Flow<List<Person>> = personDao.getAllPeopleByName()

    fun getAllPeopleByLastInteracted(): Flow<List<Person>> = personDao.getAllPeopleByLastInteracted()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPersonDetails(personId: Int): Flow<PersonDetails?> {
        return personDao.getPersonById(personId).flatMapLatest { person ->
            if (person == null) {
                flowOf(null)
            } else {
                combine(
                    personDao.getCustomFieldsForPerson(personId),
                    personDao.getInteractionsForPerson(personId)
                ) { customFields, interactions ->
                    PersonDetails(person, customFields, interactions)
                }
            }
        }
    }

    suspend fun savePerson(person: Person, customFields: List<CustomField>): Int {
        val personId = if (person.id == 0) {
            personDao.insertPerson(person).toInt()
        } else {
            personDao.updatePerson(person)
            person.id
        }

        // Update custom fields by deleting old ones and inserting new ones
        personDao.deleteCustomFieldsForPerson(personId)
        customFields.forEach { field ->
            personDao.insertCustomField(field.copy(personId = personId))
        }

        return personId
    }

    suspend fun deletePerson(person: Person) {
        personDao.deletePerson(person)
    }

    suspend fun addInteraction(interaction: Interaction) {
        personDao.insertInteraction(interaction)
        // Also update the person's lastInteracted timestamp
        val person = personDao.getPersonById(interaction.personId).first()
        if (person != null) {
            personDao.updatePerson(person.copy(lastInteracted = interaction.timestamp))
        }
    }

    suspend fun deleteInteraction(interaction: Interaction) {
        personDao.deleteInteraction(interaction)
    }
}
