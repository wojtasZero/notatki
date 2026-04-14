    package com.server.notatki

    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalClipboard
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.window.Dialog
    import com.server.notatki.ui.theme.AppTheme
    import kotlinx.coroutines.launch
    import io.ktor.client.*
    import io.ktor.client.request.*
    import io.ktor.client.statement.*
    import io.ktor.http.*
    import io.ktor.client.plugins.websocket.*
    import io.ktor.websocket.*
    import kotlinx.coroutines.flow.MutableSharedFlow

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
        val clipboard = LocalClipboard.current
        @Suppress("LocalVariableName") var SERVER_URL by remember { mutableStateOf("http://192.168.0.2:5000") }
        var userId by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("") }

        var shareId by remember { mutableStateOf("") }
        var isShareDialogOpen by remember { mutableStateOf(false) }
        var isSetupDialogOpen by remember { mutableStateOf(true) }

        var serverUrlInput by remember { mutableStateOf(SERVER_URL) }

        val scope = rememberCoroutineScope()
        val client = remember { HttpClient{
            install(WebSockets)
        } }

        //val latestNotification = MutableStateFlow<String?>(null)
        val outgoingTextFlow = remember { MutableSharedFlow<String>(extraBufferCapacity = 10) }
        fun connectToWebsocket() {
            scope.launch {
                try {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = SERVER_URL.substringAfter("//").substringBefore(":"),
                        port = 8000,
                        path = "/ws/notifications"
                    ) {
                        val sendJob = launch {
                            outgoingTextFlow.collect { newText ->
                                send(newText)
                            }
                        }

                        val receiveJob = launch {
                            for (frame in incoming) {
                                frame as? Frame.Text ?: continue
                                val textReceived = frame.readText()

                                if (noteContent != textReceived) {
                                    noteContent = textReceived
                                }
                            }
                        }

                        sendJob.join()
                        receiveJob.join()
                    }
                } catch (e: Exception) {
                    println("Błąd połączenia: ${e.message}")
                }
            }
        }

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
                    onValueChange = { newText ->
                        noteContent = newText

                        scope.launch {
                            outgoingTextFlow.emit(newText)
                        }
                    },
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
                    //Dialog {

                    //}
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(statusMessage, modifier = Modifier.padding(8.dp))
                    }
                }
            }

            if (isShareDialogOpen) {
                Dialog(onDismissRequest = {isShareDialogOpen = false}) {
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
            Dialog(onDismissRequest = {isSetupDialogOpen = false}) {
                LaunchedEffect(Unit) {
                    getClipboardText(clipboard)?.let { text ->
                        if (text.startsWith("http")) {
                            serverUrlInput = text
                        }
                    }
                }
                Column {
                    TextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Wpisz adres serwera") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    Button(onClick = { 
                        SERVER_URL = serverUrlInput
                        isSetupDialogOpen = false
                        connectToWebsocket() 
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }