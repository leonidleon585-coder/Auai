package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.ModelType
import kotlinx.coroutines.launch
import kotlin.random.Random

// Beautiful Gemini High-Fidelity Custom Dark Mode Colors
val GeminiBlack = Color(0xFF0F1012)
val GeminiGreyDark = Color(0xFF131314)
val GeminiSheetBg = Color(0xFF1E1F22)
val GeminiPillBase = Color(0xFF2B2D31)
val GeminiTextLight = Color(0xFFE3E3E3)
val GeminiTextMuted = Color(0xFF9E9E9E)
val GeminiPurpleAccent = Color(0xFF9C27B0)
val GeminiBlueAccent = Color(0xFF2196F3)
val GeminiOrangeAccent = Color(0xFFFF9800)
val GeminiGreenAccent = Color(0xFF4CAF50)

// Gradient for the 4-point Gemini logo star
val GeminiSparkleBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4285F4), // Teal Blue
        Color(0xFF9B72CB), // Indigo Spark
        Color(0xFFD96A83), // Soft Rose
        Color(0xFFF4B400)  // Golden Yellow
    )
)

data class DrawerItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tabIndex: Int = 0,
    val customPrompt: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnsembleDashboard(
    viewModel: EnsembleDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showModelDropdown by remember { mutableStateOf(false) }
    var expandReasoningGroup by remember { mutableStateOf(true) }

    val recentPrompts = listOf(
        "Дай мне ПРОМПТ на создание моделей",
        "Обзор лучших моделей для MoE",
        "Как изменить папку проекта",
        "Бесплатные AI-замены для софта"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = GeminiGreyDark,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header (Title Gemini & close button)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gemini",
                            style = LocalCyberTypography.title.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = GeminiTextLight
                        )
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Close Menu",
                                tint = GeminiTextLight
                            )
                        }
                    }

                    // Button: Новый чат (Custom rounded capsule)
                    Surface(
                        onClick = {
                            viewModel.clearChat()
                            viewModel.selectTab(0)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .height(52.dp),
                        shape = CircleShape,
                        color = GeminiPillBase,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Pencil Icon",
                                tint = GeminiTextLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Новый чат",
                                style = LocalCyberTypography.body,
                                color = GeminiTextLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Options: Search & Telemetry
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updatePromptInput("Search local MoE indices")
                                    viewModel.selectTab(0)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, "Search Icon", tint = GeminiTextMuted, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Поиск по чатам", style = LocalCyberTypography.body, color = GeminiTextLight)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (state.selectedTab == 1) GeminiPillBase else Color.Transparent)
                                .clickable {
                                    viewModel.selectTab(1) // Open System Monitor
                                    scope.launch { drawerState.close() }
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Build, "Settings/System Icon", tint = GeminiBlueAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("⚙️ Telemetry & scraper", style = LocalCyberTypography.body, color = GeminiTextLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section header: Блокноты
                    Text(
                        text = "Блокноты",
                        style = LocalCyberTypography.mono,
                        color = GeminiTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, "Add Note", tint = GeminiTextMuted, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Новый блокнот", style = LocalCyberTypography.body, color = GeminiTextLight)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section header: Недавние (Recents List, clicking a cell loads text!)
                    Text(
                        text = "Недавние",
                        style = LocalCyberTypography.mono,
                        color = GeminiTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(recentPrompts) { promptText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.selectTab(0)
                                        viewModel.loadPresetChat(promptText)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Chat Bubble",
                                    tint = GeminiTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = promptText,
                                    style = LocalCyberTypography.body,
                                    color = GeminiTextLight,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Profile User 19 & Settings gear at bottom
                    Divider(color = GeminiPillBase.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE91E63)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "User 19",
                                    style = LocalCyberTypography.body,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiTextLight
                                )
                                Text(
                                    text = "Local Developer",
                                    style = LocalCyberTypography.mono,
                                    fontSize = 10.sp,
                                    color = GeminiTextMuted
                                )
                            }
                        }

                        IconButton(onClick = {
                            viewModel.selectTab(1)
                            scope.launch { drawerState.close() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Icon",
                                tint = GeminiTextMuted
                            )
                        }
                    }
                }
            }
        }
    ) {
        // Main Screen Interface container
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GeminiBlack,
                        titleContentColor = GeminiTextLight
                    ),
                    title = {
                        // Dropdown selection triggering title
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showModelDropdown = !showModelDropdown }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gemini " + state.selectedModel.substringAfter("3.5 ").substringAfter("3.1 "),
                                    style = LocalCyberTypography.title.copy(fontSize = 17.sp),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (showModelDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Dropdown Indicator",
                                    tint = GeminiTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Switch",
                                tint = GeminiTextLight
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "New Chat Spark",
                                tint = GeminiTextLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GeminiBlack,
                                Color(0xFF0F121C),
                                Color(0xFF141221)
                            )
                        )
                    )
                    .drawBehind {
                        // Drawing Gemini subtle stardust neon purple bottom points seen in Screen 3, 4, 5
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF007F).copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width / 2, size.height),
                            radius = size.width * 0.8f
                        )
                        drawRect(brush = brush)

                        // Dotted galaxy matrix at bottom
                        val rnd = Random(42)
                        val startY = size.height * 0.75f
                        for (i in 1..80) {
                            val x = rnd.nextFloat() * size.width
                            val y = startY + (rnd.nextFloat() * (size.height - startY))
                            val opacity = 0.05f + (rnd.nextFloat() * 0.15f)
                            val ptRadius = 1.dp.toPx() + (rnd.nextFloat() * 2.dp.toPx())
                            drawCircle(
                                color = Color(0xFFD0BCFF).copy(alpha = opacity),
                                radius = ptRadius,
                                center = Offset(x, y)
                            )
                        }
                    }
            ) {
                // Determine our viewing layer
                if (state.selectedTab == 0) {
                    // TAB 0: CONVERSATIONAL CHAT (RECREATING THE MAIN SCREEN SCENARIOS)
                    ChatContainer(state, viewModel)
                } else {
                    // TAB 1: ADVANCED MACHINE LEARNING SYSTEM PANEL (SCRAPER & 6-THREAD TRAINER CORES)
                    SystemTelemetryHub(state, viewModel)
                }

                // FLOATING MODEL DROPDOWN ACCORDION OVERLAY SHEET (SCREEN 1)
                if (showModelDropdown) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { showModelDropdown = false }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { /* Intercept click to prevent closing dropdown */ }
                                ),
                            shape = RoundedCornerShape(28.dp),
                            color = GeminiSheetBg,
                            tonalElevation = 8.dp,
                            border = BorderStroke(1.dp, GeminiPillBase)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                            ) {
                                // Title Model items
                                DropdownModelRow(
                                    title = "3.1 Flash-Lite",
                                    caption = "Самые быстрые ответы",
                                    isActive = state.selectedModel == "3.1 Flash-Lite",
                                    onClick = {
                                        viewModel.selectModel("3.1 Flash-Lite")
                                        showModelDropdown = false
                                    }
                                )

                                DropdownModelRow(
                                    title = "3.5 Flash",
                                    caption = "All-around help",
                                    isActive = state.selectedModel == "3.5 Flash",
                                    onClick = {
                                        viewModel.selectModel("3.5 Flash")
                                        showModelDropdown = false
                                    }
                                )

                                DropdownModelRow(
                                    title = "3.1 Pro",
                                    caption = "Сложные математические задачи и программирование",
                                    isActive = state.selectedModel == "3.1 Pro",
                                    onClick = {
                                        viewModel.selectModel("3.1 Pro")
                                        showModelDropdown = false
                                    }
                                )

                                Divider(color = GeminiPillBase, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                                // reasoning selector level
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandReasoningGroup = !expandReasoningGroup }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Уровень рассуждений",
                                        style = LocalCyberTypography.body.copy(fontSize = 15.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiTextLight
                                    )
                                    Icon(
                                        imageVector = if (expandReasoningGroup) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand Reasoning",
                                        tint = GeminiTextMuted
                                    )
                                }

                                AnimatedVisibility(visible = expandReasoningGroup) {
                                    Column {
                                        DropdownModelRow(
                                            title = "Стандартный",
                                            caption = "Подходит для большинства вопросов",
                                            isActive = state.reasoningLevel == "Стандартный",
                                            onClick = {
                                                viewModel.selectReasoningLevel("Стандартный")
                                                showModelDropdown = false
                                            }
                                        )

                                        DropdownModelRow(
                                            title = "Расширенный",
                                            caption = "Решение сложных проблем",
                                            isActive = state.reasoningLevel == "Расширенный",
                                            onClick = {
                                                viewModel.selectReasoningLevel("Расширенный")
                                                showModelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper dropdown item rows to fit Screen 1 layout
@Composable
fun DropdownModelRow(
    title: String,
    caption: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected model checked",
                    tint = GeminiTextLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = LocalCyberTypography.body.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) GeminiTextLight else GeminiTextMuted
            )
            Text(
                text = caption,
                style = LocalCyberTypography.mono.copy(fontSize = 11.sp, lineHeight = 14.sp),
                color = GeminiTextMuted
            )
        }
    }
}

// ------------------- GEMINI LOGO programmatically drawn star -------------------
@Composable
fun GeminiStarLogo(
    sizeDp: Int = 80,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(sizeDp.dp)
    ) {
        val width = size.width
        val height = size.height
        val cx = width / 2
        val cy = height / 2

        // A beautiful organic 4-point star path representing Gemini
        val path = Path().apply {
            moveTo(cx, 0f) // Top tip
            quadraticBezierTo(cx, cy, cx + width / 4, cy) // Curved line to right inner corner
            quadraticBezierTo(cx, cy, cx + width / 2, cy) // Right outer tip
            quadraticBezierTo(cx, cy, cx + width / 4, cy) // Curved line back
            // Inner corner tight
            quadraticBezierTo(cx, cy, cx, cy + height / 2) // Bottom outer tip
            quadraticBezierTo(cx, cy, cx - width / 4, cy) // Left inner corner
            quadraticBezierTo(cx, cy, 0f, cy) // Left outer tip
            quadraticBezierTo(cx, cy, cx - width / 4, cy) // Back to center curved top
            close()
        }

        drawPath(
            path = path,
            brush = GeminiSparkleBrush
        )
    }
}

// ------------------- CONVERSATION SHELL VIEW -------------------
@Composable
fun ChatContainer(
    state: DashboardState,
    viewModel: EnsembleDashboardViewModel
) {
    val listState = rememberLazyListState()

    // Auto-scroll list when message history receives new values
    LaunchedEffect(state.chatHistory.size) {
        if (state.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(state.chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Chat Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (state.chatHistory.isEmpty()) {
                // Pristine State (Screens 3, 4, 5)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GeminiStarLogo(sizeDp = 70)
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = state.activeGreetingPhrase,
                        style = LocalCyberTypography.title.copy(fontSize = 25.sp),
                        fontWeight = FontWeight.Normal,
                        color = GeminiTextLight,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Conversational Bubbles List
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(state.chatHistory) { message ->
                        ChatMessageBubble(message, onToggleTelemetry = {
                            viewModel.toggleMessageTelemetry(message.id)
                        })
                    }

                    if (state.ongoingInference) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GeminiStarLogo(sizeDp = 24)
                                Spacer(modifier = Modifier.width(12.dp))
                                // Smooth glowing text or dots loading
                                Text(
                                    text = "Система MoE проводит инференс...",
                                    style = LocalCyberTypography.mono,
                                    color = GeminiTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING INPUT CAPSULE BAR (SCREEN 3, 4, 5 BOTTOM BAR)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 8.dp)
                .height(64.dp),
            shape = CircleShape,
            color = GeminiSheetBg,
            border = BorderStroke(1.dp, GeminiPillBase)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // '+' Add action button inside
                IconButton(onClick = { viewModel.selectTab(1) }) { // Toggles System Control Center
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "System Control Action Shortcut",
                        tint = GeminiTextLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Smooth unbordered capsule BasicTextField integration
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.promptInput.isEmpty()) {
                        Text(
                            text = "Спросить Gemini / local index...",
                            style = LocalCyberTypography.body.copy(fontSize = 15.sp),
                            color = GeminiTextMuted
                        )
                    }

                    androidx.compose.foundation.text.BasicTextField(
                        value = state.promptInput,
                        onValueChange = { viewModel.updatePromptInput(it) },
                        textStyle = LocalCyberTypography.body.copy(color = GeminiTextLight, fontSize = 15.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input_field")
                    )
                }

                // Audio Simulator icon button
                IconButton(onClick = { 
                    viewModel.updatePromptInput("Обзор лучших моделей для MoE")
                }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Audio Trigger Play",
                        tint = GeminiTextLight,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Circular blue/purple speech audio indicator button (Screen 5 right end button)
                IconButton(
                    onClick = { viewModel.submitPrompt() },
                    enabled = state.promptInput.isNotEmpty() && !state.ongoingInference,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (state.promptInput.isNotEmpty()) GeminiSparkleBrush else Brush.linearGradient(colors = listOf(GeminiPillBase, GeminiPillBase)))
                        .testTag("generate_output_button")
                ) {
                    Icon(
                        imageVector = if (state.ongoingInference) Icons.Default.Refresh else Icons.Default.Send,
                        contentDescription = "Send prompt button",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Custom Bubble showing chat conversations and beautiful nested network components
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onToggleTelemetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!message.isUser) {
                // Show Gemini Logo for model responses
                GeminiStarLogo(sizeDp = 26, modifier = Modifier.padding(top = 4.dp, end = 12.dp))
            }

            // Message Bubble Card
            Surface(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 290.dp),
                shape = RoundedCornerShape(
                    topStart = if (message.isUser) 18.dp else 4.dp,
                    topEnd = if (message.isUser) 4.dp else 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                color = if (message.isUser) GeminiPillBase else Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        style = LocalCyberTypography.body.copy(fontSize = 15.sp, lineHeight = 20.sp),
                        color = GeminiTextLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        style = LocalCyberTypography.mono,
                        fontSize = 9.sp,
                        color = GeminiTextMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (message.isUser) TextAlign.End else TextAlign.Start
                    )
                }
            }
        }

        // Expandable MoE system details telemetry display (for Model messages)
        if (!message.isUser && message.result != null) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                shape = RoundedCornerShape(14.dp),
                color = GeminiSheetBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTelemetry() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FF9D))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MoE Arbitrator Telemetry Details",
                                style = LocalCyberTypography.mono,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextLight
                            )
                        }
                        Icon(
                            imageVector = if (message.isTelemetryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Telemetry",
                            tint = GeminiTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = message.isTelemetryExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            // Weight progress coefficients
                            Text(
                                text = "Active weights: LSTM = 22% | SLM = 31% | LLM = 47%",
                                style = LocalCyberTypography.mono,
                                fontSize = 10.sp,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Draw graphical coefficients matching screen styling
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GeminiPillBase)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.22f)
                                        .background(GeminiOrangeAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.31f)
                                        .background(GeminiBlueAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.47f)
                                        .background(GeminiGreenAccent)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Matrix numerical displays of the 8D hidden vectors!
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MiniVectorCard("H_lstm", message.result.hLstm, GeminiOrangeAccent)
                                MiniVectorCard("H_slm", message.result.hSlm, GeminiBlueAccent)
                                MiniVectorCard("H_llm", message.result.hLlm, GeminiGreenAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.MiniVectorCard(
    label: String,
    vals: List<Float>,
    color: Color
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .background(GeminiBlack.copy(alpha = 0.4f))
            .padding(6.dp)
    ) {
        Text(
            text = label, 
            style = LocalCyberTypography.mono, 
            fontSize = 9.sp, 
            fontWeight = FontWeight.Bold, 
            color = color
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = vals.take(4).joinToString(separator = "\n") { String.format("%.2f", it) },
            style = LocalCyberTypography.mono,
            fontSize = 8.sp,
            color = GeminiTextLight,
            lineHeight = 10.sp
        )
    }
}


// ==================== TAB 2: ADVANCED LOCAL CRAWLER & SCRAPER + 6-THREAD MONOLITHIC TRAINER ====================
@Composable
fun SystemTelemetryHub(
    state: DashboardState,
    viewModel: EnsembleDashboardViewModel
) {
    val logsListState = rememberLazyListState()

    // Auto-scroll logs automatically
    LaunchedEffect(state.scraperLogs.size) {
        if (state.scraperLogs.isNotEmpty()) {
            logsListState.animateScrollToItem(state.scraperLogs.size - 1)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Core Control Hub description box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, "Core monitor icon", tint = GeminiBlueAccent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "MONOLITHIC ANSEMBLER CONTROL CORE",
                                style = LocalCyberTypography.title.copy(fontSize = 15.sp),
                                color = GeminiBlueAccent
                            )
                        }

                        // Close returning tab action
                        IconButton(onClick = { viewModel.selectTab(0) }) {
                            Icon(Icons.Default.Close, "Return to Chat", tint = GeminiTextMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Allows configuring local parameters, crawling training textual vectors, tracking the 6 CPU processes dynamically of the physical GPU/CPU cores, and checking training loss.",
                        style = LocalCyberTypography.body,
                        fontSize = 13.sp,
                        color = GeminiTextMuted
                    )
                }
            }
        }

        // LOCAL DATA CRAWLER LOGS console
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LOCAL CRAWLER SYSTEM (Task A)",
                                style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                                color = GeminiTextLight
                            )
                            Text(
                                text = "Corpus: ${String.format("%.1f", state.corpusSizeKb)} KB (${state.totalUrlsScraped} links done)",
                                style = LocalCyberTypography.mono,
                                fontSize = 11.sp,
                                color = GeminiGreenAccent
                            )
                        }

                        Button(
                            onClick = { viewModel.startUrlScraping() },
                            enabled = state.status != TaskStatus.CRAWLING && state.status != TaskStatus.TRAINING,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiGreenAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scrape_data_button")
                        ) {
                            Text("RUN CRAWLING", style = LocalCyberTypography.mono, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated command line logs frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, GeminiPillBase)
                            .padding(10.dp)
                    ) {
                        if (state.scraperLogs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Terminal awaiting initiation...",
                                    style = LocalCyberTypography.mono,
                                    color = GeminiTextMuted
                                )
                            }
                        } else {
                            LazyColumn(
                                state = logsListState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(state.scraperLogs) { log ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text("[${log.timestamp}] ", style = LocalCyberTypography.mono, color = GeminiTextMuted, fontSize = 10.sp)
                                        Text("${log.level} ", style = LocalCyberTypography.mono, color = when(log.level){
                                            "INFO" -> GeminiBlueAccent
                                            "FETCH" -> GeminiOrangeAccent
                                            "PARSE" -> GeminiPurpleAccent
                                            "SAVED" -> GeminiGreenAccent
                                            else -> GeminiTextLight
                                        }, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(log.message, style = LocalCyberTypography.mono, color = GeminiTextLight, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CONCURRENT THREADS TELEMETRY (Task B)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "6-THREAD PARALLEL ENGAGEMENT",
                                style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                                color = GeminiTextLight
                            )
                            Text(
                                text = "Training current epoch: ${state.epoch}/${state.maxEpochs}",
                                style = LocalCyberTypography.mono,
                                fontSize = 11.sp,
                                color = GeminiBlueAccent
                            )
                        }

                        Button(
                            onClick = { viewModel.startParallelTraining() },
                            enabled = state.status != TaskStatus.CRAWLING && state.status != TaskStatus.TRAINING,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlueAccent, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("train_ensemble_button")
                        ) {
                            Text("RUN TRAINING", style = LocalCyberTypography.mono, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6 CPU bars layout matching original telemetry
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.threadMetrics.chunked(2).forEach { pairs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pairs.forEach { thread ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GeminiBlack)
                                            .border(1.dp, GeminiPillBase)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(thread.name, style = LocalCyberTypography.mono, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiTextLight)
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (state.status == TaskStatus.TRAINING) GeminiGreenAccent else GeminiTextMuted)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { thread.progress },
                                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                                color = when(thread.modelType){
                                                    ModelType.LSTM -> GeminiOrangeAccent
                                                    ModelType.SLM -> GeminiBlueAccent
                                                    ModelType.LLM -> GeminiGreenAccent
                                                    else -> GeminiTextMuted
                                                },
                                                trackColor = GeminiPillBase
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Loss: ${thread.currentLoss}", style = LocalCyberTypography.mono, fontSize = 9.sp, color = GeminiTextLight)
                                                Text(thread.throughput, style = LocalCyberTypography.mono, fontSize = 9.sp, color = GeminiTextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // PLOTTED LOSS HISTORY
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MONOLITHIC DUAL LOSS GRAPH",
                        style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                        color = GeminiTextLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw grids
                            for (i in 1..4) {
                                val x = (w / 5) * i
                                drawLine(GeminiPillBase.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                                val y = (h / 5) * i
                                drawLine(GeminiPillBase.copy(alpha = 0.5f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                            }

                            drawPlotLine(state.lstmMetricHistory, GeminiOrangeAccent, w, h)
                            drawPlotLine(state.slmMetricHistory, GeminiBlueAccent, w, h)
                            drawPlotLine(state.llmMetricHistory, GeminiGreenAccent, w, h)
                        }

                        // Legend labels
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LegendLabel("LSTM", GeminiOrangeAccent)
                            LegendLabel("SLM", GeminiBlueAccent)
                            LegendLabel("LLM", GeminiGreenAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendLabel(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = LocalCyberTypography.mono, fontSize = 9.sp, color = GeminiTextLight)
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlotLine(
    data: List<Float>,
    color: Color,
    width: Float,
    height: Float
) {
    if (data.size < 2) return
    val maxVal = 3.5f
    val path = Path()
    val step = width / (data.size - 1)

    data.forEachIndexed { i, value ->
        val x = step * i
        val normalisedVal = value / maxVal
        val y = height - (normalisedVal * height)
        val clampedY = y.coerceIn(0f, height)
        if (i == 0) {
            path.moveTo(x, clampedY)
        } else {
            path.lineTo(x, clampedY)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
}

// ------------------- SHARED TYPOGRAPHY BLOCK -------------------
object LocalCyberTypography {
    val title = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = GeminiTextLight
    )
    val body = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = GeminiTextLight,
        lineHeight = 20.sp
    )
    val mono = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = GeminiTextLight
    )
}
