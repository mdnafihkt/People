package com.example.people.data.model

data class PersonDetails(
    val person: Person,
    val customFields: List<CustomField> = emptyList(),
    val interactions: List<Interaction> = emptyList()
)
