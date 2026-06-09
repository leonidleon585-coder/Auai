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
import androidx.compose.ui.graphics.graphicsLayer
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

@Composable
fun DashboardState.t(key: String): String {
    val lang = this.language.lowercase()
    val dict = mapOf(
        "ru" to mapOf(
            "new_chat" to "Новый чат",
            "search_chats" to "Поиск",
            "telemetry_settings" to "⚙️ Настройки и Лимиты",
            "notebooks" to "Блокноты",
            "new_notebook" to "Новый блокнот",
            "recents" to "Недавние",
            "no_recents" to "Нет недавних запросов. Спросите что-нибудь у Flux AI!",
            "local_developer" to "Локальный разработчик",
            "model_lite" to "Flux Lite",
            "model_lite_desc" to "Минимальная задержка sequence",
            "model_flash" to "Flux Flash",
            "model_flash_desc" to "Универсальный ИИ-помощник",
            "model_pro" to "Flux Pro",
            "model_pro_desc" to "Сложные логические цепочки",
            "reasoning_level" to "Уровень рассуждения ИИ",
            "standard" to "Стандартный",
            "standard_desc" to "Обычный ответ",
            "advanced" to "Расширенный",
            "advanced_desc" to "Пошаговый разбор (CoT)",
            "inference_status" to "Flux AI проводит инференс...",
            "input_hint" to "Запрос к Flux AI / индексу...",
            "control_core" to "ПАРАМЕТРЫ ЯДРА FLUX AI",
            "control_desc" to "Здесь вы можете настраивать локальные параметры, обучать ИИ и переключать языки.",
            "scraper_title" to "ЛОКАЛЬНЫЙ СБОР ดЕННЫХ (Задача А)",
            "run_crawling" to "ЗАПУСТИТЬ СБОР",
            "crawling_active" to "Поиск данных...",
            "terminal_awaiting" to "Терминал ожидает запуска...",
            "training_hub" to "ЦЕНТР ЛОКАЛЬНОГО ОБУЧЕНИЯ (Задача Б)",
            "start_training" to "НАЧАТЬ ОБУЧЕНИЕ",
            "stop_training" to "ОСТАНОВИТЬ ОБУЧЕНИЕ",
            "epoch_status" to "Виртуальная эпоха: %d",
            "hyper_params" to "ПАРАМЕТРЫ И СЕТЕВЫЕ МЕТОДЫ",
            "gating_rule" to "Метод роутинга gating:",
            "lr" to "Скорость обучения LR:",
            "interface_custom" to "КАСТОМИЗАЦИЯ ИНТЕРФЕЙСА ПРИЛОЖЕНИЯ",
            "change_interface_name" to "Название приложения:",
            "select_language" to "Язык интерфейса приложения:",
            "avatar_preset_title" to "Выберите аватар:"
        ),
        "en" to mapOf(
            "new_chat" to "New Chat",
            "search_chats" to "Search chats",
            "telemetry_settings" to "⚙️ Settings & Limits",
            "notebooks" to "Notebooks",
            "new_notebook" to "New Notebook",
            "recents" to "Recent",
            "no_recents" to "No queries yet. Ask Flux AI a question!",
            "local_developer" to "Local Developer",
            "model_lite" to "Flux Lite",
            "model_lite_desc" to "Fastest responsive feedback",
            "model_flash" to "Flux Flash",
            "model_flash_desc" to "Universal chatbot helper",
            "model_pro" to "Flux Pro",
            "model_pro_desc" to "Complex logical/coding tasks",
            "reasoning_level" to "AI Reasoning Level",
            "standard" to "Standard",
            "standard_desc" to "Excellent for general prompts",
            "advanced" to "Advanced",
            "advanced_desc" to "Deeper step-by-step logic",
            "inference_status" to "Flux AI is performing inference...",
            "input_hint" to "Ask Flux AI / local index...",
            "control_core" to "FLUX AI PARAMETERS & CORE HUB",
            "control_desc" to "Configure local hyper-parameters, scrape training data streams, execute training loops, and toggle languages.",
            "scraper_title" to "DATA SCRAPER CORE (Task A)",
            "run_crawling" to "RUN CRAWLING",
            "crawling_active" to "Scraping streams...",
            "terminal_awaiting" to "Terminal awaiting initiation...",
            "training_hub" to "AI TRAINING CONTINUUM (Task B)",
            "start_training" to "START TRAINING",
            "stop_training" to "STOP TRAINING",
            "epoch_status" to "Virtual epoch: %d",
            "hyper_params" to "HYPER-PARAMETERS & ROUTING METHODS",
            "gating_rule" to "Gating routing algorithm:",
            "lr" to "Gating learning rate scale:",
            "interface_custom" to "INTERFACE CUSTOMIZATION",
            "change_interface_name" to "Custom interface title:",
            "select_language" to "Interface language preference:",
            "avatar_preset_title" to "Select a profile avatar preset:"
        )
    )
    return dict[lang]?.get(key) ?: dict["en"]?.get(key) ?: key
}

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

    // Onboarding / Profile Screen
    if (state.nickname.isEmpty()) {
        OnboardingSetupScreen(viewModel = viewModel, state = state)
    } else {
        // Continuous rotation for Flux logo star during training/inference
        val infiniteTransition = rememberInfiniteTransition(label = "flux_rotation")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (state.status == TaskStatus.TRAINING || state.ongoingInference) 3000 else 16000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation_angle"
        )

        val particleProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_progress"
        )

        val bgPulseProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "bg_pulse_progress"
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
                        // Header (Title Custom App Name & close button)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Flux logo star",
                                    tint = GeminiBlueAccent,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer(rotationZ = angle)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.interfaceName,
                                    style = LocalCyberTypography.title.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeminiTextLight
                                )
                            }
                            IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Close Menu",
                                    tint = GeminiTextLight
                                )
                            }
                        }

                        // Button: Новый чат / New Chat
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
                                    text = state.t("new_chat"),
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
                                Text(state.t("search_chats"), style = LocalCyberTypography.body, color = GeminiTextLight)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (state.selectedTab == 1) GeminiPillBase else Color.Transparent)
                                    .clickable {
                                        viewModel.selectTab(1) // Open System Monitor / Settings
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Build, "Settings/System Icon", tint = GeminiBlueAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(state.t("telemetry_settings"), style = LocalCyberTypography.body, color = GeminiTextLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Section header: Блокноты (Notebooks)
                        Text(
                            text = state.t("notebooks"),
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
                            Text(state.t("new_notebook"), style = LocalCyberTypography.body, color = GeminiTextLight)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section header: Недавние (Dynamic Chronological Prompt Recents)
                        Text(
                            text = state.t("recents"),
                            style = LocalCyberTypography.mono,
                            color = GeminiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        if (state.recentPromptsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = state.t("no_recents"),
                                    style = LocalCyberTypography.body.copy(fontSize = 12.sp),
                                    color = GeminiTextMuted
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(state.recentPromptsList) { promptText ->
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
                        }

                        // Personalized Profile card at Sidebar bottom
                        Divider(color = GeminiPillBase.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                        val avatarPresetEmoji = when(state.avatarId) {
                            0 -> "🧠"
                            1 -> "🌌"
                            2 -> "✨"
                            3 -> "🤖"
                            4 -> "🕳️"
                            5 -> "🧬"
                            else -> "🖼️"
                        }
                        val avatarPresetColor = when(state.avatarId) {
                            0 -> Color(0xFF00FF9D)
                            1 -> Color(0xFF9C27B0)
                            2 -> Color(0xFFFF9800)
                            3 -> Color(0xFF2196F3)
                            4 -> Color(0xFFE91E63)
                            5 -> Color(0xFFE040FB)
                            else -> GeminiPurpleAccent
                        }

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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(avatarPresetColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = avatarPresetEmoji,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = state.nickname,
                                        style = LocalCyberTypography.body,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiTextLight,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 140.dp)
                                    )
                                    Text(
                                        text = state.t("local_developer"),
                                        style = LocalCyberTypography.mono,
                                        fontSize = 11.sp,
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
                                val shortModelName = when(state.selectedModel) {
                                    "Flux Lite", "3.1 Flash-Lite" -> "Lite"
                                    "Flux Pro", "3.1 Pro" -> "Pro"
                                    else -> "Flash"
                                }
                                Text(
                                    text = "${state.interfaceName} $shortModelName",
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
                        if (state.ongoingInference) {
                            // Shifting beautiful modern multi-color gradient background during active inference!
                            val color1 = Color.hsv(bgPulseProgress * 360f, 0.40f, 0.14f)
                            val color2 = Color.hsv((bgPulseProgress * 360f + 120f) % 360f, 0.40f, 0.11f)
                            val color3 = Color.hsv((bgPulseProgress * 360f + 240f) % 360f, 0.40f, 0.08f)
                            Brush.verticalGradient(
                                colors = listOf(color1, color2, color3, GeminiBlack)
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    GeminiBlack,
                                    Color(0xFF0F121C),
                                    Color(0xFF141221)
                                )
                            )
                        }
                    )
                    .drawBehind {
                        // Drawing Gemini subtle stardust neon purple bottom points seen in Screen 3, 4, 5
                        val brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF007F).copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width / 2, size.height),
                            radius = size.width * 0.8f
                        )
                        drawRect(brush = brush)

                        // Dotted galaxy matrix at bottom (DYNAMIC drifting particles!)
                        val rnd = Random(42)
                        for (i in 1..80) {
                            val initialX = rnd.nextFloat() * size.width
                            val initialY = (rnd.nextFloat() * size.height)
                            
                            // Drift velocities
                            val speedX = (rnd.nextFloat() - 0.5f) * 60f
                            val speedY = -(0.2f + rnd.nextFloat() * 0.8f) * 80f // Float upwards
                            
                            val opacity = 0.06f + (rnd.nextFloat() * 0.18f)
                            val ptRadius = 1.dp.toPx() + (rnd.nextFloat() * 2.dp.toPx())
                            
                            // Displace coordinates using particleProgress
                            var currentX = (initialX + speedX * particleProgress) % size.width
                            if (currentX < 0) currentX += size.width
                            var currentY = (initialY + speedY * particleProgress) % size.height
                            if (currentY < 0) currentY += size.height

                            drawCircle(
                                color = Color(0xFFD0BCFF).copy(alpha = opacity),
                                radius = ptRadius,
                                center = Offset(currentX, currentY)
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
                                    title = state.t("model_lite"),
                                    caption = state.t("model_lite_desc"),
                                    isActive = state.selectedModel == "Flux Lite" || state.selectedModel == "3.1 Flash-Lite",
                                    onClick = {
                                        viewModel.selectModel("Flux Lite")
                                        showModelDropdown = false
                                    }
                                )

                                DropdownModelRow(
                                    title = state.t("model_flash"),
                                    caption = state.t("model_flash_desc"),
                                    isActive = state.selectedModel == "Flux Flash" || state.selectedModel == "3.5 Flash",
                                    onClick = {
                                        viewModel.selectModel("Flux Flash")
                                        showModelDropdown = false
                                    }
                                )

                                DropdownModelRow(
                                    title = state.t("model_pro"),
                                    caption = state.t("model_pro_desc"),
                                    isActive = state.selectedModel == "Flux Pro" || state.selectedModel == "3.1 Pro",
                                    onClick = {
                                        viewModel.selectModel("Flux Pro")
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
                                        text = state.t("reasoning_level"),
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
                                            title = state.t("standard"),
                                            caption = state.t("standard_desc"),
                                            isActive = state.reasoningLevel == "Стандартный" || state.reasoningLevel == "Standard",
                                            onClick = {
                                                viewModel.selectReasoningLevel(if (state.language == "ru") "Стандартный" else "Standard")
                                                showModelDropdown = false
                                            }
                                        )

                                        DropdownModelRow(
                                            title = state.t("advanced"),
                                            caption = state.t("advanced_desc"),
                                            isActive = state.reasoningLevel == "Расширенный" || state.reasoningLevel == "Advanced",
                                            onClick = {
                                                viewModel.selectReasoningLevel(if (state.language == "ru") "Расширенный" else "Advanced")
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
                        ChatMessageBubble(
                            message = message,
                            onToggleTelemetry = {
                                viewModel.toggleMessageTelemetry(message.id)
                            },
                            onToggleDetails = {
                                viewModel.toggleMessageDetails(message.id)
                            }
                        )
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
    onToggleTelemetry: () -> Unit,
    onToggleDetails: () -> Unit
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
                color = if (message.isUser) GeminiPillBase else GeminiGreyDark,
                border = if (!message.isUser) BorderStroke(1.dp, GeminiPillBase) else null
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (!message.isUser && message.detailsText.isNotEmpty()) {
                        // Top menu with ↓ button as requested
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(GeminiPillBase.copy(alpha = 0.5f))
                                .clickable { onToggleDetails() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(GeminiBlueAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Системный анализ",
                                    style = LocalCyberTypography.mono,
                                    fontSize = 10.sp,
                                    color = GeminiTextMuted
                                )
                            }
                            // Down arrow button [↓]
                            Icon(
                                imageVector = if (message.isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Показать подробности",
                                tint = GeminiBlueAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Clean direct answer
                    Text(
                        text = message.text,
                        style = LocalCyberTypography.body.copy(fontSize = 15.sp, lineHeight = 20.sp),
                        color = GeminiTextLight
                    )

                    // Collapsible detailed explanation (shown when details are expanded)
                    if (!message.isUser && message.detailsText.isNotEmpty()) {
                        AnimatedVisibility(visible = message.isDetailsExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = message.detailsText,
                                    style = LocalCyberTypography.body.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                    color = GeminiTextMuted
                                )
                            }
                        }
                    }

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
                                text = state.t("control_core"),
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
                        text = state.t("control_desc"),
                        style = LocalCyberTypography.body,
                        fontSize = 13.sp,
                        color = GeminiTextMuted
                    )
                }
            }
        }

        // INTERFACE AND PROFILE CUSTOMIZATION SECTION (User requested simplify & customize)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = state.t("interface_custom"),
                        style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                        color = GeminiOrangeAccent
                    )

                    // Nickname & App Title Inputs side-by-side or stacked
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = state.t("change_interface_name"),
                            style = LocalCyberTypography.mono,
                            fontSize = 11.sp,
                            color = GeminiTextMuted
                        )
                        OutlinedTextField(
                            value = state.interfaceName,
                            onValueChange = { viewModel.updateInterfaceName(it) },
                            textStyle = LocalCyberTypography.body.copy(fontSize = 14.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GeminiTextLight,
                                unfocusedTextColor = GeminiTextLight,
                                focusedBorderColor = GeminiBlueAccent,
                                unfocusedBorderColor = GeminiPillBase
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("update_interface_title_field")
                        )
                    }

                    // Selected Language Preferred Preferences Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = state.t("select_language"),
                            style = LocalCyberTypography.mono,
                            fontSize = 11.sp,
                            color = GeminiTextMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateLanguage("ru") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.language == "ru") GeminiBlueAccent else GeminiPillBase,
                                    contentColor = GeminiTextLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Русский 🇷🇺", style = LocalCyberTypography.mono, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.updateLanguage("en") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.language == "en") GeminiBlueAccent else GeminiPillBase,
                                    contentColor = GeminiTextLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("English 🇬🇧", style = LocalCyberTypography.mono, fontSize = 11.sp)
                            }
                        }
                    }

                    // Profile preset selection in Settings
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = state.t("avatar_preset_title"),
                            style = LocalCyberTypography.mono,
                            fontSize = 11.sp,
                            color = GeminiTextMuted
                        )
                        val avatarList = listOf(
                            Triple(0, "🧠", Color(0xFF00FF9D)),
                            Triple(1, "🌌", Color(0xFF9C27B0)),
                            Triple(2, "✨", Color(0xFFFF9800)),
                            Triple(3, "🤖", Color(0xFF2196F3)),
                            Triple(4, "🕳️", Color(0xFFE91E63)),
                            Triple(5, "🧬", Color(0xFFE040FB))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            avatarList.forEach { entry ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (state.avatarId == entry.first) entry.third.copy(alpha = 0.35f) else GeminiPillBase)
                                        .border(
                                            width = if (state.avatarId == entry.first) 2.dp else 1.dp,
                                            color = if (state.avatarId == entry.first) entry.third else GeminiPillBase,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.updateProfile(state.nickname, entry.first)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(entry.second, fontSize = 17.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // HYPER-PARAMETERS CORE CONFIGURATION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.t("hyper_params"),
                        style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                        color = GeminiBlueAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.t("gating_rule"),
                            style = LocalCyberTypography.body.copy(fontSize = 13.sp),
                            color = GeminiTextLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GeminiPillBase)
                                .clickable {
                                    val currentMethod = state.routingMethod
                                    val nextMethod = when(currentMethod) {
                                        "Expert Soft Gating GOR" -> "Dense Mixture-of-Experts Mode"
                                        "Dense Mixture-of-Experts Mode" -> "Sparse MoE Top-K Hard"
                                        else -> "Expert Soft Gating GOR"
                                    }
                                    viewModel.updateRoutingMethod(nextMethod)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (state.routingMethod) {
                                    "Expert Soft Gating GOR" -> "Soft Gating"
                                    "Dense Mixture-of-Experts Mode" -> "Dense MoE"
                                    else -> "Sparse Top-K"
                                },
                                style = LocalCyberTypography.mono,
                                color = GeminiBlueAccent
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = state.t("lr"),
                            style = LocalCyberTypography.body.copy(fontSize = 13.sp),
                            color = GeminiTextLight
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.001, 0.01, 0.05).forEach { lrVal ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (state.learningRate == lrVal) GeminiBlueAccent else GeminiPillBase)
                                        .clickable { viewModel.updateLearningRate(lrVal) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = lrVal.toString(),
                                        style = LocalCyberTypography.mono,
                                        color = if (state.learningRate == lrVal) Color.White else GeminiTextLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // NEURAL MODULE METRICS (UNDERSTANDING & RESPONSE LOGIC)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (state.language == "ru") "ИНТЕЛЛЕКТУАЛЬНЫЙ ПОТЕНЦИАЛ НЕЙРОСЕТИ" else "NEURAL INTELLECT POTENTIAL",
                        style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                        color = GeminiGreenAccent
                    )

                    // Calculate understanding & logic dynamically
                    val understandingPercent = Math.min(100f, 45f + (state.corpusSizeKb * 0.5f) + (state.chatHistory.size * 2f))
                    val logicPercent = Math.min(100f, 32f + (state.epoch * 0.68f) + (state.chatHistory.size * 1.5f))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (state.language == "ru") "Понимание нейросети" else "Understanding level",
                                style = LocalCyberTypography.body.copy(fontSize = 13.sp),
                                color = GeminiTextLight
                            )
                            Text(
                                text = String.format("%.1f%%", understandingPercent),
                                style = LocalCyberTypography.mono,
                                fontSize = 13.sp,
                                color = GeminiGreenAccent
                            )
                        }
                        // Beautiful filled progress bar
                        LinearProgressIndicator(
                            progress = { understandingPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GeminiGreenAccent,
                            trackColor = GeminiPillBase
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (state.language == "ru") "Логика ответа" else "Response Logic",
                                style = LocalCyberTypography.body.copy(fontSize = 13.sp),
                                color = GeminiTextLight
                            )
                            Text(
                                text = String.format("%.1f%%", logicPercent),
                                style = LocalCyberTypography.mono,
                                fontSize = 13.sp,
                                color = GeminiBlueAccent
                            )
                        }
                        // Beautiful filled progress bar
                        LinearProgressIndicator(
                            progress = { logicPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GeminiBlueAccent,
                            trackColor = GeminiPillBase
                        )
                    }
                    
                    Text(
                        text = if (state.language == "ru") {
                            "Понимание повышается по мере загрузки данных краулером. Логика совершенствуется в процессе обучения эпох."
                        } else {
                            "Understanding expands as crawler scrapes data. Logic refines as epochs are trained."
                        },
                        style = LocalCyberTypography.mono,
                        fontSize = 10.sp,
                        color = GeminiTextMuted
                    )
                }
            }
        }

        // SNAPDRAGON 8s GEN X OPTIMIZATION MODULE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Snapdragon optimization",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Qualcomm Snapdragon 8s Gen x",
                                style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                                color = GeminiTextLight
                            )
                        }
                        
                        // Active status chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00FF9D).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00FF9D), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (state.language == "ru") "ОПТИМИЗИРОВАНО" else "OPTIMIZED",
                                style = LocalCyberTypography.mono,
                                fontSize = 8.sp,
                                color = Color(0xFF00FF9D),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = if (state.language == "ru") {
                            "Активированы низкоуровневые синаптические шлюзы для чипсетов Snapdragon 8s Gen 3 и аналогичных серий 8s Gen. Потоки распределены по схеме планировщика Kryo: 1x супер-ядро Cortex-X4 обрабатывает MoE арбитрацию, 4x ядра Cortex-A720 ускоряют вычисления SLM/LLM блоков, а NPU Hexagon выполняет параллельные тензорные операции в формате FP16 с нулевой задержкой."
                        } else {
                            "Low-level synaptic gates activated for Snapdragon 8s Gen 3 and similar 8s Gen chipsets. Thread distribution is mapped via Kryo scheduler: 1x Prime Cortex-X4 core handles MoE arbitration, 4x Performance Cortex-A720 handles concurrent SLM/LLM evaluations, while NPU Hexagon processes parallel tensor blocks in FP16 with zero latency."
                        },
                        style = LocalCyberTypography.body.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = GeminiTextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Specs chips
                        listOf(
                            Triple("NPU Hexagon", "FP16 Acceleration", GeminiGreenAccent),
                            Triple("Kryo Cores", "8 Threads Bound", GeminiBlueAccent),
                            Triple("Adreno GPU", "Vulkan Pipeline", GeminiOrangeAccent)
                        ).forEach { (label, value, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .border(1.dp, GeminiPillBase)
                                    .padding(6.dp)
                            ) {
                                Column {
                                    Text(label, style = LocalCyberTypography.mono, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold)
                                    Text(value, style = LocalCyberTypography.mono, fontSize = 8.sp, color = GeminiTextLight)
                                }
                            }
                        }
                    }
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
                                text = state.t("scraper_title"),
                                style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                                color = GeminiTextLight
                            )
                            Text(
                                text = if (state.language == "ru")
                                    "Индекс: ${String.format("%.1f", state.corpusSizeKb)} КБ (ссылок: ${state.totalUrlsScraped})"
                                else
                                    "Corpus: ${String.format("%.1f", state.corpusSizeKb)} KB (${state.totalUrlsScraped} links done)",
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
                            Text(
                                text = if (state.status == TaskStatus.CRAWLING) state.t("crawling_active") else state.t("run_crawling"),
                                style = LocalCyberTypography.mono,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated command line logs frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, GeminiPillBase)
                            .padding(10.dp)
                    ) {
                        if (state.scraperLogs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.t("terminal_awaiting"),
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

        // CONCURRENT THREADS TELEMETRY (Task B, endlessly runs until stop clicked)
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
                                text = state.t("training_hub"),
                                style = LocalCyberTypography.title.copy(fontSize = 14.sp),
                                color = GeminiTextLight
                            )
                            Text(
                                text = String.format(state.t("epoch_status"), state.epoch),
                                style = LocalCyberTypography.mono,
                                fontSize = 11.sp,
                                color = GeminiBlueAccent
                            )
                        }

                        Button(
                            onClick = {
                                if (state.status == TaskStatus.TRAINING) {
                                    viewModel.stopParallelTraining()
                                } else {
                                    viewModel.startParallelTraining()
                                }
                            },
                            enabled = state.status != TaskStatus.CRAWLING,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.status == TaskStatus.TRAINING) Color(0xFFE91E63) else GeminiBlueAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("train_ensemble_button")
                        ) {
                            Text(
                                text = if (state.status == TaskStatus.TRAINING) state.t("stop_training") else state.t("start_training"),
                                style = LocalCyberTypography.mono,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic CPU Core Thread Progress list (SIMPLIFIED / CLEARED FOR BETTER SCANNING)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.threadMetrics.forEach { thread ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, GeminiPillBase.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (thread.modelType) {
                                                        ModelType.LSTM -> GeminiOrangeAccent
                                                        ModelType.SLM -> GeminiBlueAccent
                                                        ModelType.LLM -> GeminiGreenAccent
                                                        else -> GeminiTextMuted
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = thread.name,
                                            style = LocalCyberTypography.mono,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GeminiTextLight
                                        )
                                    }
                                    Text(
                                        text = thread.throughput,
                                        style = LocalCyberTypography.mono,
                                        fontSize = 10.sp,
                                        color = GeminiTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { thread.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = when (thread.modelType) {
                                        ModelType.LSTM -> GeminiOrangeAccent
                                        ModelType.SLM -> GeminiBlueAccent
                                        ModelType.LLM -> GeminiGreenAccent
                                        else -> GeminiTextMuted
                                    },
                                    trackColor = GeminiPillBase
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Loss: ${thread.currentLoss}",
                                    style = LocalCyberTypography.mono,
                                    fontSize = 10.sp,
                                    color = GeminiTextLight
                                )
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
                        text = if (state.language == "ru") "ГРАФИК ФУНКЦИИ ПОТЕРЬ (Loss)" else "MONOLITHIC DUAL LOSS GRAPH",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSetupScreen(
    viewModel: EnsembleDashboardViewModel,
    state: DashboardState
) {
    var inputNick by remember { mutableStateOf("") }
    var selectedAvatarIdx by remember { mutableStateOf(0) }
    var customImageSelected by remember { mutableStateOf(false) }
    var customImageUri by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeminiBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            GeminiStarLogo(sizeDp = 64)
            
            Text(
                text = if (state.language == "ru") "Добро пожаловать в Flux AI!" else "Welcome to Flux AI!",
                style = LocalCyberTypography.title.copy(fontSize = 24.sp),
                color = GeminiTextLight,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (state.language == "ru") "Настройте ваш профиль локального разработчика для начала работы." else "Setup your local developer profile to start working.",
                style = LocalCyberTypography.body,
                color = GeminiTextMuted,
                textAlign = TextAlign.Center
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = GeminiSheetBg),
                border = BorderStroke(1.dp, GeminiPillBase),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (state.language == "ru") "Ваш никнейм" else "Your Nickname",
                        style = LocalCyberTypography.mono,
                        color = GeminiTextMuted
                    )

                    OutlinedTextField(
                        value = inputNick,
                        onValueChange = { inputNick = it },
                        placeholder = { Text(if (state.language == "ru") "Введите никнейм..." else "Enter nickname...", color = GeminiTextMuted, style = LocalCyberTypography.body) },
                        textStyle = LocalCyberTypography.body.copy(color = GeminiTextLight),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GeminiTextLight,
                            unfocusedTextColor = GeminiTextLight,
                            focusedBorderColor = GeminiBlueAccent,
                            unfocusedBorderColor = GeminiPillBase,
                            cursorColor = GeminiBlueAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nickname_input_field")
                    )

                    Divider(color = GeminiPillBase, thickness = 1.dp)

                    Text(
                        text = if (state.language == "ru") "Выберите аватар" else "Select Avatar",
                        style = LocalCyberTypography.mono,
                        color = GeminiTextMuted
                    )

                    // Presets Grid
                    val gridAvatars = listOf(
                        Triple(0, "🧠", Color(0xFF00FF9D)),
                        Triple(1, "🌌", Color(0xFF9C27B0)),
                        Triple(2, "✨", Color(0xFFFF9800)),
                        Triple(3, "🤖", Color(0xFF2196F3)),
                        Triple(4, "🕳️", Color(0xFFE91E63)),
                        Triple(5, "🧬", Color(0xFFE040FB))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        gridAvatars.forEach { avatar ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatarIdx == avatar.first && !customImageSelected) avatar.third.copy(alpha = 0.35f) else GeminiPillBase)
                                    .border(
                                        width = if (selectedAvatarIdx == avatar.first && !customImageSelected) 2.dp else 1.dp,
                                        color = if (selectedAvatarIdx == avatar.first && !customImageSelected) avatar.third else GeminiPillBase,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedAvatarIdx = avatar.first
                                        customImageSelected = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatar.second, fontSize = 20.sp)
                            }
                        }
                    }

                    // Simulated Gallery selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (customImageSelected) GeminiPillBase else Color.Transparent)
                            .border(1.dp, if (customImageSelected) GeminiGreenAccent else GeminiPillBase, RoundedCornerShape(12.dp))
                            .clickable {
                                customImageSelected = true
                                customImageUri = "custom_avatar_from_gallery"
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Gallery Picker Icon",
                            tint = if (customImageSelected) GeminiGreenAccent else GeminiTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.language == "ru") "Выбрать фото из галереи" else "Choose standard custom photo",
                            style = LocalCyberTypography.mono,
                            color = if (customImageSelected) GeminiTextLight else GeminiTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val finalNick = inputNick.trim().ifEmpty { if (state.language == "ru") "Оператор" else "Operator" }
                            val finalAvatarId = if (customImageSelected) 99 else selectedAvatarIdx
                            viewModel.updateProfile(finalNick, finalAvatarId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlueAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text(
                            text = if (state.language == "ru") "Сохранить профиль" else "Save Profile",
                            style = LocalCyberTypography.mono.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }

            // Small language selection triggers during onboarding
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🇷🇺 Русский",
                    style = LocalCyberTypography.mono,
                    color = if (state.language == "ru") GeminiBlueAccent else GeminiTextMuted,
                    modifier = Modifier.clickable { viewModel.updateLanguage("ru") }
                )
                Text("|", color = GeminiPillBase)
                Text(
                    text = "🇬🇧 English",
                    style = LocalCyberTypography.mono,
                    color = if (state.language == "en") GeminiBlueAccent else GeminiTextMuted,
                    modifier = Modifier.clickable { viewModel.updateLanguage("en") }
                )
            }
        }
    }
}
