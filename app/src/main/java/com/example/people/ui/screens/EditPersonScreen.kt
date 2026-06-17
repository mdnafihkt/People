package com.example.people.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.people.data.model.CustomField
import com.example.people.data.model.Person
import com.example.people.ui.PeopleViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonScreen(
    viewModel: PeopleViewModel,
    personId: Int?, // Pass null for adding, or person ID for editing
    onBackClick: () -> Unit,
    onComplete: (Int) -> Unit
) {
    val context = LocalContext.current
    val details by viewModel.selectedPersonDetails.collectAsState()

    var name by remember { mutableStateOf("") }
    var howWeMet by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    
    // Dynamic Key-Value list
    val customFields = remember { mutableStateListOf<Pair<String, String>>() }

    // Load existing profile details if editing
    LaunchedEffect(personId) {
        if (personId != null) {
            viewModel.selectPerson(personId)
        } else {
            viewModel.selectPerson(null)
            name = ""
            howWeMet = ""
            notes = ""
            photoPath = null
            customFields.clear()
        }
    }

    // Populate data when details load
    LaunchedEffect(details) {
        val currentDetails = details
        if (personId != null && currentDetails != null && currentDetails.person.id == personId) {
            name = currentDetails.person.name
            howWeMet = currentDetails.person.howWeMet
            notes = currentDetails.person.notes
            photoPath = currentDetails.person.photoPath
            customFields.clear()
            currentDetails.customFields.forEach {
                customFields.add(Pair(it.key, it.value))
            }
        }
    }

    // Camera launcher (returns Bitmap directly)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val path = saveBitmapToInternalStorage(context, bitmap)
            if (path != null) {
                photoPath = path
            }
        }
    }

    // Gallery launcher (returns Uri)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = viewModel.saveImageToInternalStorage(context, uri)
            if (path != null) {
                photoPath = path
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (personId == null) "Add Person" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val person = Person(
                                    id = personId ?: 0,
                                    name = name.trim(),
                                    howWeMet = howWeMet.trim(),
                                    notes = notes.trim(),
                                    photoPath = photoPath,
                                    createdAt = details?.person?.createdAt ?: System.currentTimeMillis()
                                )
                                val fields = customFields.map {
                                    CustomField(personId = personId ?: 0, key = it.first.trim(), value = it.second.trim())
                                }.filter { it.key.isNotBlank() || it.value.isNotBlank() }

                                viewModel.savePerson(person, fields) { savedId ->
                                    onComplete(savedId)
                                }
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Picker Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(120.dp)
                    ) {
                        ProfileImage(
                            photoPath = photoPath,
                            name = name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch() },
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery")
                        }
                    }
                }
            }

            // Input Fields
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = howWeMet,
                    onValueChange = { howWeMet = it },
                    label = { Text("How / Where We Met") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("General Notes") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Custom Key-Values Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Key Attributes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { customFields.add(Pair("", "")) }) {
                        Text("+ Add Field")
                    }
                }
            }

            // Dynamic Key-Value Inputs
            itemsIndexed(customFields) { index, field ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = field.first,
                        onValueChange = { newKey ->
                            customFields[index] = Pair(newKey, field.second)
                        },
                        placeholder = { Text("Label (e.g. Birthday)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = field.second,
                        onValueChange = { newValue ->
                            customFields[index] = Pair(field.first, newValue)
                        },
                        placeholder = { Text("Value") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = { customFields.removeAt(index) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete field")
                    }
                }
            }
        }
    }
}

// Helper to save camera bitmap directly to internal storage
private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
    return try {
        val fileName = "profile_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
