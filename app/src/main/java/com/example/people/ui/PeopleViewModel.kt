package com.example.people.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.people.data.model.CustomField
import com.example.people.data.model.Interaction
import com.example.people.data.model.Person
import com.example.people.data.model.PersonDetails
import com.example.people.data.repository.PeopleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

enum class SortOrder {
    NAME, LAST_INTERACTED
}

class PeopleViewModel(private val repository: PeopleRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NAME)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Combined people flow sorted and filtered
    @OptIn(ExperimentalCoroutinesApi::class)
    val people: StateFlow<List<Person>> = combine(_sortOrder, _searchQuery) { sortOrder, query ->
        Pair(sortOrder, query)
    }.flatMapLatest { (sortOrder, query) ->
        val baseFlow = when (sortOrder) {
            SortOrder.NAME -> repository.getAllPeopleByName()
            SortOrder.LAST_INTERACTED -> repository.getAllPeopleByLastInteracted()
        }
        baseFlow.map { list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter { person ->
                    person.name.contains(query, ignoreCase = true) ||
                            person.howWeMet.contains(query, ignoreCase = true) ||
                            person.notes.contains(query, ignoreCase = true)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected person details flow
    private val _selectedPersonId = MutableStateFlow<Int?>(null)
    val selectedPersonId: StateFlow<Int?> = _selectedPersonId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPersonDetails: StateFlow<PersonDetails?> = _selectedPersonId.flatMapLatest { id ->
        if (id == null) {
            MutableStateFlow(null)
        } else {
            repository.getPersonDetails(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun selectPerson(id: Int?) {
        _selectedPersonId.value = id
    }

    fun savePerson(person: Person, customFields: List<CustomField>, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.savePerson(person, customFields)
            onComplete(id)
        }
    }

    fun deletePerson(person: Person, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePerson(person)
            if (_selectedPersonId.value == person.id) {
                _selectedPersonId.value = null
            }
            // If the person has a local photo, delete it
            person.photoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            onComplete()
        }
    }

    fun addInteraction(personId: Int, notes: String) {
        viewModelScope.launch {
            val interaction = Interaction(personId = personId, notes = notes)
            repository.addInteraction(interaction)
        }
    }

    fun deleteInteraction(interaction: Interaction) {
        viewModelScope.launch {
            repository.deleteInteraction(interaction)
        }
    }

    // Helper to save image from Uri to internal storage
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "profile_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class PeopleViewModelFactory(private val repository: PeopleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PeopleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
