@file:Suppress("UnusedImport")

package com.server.notatki

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FindReplace
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.server.notatki.ui.theme.darkScheme
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class NoteUpdate(
    val username: String,
    val content: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
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

    var lastEditor by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("Guest") }
    val outgoingTextFlow = remember { MutableSharedFlow<String>(extraBufferCapacity = 10) }
    fun connectToWebsocket() {
        scope.launch {
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = SERVER_URL.substringAfter("//").substringBefore(":"),
                    port = 8000,
                    path = "/ws/notatki"
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

                                if (noteContent != update.content) {
                                    noteContent = update.content
                                    lastEditor = update.username
                                }
                            } catch (e: Exception) {
                                println(e.message)
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


    var showToolbar by remember { mutableStateOf(true) }
    var fontSize by remember { mutableStateOf(16) }
    var wrapText by remember { mutableStateOf(true) }
    //var exp by remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }
    var currentScreen by remember { mutableStateOf(0) }
    var currentScreen2 by remember { mutableStateOf(0) }

    MaterialExpressiveTheme(colorScheme = darkScheme) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHost) { Snackbar(it, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) } })
        {
            AnimatedContent(currentScreen2) { screen ->
                when(screen) {
                    0, 1 -> {
                        Box {
                            val hazeState = rememberHazeState()
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val screenHeight = maxHeight

                                val minTextFieldHeight = screenHeight - 200.dp

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .hazeSource(hazeState)
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.background)))
                                    )

                                    val baseTextFieldModifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = minTextFieldHeight)
                                        .safeContentPadding()

                                    Column(modifier = Modifier.fillMaxSize()) {

                                        AnimatedVisibility(visible = lastEditor != null) {
                                            Text(
                                                text = "Ostatnia edycja: $lastEditor",
                                                style = TextStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 12.sp,
                                                    fontStyle = FontStyle.Italic
                                                ),
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                            )
                                        }

                                        BasicTextField(
                                            value = noteContent,
                                            onValueChange = {
                                                noteContent = it
                                                scope.launch { outgoingTextFlow.emit(it) }
                                            },
                                            textStyle = TextStyle(
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = fontSize.sp
                                            ),
                                            modifier = if (wrapText) {
                                                baseTextFieldModifier
                                            } else {
                                                baseTextFieldModifier.horizontalScroll(rememberScrollState())
                                            },
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                                        )
                                    }
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onPrimary)))
                                    )
                                }
                            }


                            var exp by remember { mutableStateOf(0) }
                            val alpha = animateDpAsState(if (exp == 0) 55.dp else 45.dp, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))

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
                                        modifier = Modifier.safeDrawingPadding().align(Alignment.BottomCenter).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.background).clip(RoundedCornerShape(16.dp)).height(alpha.value).clip(RoundedCornerShape(16.dp)).hazeEffect(state = hazeState)
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
                                        IconBtn(Icons.AutoMirrored.Filled.Undo, "Undo")
                                        IconBtn(Icons.AutoMirrored.Filled.Redo, "Redo")
                                        IconBtn(Icons.Default.ContentCopy, "Copy")
                                        IconBtn(Icons.Default.ContentCut, "Cut")
                                        IconBtn(Icons.Default.ContentPaste, "Paste")
                                    }
                                }

                                FloatingActionButtonMenu(exp == 1, modifier = Modifier.align(Alignment.BottomStart).imePadding(), horizontalAlignment = Alignment.Start, button = {
                                    ToggleFloatingActionButton(exp == 1, { exp = if (exp == 1) 0 else 1 }, containerSize = {48.dp}) {
                                        if (exp == 1) Icon(Icons.AutoMirrored.Rounded.MenuOpen, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer) else Icon(Icons.Rounded.Menu, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                })
                                {

                                    FloatingActionButtonMenuItem({}, { Text("Settings") }, { Icon(Icons.Rounded.Settings, "Settings") })

                                    FloatingActionButtonMenuItem({}, { Text("Info") }, { Icon(Icons.Rounded.Info, "Info") })

                                    FloatingActionButtonMenuItem({}, { Text("Translate") }, { Icon(Icons.Rounded.Translate, "Translate") })

                                    FloatingActionButtonMenuItem({}, { Text("Read") }, { Icon(Icons.AutoMirrored.Rounded.VolumeUp, "Read") })

                                    FloatingActionButtonMenuItem({}, { Text("Replace") }, { Icon(Icons.Rounded.FindReplace, "Replace") })

                                    FloatingActionButtonMenuItem({}, { Text("Find") }, { Icon(Icons.Rounded.Search, "Find") })

                                    FloatingActionButtonMenuItem({}, { Text("Pin") }, { Icon(Icons.AutoMirrored.Rounded.CallReceived, "Pin", modifier = Modifier.graphicsLayer(scaleY = -1f, scaleX = -1f)) })

                                    FloatingActionButtonMenuItem({}, { Text("Share") }, { Icon(Icons.Rounded.Share, "Share") })

                                    FloatingActionButtonMenuItem({}, { Text("Delete") }, { Icon(Icons.Rounded.Close, "Delete") })

                                    FloatingActionButtonMenuItem({}, { Text("Save") }, { Icon(Icons.Rounded.Save, "Save") })

                                    FloatingActionButtonMenuItem({
                                        currentScreen = 1; exp = 0;
                                    }, { Text("Open") }, { Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Open") })
                                }

                                FloatingActionButtonMenu(exp == 2, modifier = Modifier.align(Alignment.BottomEnd), button = {
                                    ToggleFloatingActionButton(exp == 2, { exp = if (exp == 2) 0 else 2 }, containerSize = {48.dp}) {
                                        if (exp == 2) Icon(Icons.Rounded.Close, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer) else Icon(Icons.Rounded.MoreVert, "Menu", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                })
                                {
                                    var showSize by remember { mutableStateOf(false) }
                                    FloatingActionButtonMenuItem(
                                        { showToolbar = !showToolbar },
                                        { Text("Toolbar"); Switch(showToolbar, { showToolbar = !showToolbar }) },
                                        { Icon(Icons.Rounded.Apps, contentDescription = null) })
                                    FloatingActionButtonMenuItem(
                                        {},
                                        {
                                            Text("Font size"); Slider(
                                            fontSize.toFloat(),
                                            { fontSize = it.roundToInt(); showSize = true },
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
                                        { wrapText = !wrapText },
                                        { Text("Wrap text"); Switch(wrapText, { wrapText = !wrapText }) },
                                        { Icon(Icons.AutoMirrored.Rounded.WrapText, contentDescription = null) })
                                    AnimatedVisibility(!showToolbar) {
                                        FloatingActionButtonMenuItem(
                                            {},
                                            { Text("Redo") },
                                            { Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = null) })
                                    }
                                    AnimatedVisibility(!showToolbar) {
                                        FloatingActionButtonMenuItem(
                                            {},
                                            { Text("Undo") },
                                            { Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = null) })
                                    }
                                    //FloatingActionButtonMenuItem(
                                    //    {},
                                    //    { Text("History") },
                                    //    { Icon(Icons.Rounded.History, contentDescription = null) })
                                }
                            }
                        }

                        if (currentScreen == 1) {
                            Box(Modifier.fillMaxSize())
                            LazyColumn(Modifier.fillMaxSize().safeContentPadding()) {
                                items(100) {
                                    Row {
                                        SavedNote(noteContent, Modifier)
                                        SavedNote(noteContent, Modifier)
                                    }
                                }
                            }
                            Button(modifier = Modifier.padding(10.dp), onClick = { currentScreen = 0 }) {
                                Text("Close")
                            }
                        }
                    }
                    2 -> {
                        HeroSection()
                        Column(Modifier.fillMaxSize().safeContentPadding()) {
                            Text("Saved notes", color = MaterialTheme.colorScheme.onBackground)
                            Text("Empty list", modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onBackground)
                            Text("Use 'Save' button to add a note to the list for later use", modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(100.dp))
                            Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp), onClick = { currentScreen = 0 }) {
                                Text("Close")
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
                    Column(Modifier.clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                        TextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("Wpisz adres serwera") },
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            modifier = Modifier.fillMaxWidth()//.padding(top = 8.dp)
                        )
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        //make button rounded only at bottom edges
                        Button(
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                            onClick = {
                                SERVER_URL = serverUrlInput
                                isSetupDialogOpen = false
                                connectToWebsocket()
                            }, modifier = Modifier.fillMaxWidth()) {
                            Text("OK")
                        }
                    }
                }
            }


            StatusBarProtection()
        }
    }
}

/*@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun old() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    //val hazeState = rememberHazeState(blurEnabled = false)

    var showToolbar by remember { mutableStateOf(true) }
    var fontSize by remember { mutableStateOf(16) }
    var wrapText by remember { mutableStateOf(true) }
    var exp by remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }
    Box {
        AnimatedVisibility(
            drawerState.targetValue.ordinal == 0 && showToolbar,
            Modifier.align(Alignment.BottomCenter).alpha(.8F),
            enter = slideInVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = .5F
                ), initialOffsetY = { it }),
            exit = slideOutVertically(
                animationSpec = spring(),
                targetOffsetY = { it })
        ) {
            Row {
                HorizontalFloatingToolbar(
                    true,
                    //colors = FloatingToolbarDefaults.standardFloatingToolbarColors(Color.Transparent),
                    modifier = Modifier.safeDrawingPadding(),///.hazeEffect(state = hazeState)
                    //{
                    //    blurEnabled = false
                    //    blurRadius = 10.dp
                    //    tints = listOf(
                    //        HazeDefaults.tint(
                    //            Color(0, 0, 0, 200)
                    //        )
                    //    )
                    //    noiseFactor = .3f
                    //}
                    floatingActionButton = {

                    }
                ) {
                    IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.Undo, "Undo") }
                    IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.Redo, "Redo") }
                    IconButton(onClick = {}) { Icon(Icons.Default.ContentCopy, "Copy") }
                    IconButton(onClick = {}) { Icon(Icons.Default.ContentCut, "Cut") }
                    IconButton(onClick = {}) { Icon(Icons.Default.ContentPaste, "Paste") }
                }
                //Spacer(Modifier.size(6.dp))
                //FloatingActionButton(
                //    onClick = {},
                //    Modifier.align(Alignment.CenterVertically).clip(CircleShape)
                //    //.hazeEffect(state = hazeState)
                //    //{
                //    //    blurEnabled = false
                //    //    blurRadius = 10.dp
                //    //    tints = listOf(
                //    //        HazeDefaults.tint(
                //    //            Color(0, 0, 0, 200)
                //    //        )
                //    //    )
                //    //    noiseFactor = .3f
                //    //},
                //    //containerColor = Color.Transparent,
                //    //contentColor = Color.White
                //) {
                //    Icon(Icons.Rounded.History, "History")
                //}
            }
        }
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet(
                    windowInsets = WindowInsets(0.dp),
                    modifier = Modifier.width(210.dp).alpha(.8F)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {

                    }
                }
            },
            drawerState = drawerState,
            modifier = Modifier//.hazeSource(hazeState)
        )
        {
            var text by remember {
                mutableStateOf(
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam porta mollis quam, eu tempus odio auctor a. Ut vitae congue libero. Nulla facilisis augue et elit eleifend malesuada. Nam eget massa porta, facilisis purus sed, malesuada lectus. Suspendisse mollis condimentum ante, eu scelerisque tellus dapibus at. Cras sit amet magna ex. Curabitur malesuada ac leo at tincidunt. Nullam a ipsum id est dapibus ultrices. Ut ut tempor massa. Duis vehicula cursus suscipit. Vivamus dignissim feugiat lectus, a lobortis turpis lobortis nec. Curabitur fringilla rhoncus libero, ut mattis arcu bibendum et. Vestibulum efficitur ante consectetur tortor ultricies, sed luctus mauris consectetur. Suspendisse pharetra accumsan sem eu ultrices. Ut egestas finibus risus eu efficitur. Phasellus dapibus metus tortor, id facilisis ipsum consequat ac.\n\nPraesent eget leo eu urna ornare consequat a a urna. In hac habitasse platea dictumst. Etiam sit amet erat nunc. Duis facilisis neque id sem pretium rhoncus. In tristique posuere massa, vel cursus lorem. Cras vestibulum nulla id est vulputate imperdiet. Donec facilisis elit eget erat scelerisque porttitor. Proin eleifend, urna nec interdum placerat, massa metus cursus metus, in commodo turpis turpis sit amet nunc. Proin ut nibh magna. Curabitur efficitur enim a massa consectetur, ac pulvinar quam mattis. Donec a lorem at turpis accumsan consectetur. Etiam gravida tellus non risus mattis, et egestas lectus faucibus. Maecenas tellus nisl, maximus quis malesuada quis, consectetur nec arcu. Sed pretium vehicula feugiat. Nullam a faucibus justo. Sed feugiat finibus libero nec iaculis.\n\nProin mauris dolor, luctus venenatis quam nec, ultrices mollis orci. Pellentesque nulla justo, tincidunt eget dictum condimentum, vehicula sed massa. Donec imperdiet massa in porta ullamcorper. Nullam sed turpis at orci hendrerit sodales quis ut diam. Nunc gravida rutrum elementum. Nam sed rutrum velit, id gravida risus. Aenean venenatis diam vitae diam hendrerit pretium. Proin non tellus feugiat, aliquam quam a, congue nunc. Ut nec sodales eros. Ut condimentum volutpat faucibus. Integer ut ultricies nunc. In facilisis sapien ac tempor fermentum. Praesent sed tortor enim.\n\nVivamus et dui ipsum. Cras nec enim augue. Fusce sodales, enim et accumsan auctor, nisl sapien lobortis tortor, sit amet lobortis mi mi eu lectus. Pellentesque quis eros dolor. Duis consectetur est tellus, id luctus tortor porttitor in. Praesent eget auctor magna. Donec blandit, dui a consectetur aliquet, nibh leo cursus purus, in sagittis nulla urna eget erat. Curabitur sollicitudin neque quam, ac varius massa imperdiet sit amet. Nulla finibus vehicula diam at cursus. Praesent interdum augue velit, sed venenatis augue vehicula eget. Fusce enim elit, accumsan viverra dolor ac, aliquet hendrerit justo. Duis mattis velit ac turpis mollis dictum. Aenean dictum, orci et posuere facilisis, dolor eros maximus libero, ac pellentesque velit augue id magna."
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = fontSize.sp
                        ),
                        modifier = if (wrapText) Modifier.fillMaxSize().safeContentPadding()
                            .padding(vertical = 100.dp) else Modifier.horizontalScroll(
                            rememberScrollState()
                        ).fillMaxSize(),
                        cursorBrush = SolidColor(Color.White),
                    )
                }
            }

        }

        var exp by remember { mutableStateOf(false) }

        FloatingActionButton({
            scope.launch {
                if (drawerState.targetValue.name == DrawerValue.Open.name) {
                    drawerState.close()
                } else {
                    drawerState.open()
                }
            }
        })
        {
            val isOpen = drawerState.targetValue.ordinal != 0

            // 1. Set up the transition (This is unchanged)
            val transition =
                updateTransition(targetState = isOpen, label = "Menu Wipe Transition")

            // 2. Animate a single float from 1.0f to -1.0f (This is unchanged)
            val wipeFraction by transition.animateFloat(
                label = "Wipe Fraction",
                transitionSpec = {
                    spring(dampingRatio = .7F, stiffness = Spring.StiffnessVeryLow)
                }
            ) { isDrawerOpen ->
                if (isDrawerOpen) -1.0f else 1.0f
            }

            // 3. Determine which icon to show (This is unchanged)
            val image = if (wipeFraction > 0) {
                Icons.Default.Menu
            } else {
                Icons.AutoMirrored.Filled.MenuOpen
            }

            // 4. Calculate the clip amount (This is the only change!)

            // This is the "moving" part of the clip (1/3f or 0.3333f)
            val animatedClipRange = 1.0f - 0.7F

            // We map the animation (abs(wipeFraction) which goes 1.0 -> 0.0 -> 1.0)
            // to our new range.
            val clipAmount = 0.7F + (animatedClipRange * abs(wipeFraction))


            // 5. Use Modifier.drawWithContent (This is unchanged)
            Icon(
                imageVector = image,
                contentDescription = "Menu",
                modifier = Modifier.drawWithContent {
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = size.width * clipAmount,
                        bottom = size.height
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
            )
        }


    }
}
*/
@Composable
private fun StatusBarProtection(color: Color = MaterialTheme.colorScheme.background, heightProvider: () -> Float = calculateGradientHeight()) {
    Canvas(Modifier.fillMaxSize()) {
        val calculatedHeight = heightProvider()
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 1f),
                color.copy(alpha = .8f),
                Color.Transparent
            ),
            startY = 0f,
            endY = calculatedHeight
        )
        drawRect(
            brush = gradient,
            size = Size(size.width, calculatedHeight),
        )
    }
}
@Composable
fun calculateGradientHeight(): () -> Float {
    val statusBars = WindowInsets.statusBars
    val density = LocalDensity.current
    return { statusBars.getTop(density).times(1.2f) }
}
@Composable
fun IconBtn(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(imageVector, contentDescription, modifier.height(50.dp).clip(CircleShape).clickable {}.padding(6.dp))
}
@Composable
fun HeroSection() {
    Box(Modifier.height(150.dp))//.fillMaxWidth().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background))))
    //Text(
    //    modifier = Modifier.padding(10.dp, 60.dp, 10.dp, 0.dp).fillMaxWidth(),
    //    text = "ShadowNote",
    //    style = MaterialTheme.typography.displaySmall.copy(
    //        shadow = Shadow(
    //            color = MaterialTheme.colorScheme.primary, blurRadius = 16f
    //        ),
    //        brush = Brush.linearGradient(
    //            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    //        ),
    //        textAlign = TextAlign.Center,
    //        letterSpacing = 1.sp
    //    ),
    //    color = Color.White,
    //)
}
@Composable
fun SavedNote(text: String, m: Modifier) {
    Text(text, color = MaterialTheme.colorScheme.onBackground, modifier = m.padding(10.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp), maxLines = 5)
}