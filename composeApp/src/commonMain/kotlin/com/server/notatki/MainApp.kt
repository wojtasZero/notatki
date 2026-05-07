@file:Suppress("UnusedImport")

package com.server.notatki

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.server.notatki.data.Note
import com.server.notatki.data.Settings
import com.server.notatki.data.getDatabase
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
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.encodeURLPathPart
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import kotlin.time.Instant

@Serializable
data class NoteUpdate(
    val username: String,
    val content: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val snackbarHost = remember { SnackbarHostState() }

    val database = remember { getDatabase() }
    val noteDao = remember { database.noteDao() }
    val settingsDao = remember { database.settingsDao() }

    val localNotes by noteDao.getAllNotes().collectAsState(initial = emptyList())
    val settingsState by settingsDao.getSettings().collectAsState(initial = null)

    val clipboard = LocalClipboard.current
    @Suppress("LocalVariableName") var SERVER_URL by remember { mutableStateOf("http://192.168.0.2:5000") }
    var userId by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf(TextFieldValue("")) }
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    var shareId by remember { mutableStateOf("") }
    var isSetupDialogOpen by remember { mutableStateOf(false) }
    var isInfoDialogOpen by remember { mutableStateOf(false) }
    var infoText by remember { mutableStateOf("") }

    var serverUrlInput by remember { mutableStateOf(SERVER_URL) }


    val scope = rememberCoroutineScope()
    val client = remember {
        HttpClient {
            install(WebSockets)
        }
    }

    var websocketJob by remember { mutableStateOf<Job?>(null) }

    var lastEditor by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("Gość") }
    val outgoingTextFlow = remember { MutableSharedFlow<String>(extraBufferCapacity = 10) }
    fun connectToWebsocket(shareId: String) {
        websocketJob?.cancel()
        if (shareId.isBlank()) return
        scope.launch { snackbarHost.showSnackbar("Łączenie z serwerem") }
        websocketJob = scope.launch {
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = SERVER_URL.substringAfter("//").substringBefore(":"),
                    port = 8000,
                    path = "/ws/notatki/$shareId"
                ) {
                    val sendJob = launch {
                        outgoingTextFlow.collect { newText ->
                            val payload = NoteUpdate(username = username, content = newText)
                            val jsonString = Json.encodeToString(payload)
                            send(jsonString)
                            lastEditor = username
                        }
                    }

                    val receiveJob = launch {
                        for (frame in incoming) {
                            frame as? Frame.Text ?: continue
                            val textReceived = frame.readText()

                            try {
                                val update = Json.decodeFromString<NoteUpdate>(textReceived)

                                if (noteContent.text != update.content) {
                                    noteContent = TextFieldValue(update.content)
                                    lastEditor = update.username
                                }
                            } catch (e: Exception) {
                                snackbarHost.showSnackbar(e.toString())
                            }
                        }
                    }

                    sendJob.join()
                    receiveJob.join()
                }
            } catch (e: Exception) {
                snackbarHost.showSnackbar("Błąd połączenia: ${e.message}")
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

    fun request(doStuff: suspend () -> Unit) {
        scope.launch {
            try {
                doStuff()
            } catch (e: Exception) {
                snackbarHost.showSnackbar("Wystąpił błąd: ${e.message}")
            }
        }
    }

    fun backup() {
        if (userId.isBlank()) {
            scope.launch { snackbarHost.showSnackbar("Podaj swój nickname!") }
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/backup") {
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("{\"user_id\": \"$userId\", \"content\": \"${noteContent.text}\"}")
            }
            scope.launch { snackbarHost.showSnackbar(response.bodyAsText()) }
        }
    }

    fun load() {
        if (userId.isBlank()) {
            scope.launch { snackbarHost.showSnackbar("Podaj swój nickname!") }
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/backup/$userId") {
                method = HttpMethod.Get
            }
            noteContent = TextFieldValue(response.bodyAsText())
        }
    }

    fun share() {
        if (userId.isBlank()) {
            scope.launch { snackbarHost.showSnackbar("Podaj swój nickname!") }
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/share") {
                method = HttpMethod.Post
                header("Content-Type", "application/json")
                setBody("{\"user_id\": \"$userId\", \"content\": \"${noteContent.text}\"}")
            }
            scope.launch { snackbarHost.showSnackbar(response.bodyAsText()) }
        }
    }

    /*fun loadShared() {
        if (shareId.isBlank()) {
            statusMessage = "Podaj ID udostępnienia!"
            return
        }
        request {
            val response: HttpResponse = client.request("$SERVER_URL/share/$shareId") {
                method = HttpMethod.Get
            }
            noteContent.text = TextFieldValue(response.bodyAsText()

        }
    }*/


    var showToolbar by remember { mutableStateOf(true) }
    var fontSize by remember { mutableStateOf(20) }
    var wrapText by remember { mutableStateOf(true) }
    var hasLoadedContent by remember { mutableStateOf(false) }

    LaunchedEffect(settingsState) {
        settingsState?.let {
            showToolbar = it.showToolbar
            fontSize = it.fontSize
            wrapText = it.wrapText
            if (!hasLoadedContent) {
                noteContent = TextFieldValue(it.lastContent)
                hasLoadedContent = true
            }
        }
    }

    fun saveSettings() {
        scope.launch {
            settingsDao.saveSettings(
                Settings(
                    showToolbar = showToolbar,
                    fontSize = fontSize,
                    wrapText = wrapText,
                    lastContent = noteContent.text
                )
            )
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            if (hasLoadedContent) {
                saveSettings()
            }
        }
    }

    //var exp by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(0) }

    val darkTheme = isSystemInDarkTheme()

    MaterialExpressiveTheme(colorScheme = if (darkTheme) darkScheme else lightScheme) {
        Scaffold()
        {
            val hazeState = rememberHazeState()
            val bringIntoViewRequester = remember { BringIntoViewRequester() }



            var textFieldValue1 by remember { mutableStateOf(TextFieldValue()) }

            BoxWithConstraints {
                val screenHeight = maxHeight
                val minTextFieldHeight = screenHeight - 200.dp


                Column(modifier = Modifier.hazeSource(hazeState).verticalScroll(rememberScrollState()).imePadding())
                {
                    Gradient()
                    val baseTextFieldModifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = minTextFieldHeight)
                        .bringIntoViewRequester(bringIntoViewRequester)
                    BasicTextField(
                        value = noteContent.text,
                        onValueChange = { newValue ->
                            if (newValue != noteContent.text) {
                                undoStack.add(noteContent)
                                redoStack.clear()
                                if (undoStack.size > 50) undoStack.removeAt(0)
                                scope.launch { outgoingTextFlow.emit(newValue) }
                            }
                            noteContent = TextFieldValue(newValue)
                        },
                        //value = textFieldValue1,
                        //onValueChange = { textFieldValue1 = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = fontSize.sp
                        ),
                        onTextLayout = {
                            val cursorRect = it.getCursorRect(textFieldValue1.selection.start) // Get Text Field 1 cursor position
                            scope.launch {
                                //delay(500)
                                bringIntoViewRequester.bringIntoView(cursorRect) // Scroll to Text Field 1 cursor position
                            }
                        },
                        modifier = if (wrapText) {
                            baseTextFieldModifier
                        } else {
                            baseTextFieldModifier.horizontalScroll(rememberScrollState())
                        },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                    )
                    Gradient(true)
                }



                var exp by remember { mutableStateOf(0) }
                val alpha = animateDpAsState(
                    if (exp == 0) 60.dp else 45.dp,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
                )
                BoxWithConstraints(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                    AnimatedVisibility(
                        showToolbar,
                        Modifier.align(Alignment.BottomCenter),//.graphicsLayer(scaleX = alpha.value, scaleY = alpha.value, transformOrigin = TransformOrigin(.5F, 1F)),
                        enter = slideInVertically(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow,
                                dampingRatio = .5F
                            ), initialOffsetY = { it }),
                        exit = slideOutVertically(
                            animationSpec = spring(),
                            targetOffsetY = { it })
                    )
                    {
                        HorizontalFloatingToolbar(
                            maxWidth < maxHeight,
                            //colors = FloatingToolbarDefaults.standardFloatingToolbarColors(Color.Transparent),
                            modifier = Modifier.safeDrawingPadding().align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .clip(RoundedCornerShape(16.dp)).height(alpha.value)
                                .clip(RoundedCornerShape(16.dp)).hazeEffect(state = hazeState)
                                {
                                    blurEnabled = true
                                    blurRadius = 10.dp
                                    //tints = listOf(
                                    //    HazeDefaults.tint(
                                    //        Color(0, 0, 0, 200)
                                    //    )
                                    //)
                                    //noiseFactor = .3f
                                }.clip(RoundedCornerShape(16.dp)),
                            colors = FloatingToolbarColors(
                                toolbarContainerColor = Color.Transparent,
                                toolbarContentColor = MaterialTheme.colorScheme.onBackground,
                                fabContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                fabContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        {
                            IconBtn(
                                Icons.AutoMirrored.Filled.Undo,
                                "Cofnij",
                                Modifier.alpha(if (undoStack.isNotEmpty()) 1F else .5F)
                            ) {
                                if (undoStack.isNotEmpty()) {
                                    redoStack.add(noteContent)
                                    noteContent = undoStack.removeAt(undoStack.size - 1)
                                    scope.launch { outgoingTextFlow.emit(noteContent.text) }
                                }
                            }

                            IconBtn(
                                Icons.AutoMirrored.Filled.Redo,
                                "Ponów",
                                Modifier.alpha(if (redoStack.isNotEmpty()) 1F else .5F)
                            ) {
                                if (redoStack.isNotEmpty()) {
                                    undoStack.add(noteContent)
                                    noteContent = redoStack.removeAt(redoStack.size - 1)
                                    scope.launch { outgoingTextFlow.emit(noteContent.text) }
                                }
                            }

                            IconBtn(Icons.Default.ContentCopy, "Kopiuj") {
                                val selected = noteContent.text.substring(
                                    noteContent.selection.min,
                                    noteContent.selection.max
                                )
                                if (selected.isNotEmpty()) {
                                    scope.launch {
                                        setClipboardText(clipboard, selected)
                                    }
                                }
                            }

                            IconBtn(Icons.Default.ContentCut, "Wytnij") {
                                val selected = noteContent.text.substring(
                                    noteContent.selection.min,
                                    noteContent.selection.max
                                )
                                if (selected.isNotEmpty()) {
                                    scope.launch {
                                        setClipboardText(clipboard, selected)
                                        val newText = noteContent.text.removeRange(
                                            noteContent.selection.min,
                                            noteContent.selection.max
                                        )
                                        undoStack.add(noteContent)
                                        noteContent = TextFieldValue(
                                            newText,
                                            TextRange(noteContent.selection.min)
                                        )
                                        outgoingTextFlow.emit(newText)
                                    }
                                }
                            }

                            IconBtn(Icons.Default.ContentPaste, "Wklej") {
                                scope.launch {
                                    val textToPaste = getClipboardText(clipboard) ?: ""
                                    if (textToPaste.isNotEmpty()) {
                                        val newText = noteContent.text.replaceRange(
                                            noteContent.selection.min,
                                            noteContent.selection.max,
                                            textToPaste
                                        )
                                        undoStack.add(noteContent)
                                        noteContent =
                                            TextFieldValue(
                                                newText,
                                                TextRange(noteContent.selection.min + textToPaste.length)
                                            )
                                        outgoingTextFlow.emit(newText)
                                    }
                                }
                            }
                        }
                    }

                    FloatingActionButtonMenu(
                        exp == 2,
                        modifier = Modifier.align(Alignment.BottomStart),
                        horizontalAlignment = Alignment.Start,
                        button = {
                            ToggleFloatingActionButton(
                                exp == 2,
                                { exp = if (exp == 2) 0 else 2 },
                                containerSize = { 48.dp }) {
                                if (exp == 2) Icon(
                                    Icons.Rounded.Close,
                                    "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                ) else Icon(
                                    Icons.Rounded.MoreVert,
                                    "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        })
                    {
                        var showSize by remember { mutableStateOf(false) }
                        FloatingActionButtonMenuItem(
                            { showToolbar = !showToolbar; saveSettings() },
                            {
                                Text("Pasek narzędzi"); Switch(
                                showToolbar,
                                { showToolbar = !showToolbar; saveSettings() })
                            },
                            { Icon(Icons.Rounded.Apps, contentDescription = null) })
                        FloatingActionButtonMenuItem(
                            {},
                            {
                                Text("Rozmiar czcionki"); Slider(
                                fontSize.toFloat(),
                                { fontSize = it.roundToInt(); showSize = true; saveSettings() },
                                valueRange = 10F..50F,
                                steps = 19,
                                modifier = Modifier.width(200.dp)
                            )
                            },
                            {
                                if (showSize) Text(
                                    fontSize.toString(),
                                    fontFamily = FontFamily.Monospace
                                ) else Icon(Icons.Rounded.FormatSize, contentDescription = null)
                            })
                        FloatingActionButtonMenuItem(
                            { wrapText = !wrapText; saveSettings() },
                            {
                                Text("Zawijanie tekstu"); Switch(
                                wrapText,
                                { wrapText = !wrapText; saveSettings() })
                            },
                            {
                                Icon(
                                    Icons.AutoMirrored.Rounded.WrapText,
                                    contentDescription = null
                                )
                            })
                        AnimatedVisibility(
                            !showToolbar,
                            modifier = Modifier.alpha(if (redoStack.isNotEmpty()) 1F else .5F)
                        ) {
                            FloatingActionButtonMenuItem(
                                {
                                    if (redoStack.isNotEmpty()) {
                                        undoStack.add(noteContent)
                                        noteContent = redoStack.removeAt(redoStack.size - 1)
                                        scope.launch { outgoingTextFlow.emit(noteContent.text) }
                                    }
                                },
                                { Text("Ponów") },
                                {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.Redo,
                                        contentDescription = null
                                    )
                                })
                        }
                        AnimatedVisibility(
                            !showToolbar,
                            modifier = Modifier.alpha(if (undoStack.isNotEmpty()) 1F else .5F)
                        ) {
                            FloatingActionButtonMenuItem(
                                {
                                    if (undoStack.isNotEmpty()) {
                                        redoStack.add(noteContent)
                                        noteContent = undoStack.removeAt(undoStack.size - 1)
                                        scope.launch { outgoingTextFlow.emit(noteContent.text) }
                                    }
                                },
                                { Text("Cofnij") },
                                {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.Undo,
                                        contentDescription = null
                                    )
                                })
                        }
                        //FloatingActionButtonMenuItem(
                        //    {},
                        //    { Text("History") },
                        //    { Icon(Icons.Rounded.History, contentDescription = null) })
                    }

                    FloatingActionButtonMenu(
                        exp == 1,
                        modifier = Modifier.align(Alignment.BottomEnd).imePadding(),
                        horizontalAlignment = Alignment.End,
                        button = {
                            ToggleFloatingActionButton(
                                exp == 1,
                                { exp = if (exp == 1) 0 else 1 },
                                containerSize = { 48.dp }) {
                                if (exp == 1) Icon(
                                    Icons.AutoMirrored.Rounded.MenuOpen,
                                    "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                ) else Icon(
                                    Icons.Rounded.Menu,
                                    "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        })
                    {
                        val uri = LocalUriHandler.current

                        FloatingActionButtonMenuItem(
                            { isSetupDialogOpen = true; exp = 0 },
                            { Text("Ustawienia") },
                            { Icon(Icons.Rounded.Settings, "Ustawienia") })

                        FloatingActionButtonMenuItem(
                            { showInfo(noteContent.text); exp = 0 },
                            { Text("Informacje") },
                            { Icon(Icons.Rounded.Info, "Informacje") })

                        FloatingActionButtonMenuItem({
                            uri.openUri("https://translate.google.com/?text=${noteContent.text.encodeURLPathPart()}"); exp =
                            0
                        }, { Text("Tłumacz") }, { Icon(Icons.Rounded.Translate, "Tłumacz") })

                        //FloatingActionButtonMenuItem({}, { Text("Zamień") }, { Icon(Icons.Rounded.FindReplace, "Zamień") })

                        //FloatingActionButtonMenuItem({}, { Text("Szukaj") }, { Icon(Icons.Rounded.Search, "Szukaj") })

                        //FloatingActionButtonMenuItem({}, { Text("Przypnij") }, { Icon(Icons.AutoMirrored.Rounded.CallReceived, "Przypnij", modifier = Modifier.graphicsLayer(scaleY = -1f, scaleX = -1f)) })

                        //FloatingActionButtonMenuItem({}, { Text("Udostępnij") }, { Icon(Icons.Rounded.Share, "Udostępnij") })

                        FloatingActionButtonMenuItem({
                            undoStack.add(noteContent); noteContent = TextFieldValue(""); exp = 0
                        }, { Text("Usuń") }, { Icon(Icons.Rounded.Close, "Usuń") })

                        FloatingActionButtonMenuItem({
                            if (noteContent.text.isNotBlank()) {
                                scope.launch {
                                    noteDao.insertNote(Note(content = noteContent.text))
                                }
                                exp = 0
                            }
                        }, { Text("Zapisz") }, { Icon(Icons.Rounded.Save, "Zapisz") })

                        FloatingActionButtonMenuItem(
                            {
                                currentScreen = 1; exp = 0
                            },
                            { Text("Otwórz") },
                            { Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Otwórz") })
                    }
                }
            }




            LastEditor(lastEditor, hazeState)

            StatusBarProtection(hazeState)

            AnimatedVisibility(currentScreen == 1, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier.fillMaxSize().clickable { currentScreen = 0 }.hazeEffect(
                        state = hazeState, style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            tint = HazeTint(Color.Black.copy(alpha = .2f)),
                            blurRadius = 12.dp
                        )
                    )
                ) {
                    if (localNotes.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        ) {
                            Text(
                                "Pusta lista",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp
                            )
                            Text(
                                "Użyj przycisku 'Zapisz', aby dodać notatkę do listy na później",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val haptics = LocalHapticFeedback.current
                        val safeContentPaddingValues = WindowInsets.safeContent.asPaddingValues()

                        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = safeContentPaddingValues) {
                            items(localNotes.size) { index ->
                                val note = localNotes[index]
                                //Spacer(Modifier.size(8.dp))
                                Text(
                                    note.content.trim().replace(Regex("(\\r?\\n)+"), "\n"),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .combinedClickable(
                                            onClick = {
                                                undoStack.add(noteContent)
                                                noteContent = TextFieldValue(note.content)
                                                currentScreen = 0
                                            },
                                            onLongClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                scope.launch { noteDao.deleteNote(note) }
                                            }
                                        ).padding(12.dp),
                                    maxLines = 5
                                )
                                val timestamp =
                                    Instant.fromEpochMilliseconds(note.timestamp).toLocalDateTime(
                                        TimeZone.currentSystemDefault()
                                    )
                                Text(
                                    "${timestamp.date} ${timestamp.hour}:${timestamp.minute}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Button(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp),
                        onClick = { currentScreen = 0 }) {
                        Text("Zamknij")
                    }
                }
            }

            if (isSetupDialogOpen) {
                Dialog(onDismissRequest = { isSetupDialogOpen = false }) {
                    Column(
                        Modifier.clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)
                    ) {
                        Box {
                            TextField(
                                value = serverUrlInput,
                                onValueChange = { serverUrlInput = it },
                                label = { Text("Adres serwera") },
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            Icon(
                                Icons.Default.ContentPaste,
                                "Wklej",
                                Modifier.clip(CircleShape).clickable {
                                    scope.launch {
                                        val textToPaste = getClipboardText(clipboard) ?: ""
                                        if (textToPaste.isNotEmpty()) {
                                            serverUrlInput = textToPaste
                                        }
                                    }
                                }.padding(12.dp).align(Alignment.CenterEnd)
                            )
                        }
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nazwa użytkownika") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        TextField(
                            value = shareId,
                            onValueChange = { shareId = it },
                            label = { Text("ID sesji kolaboracyjnej") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        //make button rounded only at bottom edges
                        Button(
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                            onClick = {
                                SERVER_URL = serverUrlInput
                                isSetupDialogOpen = false
                                connectToWebsocket(shareId)
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OK")
                        }
                    }
                }
            }

            if (isInfoDialogOpen) {
                Dialog(onDismissRequest = { isInfoDialogOpen = false }) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Informacje o tekście",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(infoText, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { isInfoDialogOpen = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Zamknij")
                        }
                    }
                }
            }

            SnackbarHost(snackbarHost, modifier = Modifier.safeDrawingPadding()) {
                Snackbar(
                    it,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBarProtection(
    hazeState: HazeState,
    color: Color = MaterialTheme.colorScheme.background,
    heightProvider: () -> Float = calculateStatusBarHeight()
) {
    val calculatedHeight = heightProvider()
    Box(
        Modifier.fillMaxWidth().height(with(LocalDensity.current) { calculatedHeight.toDp() })
            .hazeEffect(hazeState) {
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 1f,
                    endIntensity = 0f
                )
                style = HazeStyle(
                    backgroundColor = color,
                    tint = HazeTint(Color.Transparent),
                    blurRadius = 12.dp
                )
            })
}

@Composable
fun calculateStatusBarHeight(): () -> Float {
    val statusBars = WindowInsets.statusBars
    val density = LocalDensity.current
    return { statusBars.getTop(density).times(1.2f) }
}

@Composable
fun IconBtn(
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Icon(
        imageVector,
        contentDescription,
        modifier.height(50.dp).clip(CircleShape).clickable { onClick() }.padding(6.dp)
    )
}

@Composable
fun Gradient(isReversed: Boolean = false) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    if (isReversed) {
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.onPrimary,
                            MaterialTheme.colorScheme.background
                        )
                    }
                )
            )
    )
}

@Composable
fun LastEditor(lastEditor: String?, hazeState : HazeState) {
    AnimatedVisibility(visible = lastEditor != null) {
        Box(modifier = Modifier.safeContentPadding().clip(RoundedCornerShape(16.dp)).hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = MaterialTheme.colorScheme.background, blurRadius = 4.dp, tint = HazeTint(Color.Black.copy(alpha = .2f))))) {
            Text(
                text = "Ostatnia edycja: $lastEditor",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}