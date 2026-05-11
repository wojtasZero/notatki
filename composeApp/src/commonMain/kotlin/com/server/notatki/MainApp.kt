@file:Suppress("UnusedImport")

package com.server.notatki

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import com.server.notatki.ui.theme.darkScheme
import com.server.notatki.ui.theme.lightScheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.encodeURLPathPart
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import kotlin.time.Clock

@Serializable
data class NoteUpdate(
    val username: String,
    val content: String
)

@Serializable
data class AuthRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val status: String, val message: String, val session_id: String? = null)

@Serializable
data class NoteRequest(
    val username: String,
    val session_id: String,
    val content: String
)

@Serializable
data class Note(
    val id: Long = 0,
    val content: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class Response(val status: String, val message: String)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val hazeState = rememberHazeState()

    // --- Configuration State ---
    var serverUrl by remember { mutableStateOf("http://192.168.0.2:8000") }
    var username by remember { mutableStateOf("Gość") }
    var password by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf("") }
    var shareId by remember { mutableStateOf("") }

    // --- Editor State ---
    var noteContent by remember { mutableStateOf(TextFieldValue("")) }
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    var fontSize by remember { mutableStateOf(20) }
    var wrapText by remember { mutableStateOf(true) }
    var showToolbar by remember { mutableStateOf(true) }
    var lastEditor by remember { mutableStateOf<String?>(null) }

    // --- UI Navigation/Dialog State ---
    var currentScreen by remember { mutableStateOf(0) } // 0: Editor, 1: Saved Notes
    var isSetupDialogOpen by remember { mutableStateOf(true) }
    var isInfoDialogOpen by remember { mutableStateOf(false) }
    var infoText by remember { mutableStateOf("") }

    // --- Network State ---
    val client = remember {
        HttpClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }
        }
    }
    var websocketJob by remember { mutableStateOf<Job?>(null) }
    val outgoingTextFlow = remember { MutableSharedFlow<String>(extraBufferCapacity = 10) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastJob by remember { mutableStateOf<Job?>(null) }
    fun toast(txt: String) {
        toastJob?.cancel()
        toastJob = scope.launch {
            toastMessage = txt
            delay(2000)
            toastMessage = null
        }
    }

    // --- Helper Functions ---
    fun onNoteChange(newValue: TextFieldValue) {
        if (newValue.text != noteContent.text) {
            undoStack.add(noteContent)
            redoStack.clear()
            if (undoStack.size > 50) undoStack.removeAt(0)
            scope.launch { outgoingTextFlow.emit(newValue.text) }
        }
        noteContent = newValue
    }

    suspend fun saveNote() {
        if (username == "Gość" || sessionId.isBlank()) {
            toast("Najpierw się zaloguj!")
            isSetupDialogOpen = true
            return
        }
        try {
            val response: HttpResponse = client.post("$serverUrl/note") {
                contentType(ContentType.Application.Json)
                setBody(NoteRequest(username, sessionId, noteContent.text))
            }
            val obj = Json.decodeFromString<Response>(response.bodyAsText())
            toast(obj.message)
        } catch (e: Exception) {
            toast("Błąd zapisu: ${e.message}")
        }
    }

    suspend fun loadNotes(): List<Note>? {
        if (username == "Gość" || sessionId.isBlank()) {
            toast("Najpierw się zaloguj!")
            isSetupDialogOpen = true
            return null
        }
        val encodedUsername = username.encodeURLPath()
        return try {
            client.get("$serverUrl/notes/$encodedUsername") {
                header("X-Session-ID", sessionId)
            }.body<List<Note>>()
        } catch (e: Exception) {
            toast("Błąd ładowania: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteNote(note: Note) {
        try {
            client.delete("$serverUrl/note/${note.id}") {
                header("username", username)
                header("X-Session-ID", sessionId)
            }
        } catch (e: Exception) {
            toast("Błąd usuwania: ${e.message}")
        }
    }

    fun connectToWebsocket(sid: String) {
        websocketJob?.cancel()
        if (sid.isBlank()) return
        toast("Łączenie z serwerem...")
        websocketJob = scope.launch {
            try {
                val host = serverUrl.substringAfter("//").substringBefore(":")
                client.webSocket(method = HttpMethod.Get, host = host, port = 8000, path = "/ws/notatki/$sid") {
                    val sendJob = launch {
                        outgoingTextFlow.collect { newText ->
                            val jsonString = Json.encodeToString(NoteUpdate(username, newText))
                            send(jsonString)
                            lastEditor = username
                        }
                    }
                    val receiveJob = launch {
                        for (frame in incoming) {
                            (frame as? Frame.Text)?.let {
                                try {
                                    val update = Json.decodeFromString<NoteUpdate>(it.readText())
                                    if (noteContent.text != update.content) {
                                        noteContent = TextFieldValue(update.content, TextRange(update.content.length))
                                        lastEditor = update.username
                                    }
                                } catch (e: Exception) {
                                    toast("Błąd synchronizacji: ${e.message}")
                                }
                            }
                        }
                    }
                    sendJob.join()
                    receiveJob.join()
                }
            } catch (e: Exception) {
                toast("Błąd połączenia: ${e.message}")
            }
        }
    }

    fun showInfo(txt: String) {
        val characters = txt.length
        val charactersNoSpaces = txt.count { !it.isWhitespace() }
        val words = txt.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val sentences = txt.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size
        val lines = if (txt.isEmpty()) 0 else txt.lines().size

        infoText = "Znaki: $characters\n" +
                "Znaki (bez spacji): $charactersNoSpaces\n" +
                "Słowa: $words\n" +
                "Zdania: $sentences\n" +
                "Linijki: $lines"
        isInfoDialogOpen = true
    }

    // --- UI Structure ---
    MaterialExpressiveTheme(colorScheme = if (isSystemInDarkTheme()) darkScheme else lightScheme) {
        Scaffold {
            Box(Modifier.fillMaxSize()) {
                EditorScreen(
                    noteContent = noteContent,
                    onNoteChange = ::onNoteChange,
                    fontSize = fontSize,
                    wrapText = wrapText,
                    showToolbar = showToolbar,
                    lastEditor = lastEditor,
                    hazeState = hazeState,
                    undoEnabled = undoStack.isNotEmpty(),
                    redoEnabled = redoStack.isNotEmpty(),
                    onUndo = {
                        if (undoStack.isNotEmpty()) {
                            redoStack.add(noteContent)
                            val prev = undoStack.removeAt(undoStack.size - 1)
                            noteContent = prev
                            scope.launch { outgoingTextFlow.emit(prev.text) }
                        }
                    },
                    onRedo = {
                        if (redoStack.isNotEmpty()) {
                            undoStack.add(noteContent)
                            val next = redoStack.removeAt(redoStack.size - 1)
                            noteContent = next
                            scope.launch { outgoingTextFlow.emit(next.text) }
                        }
                    },
                    onCopy = {
                        val selected = noteContent.text.substring(noteContent.selection.min, noteContent.selection.max)
                        if (selected.isNotEmpty()) scope.launch { setClipboardText(clipboard, selected) }
                    },
                    onCut = {
                        val selected = noteContent.text.substring(noteContent.selection.min, noteContent.selection.max)
                        if (selected.isNotEmpty()) {
                            scope.launch {
                                setClipboardText(clipboard, selected)
                                val newText = noteContent.text.removeRange(noteContent.selection.min, noteContent.selection.max)
                                onNoteChange(TextFieldValue(newText, TextRange(noteContent.selection.min)))
                            }
                        }
                    },
                    onPaste = {
                        scope.launch {
                            getClipboardText(clipboard)?.let { textToPaste ->
                                val newText = noteContent.text.replaceRange(noteContent.selection.min, noteContent.selection.max, textToPaste)
                                onNoteChange(TextFieldValue(newText, TextRange(noteContent.selection.min + textToPaste.length)))
                            }
                        }
                    },
                    onToggleToolbar = { showToolbar = !showToolbar },
                    onFontSizeChange = { fontSize = it },
                    onToggleWrap = { wrapText = !wrapText },
                    onSettings = { isSetupDialogOpen = true },
                    onInfo = { showInfo(noteContent.text) },
                    onDelete = {
                        undoStack.add(noteContent)
                        noteContent = TextFieldValue("")
                        scope.launch { outgoingTextFlow.emit("") }
                    },
                    onSave = { scope.launch { saveNote() } },
                    onOpenNotes = { currentScreen = 1 }
                )

                AnimatedVisibility(currentScreen == 1, enter = fadeIn(), exit = fadeOut()) {
                    SavedNotesScreen(
                        loadNotes = ::loadNotes,
                        deleteNote = ::deleteNote,
                        onNoteSelected = { note ->
                            undoStack.add(noteContent)
                            noteContent = TextFieldValue(note.content)
                            currentScreen = 0
                        },
                        onClose = { currentScreen = 0 },
                        hazeState = hazeState
                    )
                }

                if (isSetupDialogOpen) {
                    SetupDialog(
                        initialUrl = serverUrl,
                        initialUsername = username,
                        initialPassword = password,
                        initialShareId = shareId,
                        clipboard = clipboard,
                        onDismiss = { isSetupDialogOpen = false },
                        onLogin = { url, user, pass, sid ->
                            scope.launch {
                                try {
                                    val response: HttpResponse = client.post("$url/login") {
                                        contentType(ContentType.Application.Json)
                                        setBody(AuthRequest(user, pass))
                                    }
                                    if (response.status.value == 200) {
                                        val loginResp = Json.decodeFromString<LoginResponse>(response.bodyAsText())
                                        sessionId = loginResp.session_id ?: ""
                                        serverUrl = url
                                        username = user
                                        password = pass
                                        shareId = sid
                                        isSetupDialogOpen = false
                                        toast(loginResp.message)
                                        connectToWebsocket(sid)
                                    } else {
                                        val err = Json.decodeFromString<Response>(response.bodyAsText())
                                        toast(err.message)
                                    }
                                } catch (e: Exception) {
                                    toast("Błąd logowania: ${e.message}")
                                }
                            }
                        },
                        onRegister = { url, user, pass ->
                            scope.launch {
                                try {
                                    val response: HttpResponse = client.post("$url/register") {
                                        contentType(ContentType.Application.Json)
                                        setBody(AuthRequest(user, pass))
                                    }
                                    val res = Json.decodeFromString<Response>(response.bodyAsText())
                                    toast(res.message)
                                } catch (e: Exception) {
                                    toast("Błąd rejestracji: ${e.message}")
                                }
                            }
                        },
                        toast = { Toast(toastMessage, hazeState) }
                    )
                }
                Toast(toastMessage, hazeState)

                if (isInfoDialogOpen) {
                    InfoDialog(infoText = infoText, onDismiss = { isInfoDialogOpen = false })
                }
            }
        }
    }
}

@Composable
private fun Toast(message: String?, hazeState: HazeState) {
    val lastMessage = remember { mutableStateOf("") }
    if (message != null) lastMessage.value = message

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleIn(initialScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleOut(targetScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        ) {
            Box(
                Modifier
                    .padding(32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.background,
                            blurRadius = 8.dp,
                            tint = HazeTint(Color.Black.copy(alpha = 0.3f))
                        )
                    )
            ) {
                Text(
                    lastMessage.value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditorScreen(
    noteContent: TextFieldValue,
    onNoteChange: (TextFieldValue) -> Unit,
    fontSize: Int,
    wrapText: Boolean,
    showToolbar: Boolean,
    lastEditor: String?,
    hazeState: HazeState,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onToggleToolbar: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onToggleWrap: () -> Unit,
    onSettings: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val minTextFieldHeight = maxHeight - 200.dp
        val isVertical = maxWidth < maxHeight

        Column(Modifier.hazeSource(hazeState).verticalScroll(rememberScrollState()).imePadding()) {
            Gradient()
            val baseModifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = minTextFieldHeight).bringIntoViewRequester(bringIntoViewRequester)
            BasicTextField(
                value = noteContent,
                onValueChange = onNoteChange,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = fontSize.sp),
                onTextLayout = {
                    val cursorRect = it.getCursorRect(noteContent.selection.start)
                    scope.launch { bringIntoViewRequester.bringIntoView(cursorRect) }
                },
                modifier = if (wrapText) baseModifier else baseModifier.horizontalScroll(rememberScrollState()),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            )
            Gradient(isReversed = true)
        }

        var menuExp by remember { mutableStateOf(0) } // 0: None, 1: Right, 2: Left

        BottomToolbars(
            showToolbar = showToolbar,
            isVertical = isVertical,
            hazeState = hazeState,
            undoEnabled = undoEnabled,
            redoEnabled = redoEnabled,
            onUndo = onUndo,
            onRedo = onRedo,
            onCopy = onCopy,
            onCut = onCut,
            onPaste = onPaste,
            menuExp = menuExp,
            onMenuExpChange = { menuExp = it },
            fontSize = fontSize,
            onFontSizeChange = onFontSizeChange,
            wrapText = wrapText,
            onToggleWrap = onToggleWrap,
            onToggleToolbar = onToggleToolbar,
            onSettings = onSettings,
            onInfo = onInfo,
            onDelete = onDelete,
            onSave = onSave,
            onOpenNotes = onOpenNotes,
            noteText = noteContent.text
        )

        LastEditor(lastEditor, hazeState)
        StatusBarProtection(hazeState)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomToolbars(
    showToolbar: Boolean,
    isVertical: Boolean,
    hazeState: HazeState,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    menuExp: Int,
    onMenuExpChange: (Int) -> Unit,
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    wrapText: Boolean,
    onToggleWrap: () -> Unit,
    onToggleToolbar: () -> Unit,
    onSettings: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onOpenNotes: () -> Unit,
    noteText: String
) {
    Box(Modifier.fillMaxSize()) {
        val toolbarHeight = animateDpAsState(if (menuExp == 0) 60.dp else 50.dp, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))

        AnimatedVisibility(
            visible = showToolbar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.5f), initialOffsetY = { it }),
            exit = slideOutVertically(spring(), targetOffsetY = { it })
        ) {
            HorizontalFloatingToolbar(
                expanded = isVertical,
                modifier = Modifier.safeDrawingPadding().align(Alignment.BottomCenter).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background).height(toolbarHeight.value)
                    .hazeEffect(state = hazeState) {
                        blurEnabled = true
                        blurRadius = 10.dp
                    },
                colors = FloatingToolbarColors(
                    toolbarContainerColor = Color.Transparent,
                    toolbarContentColor = MaterialTheme.colorScheme.onBackground,
                    fabContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    fabContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                IconBtn(Icons.AutoMirrored.Filled.Undo, "Cofnij", Modifier.alpha(if (undoEnabled) 1f else 0.5f), onUndo)
                IconBtn(Icons.AutoMirrored.Filled.Redo, "Ponów", Modifier.alpha(if (redoEnabled) 1f else 0.5f), onRedo)
                IconBtn(Icons.Default.ContentCopy, "Kopiuj", onClick = onCopy)
                IconBtn(Icons.Default.ContentCut, "Wytnij", onClick = onCut)
                IconBtn(Icons.Default.ContentPaste, "Wklej", onClick = onPaste)
            }
        }

        // Left Menu
        FloatingActionButtonMenu(
            expanded = menuExp == 2,
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalAlignment = Alignment.Start,
            button = {
                ToggleFloatingActionButton(
                    checked = menuExp == 2,
                    onCheckedChange = { onMenuExpChange(if (it) 2 else 0) },
                    containerSize = { 48.dp }
                ) {
                    Icon(if (menuExp == 2) Icons.Rounded.Close else Icons.Rounded.MoreVert, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        ) {
            var showSizeLabel by remember { mutableStateOf(false) }
            FloatingActionButtonMenuItem({ onToggleToolbar() }, { Text("Pasek narzędzi"); Switch(showToolbar, { onToggleToolbar() }) }, { Icon(Icons.Rounded.Apps, null) })
            FloatingActionButtonMenuItem({}, {
                Text("Rozmiar czcionki")
                Slider(fontSize.toFloat(), { onFontSizeChange(it.roundToInt()); showSizeLabel = true }, valueRange = 10f..50f, steps = 19, modifier = Modifier.width(200.dp))
            }, { if (showSizeLabel) Text(fontSize.toString(), fontFamily = FontFamily.Monospace) else Icon(Icons.Rounded.FormatSize, null) })
            FloatingActionButtonMenuItem(onToggleWrap, { Text("Zawijanie tekstu"); Switch(wrapText, { onToggleWrap() }) }, { Icon(Icons.AutoMirrored.Rounded.WrapText, null) })
            AnimatedVisibility(visible = !showToolbar) {
                FloatingActionButtonMenuItem(onRedo, { Text("Ponów") }, { Icon(Icons.AutoMirrored.Rounded.Redo, null) }, modifier = Modifier.alpha(if (redoEnabled) 1f else 0.5f))
            }
            AnimatedVisibility(visible = !showToolbar) {
                FloatingActionButtonMenuItem(onUndo, { Text("Cofnij") }, { Icon(Icons.AutoMirrored.Rounded.Undo, null) }, modifier = Modifier.alpha(if (undoEnabled) 1f else 0.5f))
            }
        }

        // Right Menu
        FloatingActionButtonMenu(
            expanded = menuExp == 1,
            modifier = Modifier.align(Alignment.BottomEnd).imePadding(),
            horizontalAlignment = Alignment.End,
            button = {
                ToggleFloatingActionButton(
                    checked = menuExp == 1,
                    onCheckedChange = { onMenuExpChange(if (it) 1 else 0) },
                    containerSize = { 48.dp }
                ) {
                    Icon(if (menuExp == 1) Icons.AutoMirrored.Rounded.MenuOpen else Icons.Rounded.Menu, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        ) {
            val uri = LocalUriHandler.current
            FloatingActionButtonMenuItem({ onSettings(); onMenuExpChange(0) }, { Text("Ustawienia") }, { Icon(Icons.Rounded.Settings, null) })
            FloatingActionButtonMenuItem({ onInfo(); onMenuExpChange(0) }, { Text("Informacje") }, { Icon(Icons.Rounded.Info, null) })
            FloatingActionButtonMenuItem({ uri.openUri("https://translate.google.com/?text=${noteText.encodeURLPathPart()}"); onMenuExpChange(0) }, { Text("Tłumacz") }, { Icon(Icons.Rounded.Translate, null) })
            FloatingActionButtonMenuItem({ onDelete(); onMenuExpChange(0) }, { Text("Usuń") }, { Icon(Icons.Rounded.Close, null) })
            FloatingActionButtonMenuItem({ onSave(); onMenuExpChange(0) }, { Text("Zapisz") }, { Icon(Icons.Rounded.Save, null) })
            FloatingActionButtonMenuItem({ onOpenNotes(); onMenuExpChange(0) }, { Text("Otwórz") }, { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null) })
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SavedNotesScreen(
    loadNotes: suspend () -> List<Note>?,
    deleteNote: suspend (Note) -> Unit,
    onNoteSelected: (Note) -> Unit,
    onClose: () -> Unit,
    hazeState: HazeState
) {
    var localNotes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        isLoading = true
        val result = loadNotes()
        if (result == null) {
            onClose()
            return@LaunchedEffect
        }
        localNotes = result
        isLoading = false
    }

    Box(
        Modifier.fillMaxSize().clickable { onClose() }
            .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = MaterialTheme.colorScheme.surface, tint = HazeTint(Color.Black.copy(alpha = 0.2f)), blurRadius = 12.dp))
    ) {
        when {
            isLoading -> LoadingIndicator(Modifier.align(Alignment.Center))
            localNotes.isEmpty() -> {
                Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Pusta lista", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    Text("Użyj przycisku 'Zapisz', aby dodać notatkę", textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = WindowInsets.safeContent.asPaddingValues()) {
                    items(localNotes) { note ->
                        NoteItem(note, onSelect = { onNoteSelected(note) }, onDelete = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                deleteNote(note)
                                loadNotes()?.let { localNotes = it }
                            }
                        })
                    }
                }
            }
        }
        Button(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp), onClick = onClose) { Text("Zamknij") }
    }
}

@Composable
private fun NoteItem(note: Note, onSelect: () -> Unit, onDelete: () -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(
            text = note.content.trim().replace(Regex("(\\r?\\n)+"), "\n"),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(onClick = onSelect, onLongClick = onDelete).padding(12.dp),
            maxLines = 5,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val timestamp = Instant.fromEpochMilliseconds(note.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
        Text("${timestamp.date} ${timestamp.hour}:${timestamp.minute}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun SetupDialog(
    initialUrl: String,
    initialUsername: String,
    initialPassword: String,
    initialShareId: String,
    clipboard: Clipboard,
    onDismiss: () -> Unit,
    onLogin: (String, String, String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    toast: @Composable () -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var user by remember { mutableStateOf(initialUsername) }
    var pass by remember { mutableStateOf(initialPassword) }
    var sid by remember { mutableStateOf(initialShareId) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(24.dp).align(Alignment.Center)) {
                Text("Ustawienia połączenia", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                Box {
                    TextField(value = url, onValueChange = { url = it }, label = { Text("Adres serwera") }, modifier = Modifier.fillMaxWidth())
                    Icon(Icons.Default.ContentPaste, null, Modifier.align(Alignment.CenterEnd).clip(CircleShape).clickable {
                        scope.launch { getClipboardText(clipboard)?.let { url = it } }
                    }.padding(12.dp))
                }
                Spacer(Modifier.height(8.dp))
                TextField(value = user, onValueChange = { user = it }, label = { Text("Użytkownik") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                TextField(value = pass, onValueChange = { pass = it }, label = { Text("Hasło") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                TextField(value = sid, onValueChange = { sid = it }, label = { Text("ID sesji (WebSocket)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onRegister(url, user, pass) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(bottomStart = 16.dp)) { Text("Zarejestruj") }
                    Button(onClick = { onLogin(url, user, pass, sid) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(bottomEnd = 16.dp)) { Text("Zaloguj") }
                }
            }
            toast.invoke()
        }
    }
}

@Composable
private fun InfoDialog(infoText: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(24.dp)) {
            Text("Informacje o tekście", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
            Text(infoText, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Zamknij") }
        }
    }
}

@Composable
private fun StatusBarProtection(hazeState: HazeState) {
    val density = LocalDensity.current
    val topInset = WindowInsets.statusBars.getTop(density)
    val color = MaterialTheme.colorScheme.background
    Box(Modifier.fillMaxWidth().height(with(density) { (topInset * 1.2f).toDp() }).hazeEffect(hazeState) {
        progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
        style = HazeStyle(
            backgroundColor = color,
            tint = HazeTint(Color.Transparent),
            blurRadius = 12.dp
        )
    })
}

@Composable
private fun Gradient(isReversed: Boolean = false) {
    Box(Modifier.fillMaxWidth().height(100.dp).background(Brush.verticalGradient(
        if (isReversed) listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onPrimary)
        else listOf(MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.background)
    )))
}

@Composable
private fun LastEditor(lastEditor: String?, hazeState: HazeState) {
    AnimatedVisibility(visible = lastEditor != null) {
        Box(Modifier.safeContentPadding().padding(8.dp).clip(RoundedCornerShape(16.dp))
            .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = MaterialTheme.colorScheme.background, blurRadius = 4.dp, tint = HazeTint(Color.Black.copy(alpha = 0.2f))))) {
            Text("Ostatnia edycja: $lastEditor", style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.primary), modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun IconBtn(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Icon(imageVector, contentDescription, modifier.clip(CircleShape).clickable { onClick() }.padding(8.dp))
}