package com.server.notatki

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.server.notatki.ui.theme.AppTheme
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

@Composable
fun App() {
    AppTheme {
        Scaffold { paddingValues ->
            NoteScreen(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NoteScreen(paddingValues: PaddingValues) {
    @Suppress("LocalVariableName") var SERVER_URL by remember { mutableStateOf("http://192.168.0.2:5000") }
    var userId by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    var shareId by remember { mutableStateOf("") }
    var isShareDialogOpen by remember { mutableStateOf(false) }
    var isSetupDialogOpen by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val client = remember { HttpClient() }

    fun request(doStuff: suspend () -> Unit) {
        scope.launch {
            try {
                doStuff()
            } catch (e: Exception) {
                statusMessage = "Wystąpił błąd: ${e.message}"
            }
        }
    }

    fun backup() {
        if (userId.isBlank()) {
            statusMessage = "Podaj swój nickname!"
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/backup") {
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("{\"user_id\": \"$userId\", \"content\": \"$noteContent\"}")
            }
            statusMessage = response.bodyAsText()
        }
    }

    fun load() {
        if (userId.isBlank()) {
            statusMessage = "Podaj swój nickname!"
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/backup/$userId") {
                method = HttpMethod.Get
            }
            noteContent = response.bodyAsText()
        }
    }

    fun share() {
        if (userId.isBlank()) {
            statusMessage = "Podaj swój nickname!"
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/share") {
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("{\"user_id\": \"$userId\", \"content\": \"$noteContent\"}")
            }
            statusMessage = response.bodyAsText()
        }
    }

    fun loadShared() {
        if (shareId.isBlank()) {
            statusMessage = "Podaj ID udostępnienia!"
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/share/$shareId") {
                method = HttpMethod.Get
            }
            noteContent = response.bodyAsText()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notatki", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(16.dp))
                TextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("Nickname") },
                    modifier = Modifier.fillMaxWidth()
                )}

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text("Wpisz tutaj swoją notatkę...") },
                modifier = Modifier.fillMaxWidth().weight(1F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { backup() }
                ) { Text("Zapisz") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { load() }
                ) { Text("Wczytaj") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { share() }
                ) { Text("Wyślij") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { isShareDialogOpen = true }
                ) { Text("Pobierz") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (statusMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(statusMessage, modifier = Modifier.padding(8.dp))
                }
            }
        }

        if (isShareDialogOpen) {
            Dialog({isShareDialogOpen = false}) {
               Column {
                    TextField(
                        value = shareId,
                        onValueChange = { shareId = it },
                        label = { Text("ID notatki") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    Button(onClick = { loadShared(); isShareDialogOpen = false }) {
                        Text("OK")
                    }
                }
            }
        }
    }

    if (isSetupDialogOpen) {
        Dialog({isSetupDialogOpen = false}) {
            Column {
                TextField(
                    value = SERVER_URL,
                    onValueChange = { SERVER_URL = it },
                    label = { Text("Wpisz adres serwera") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Button(onClick = { isSetupDialogOpen = false }) {
                    Text("OK")
                }
            }
        }
    }
}