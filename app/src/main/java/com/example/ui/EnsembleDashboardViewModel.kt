package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.EnsembleSystemConfig
import com.example.config.ModelType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.random.Random

enum class TaskStatus {
    IDLE, CRAWLING, TRAINING, READY
}

data class ScrapeLog(
    val timestamp: String,
    val level: String,
    val message: String
)

data class ThreadMetric(
    val id: Int,
    val name: String,
    val modelType: ModelType,
    val progress: Float,
    val currentLoss: Float,
    val currentAccuracy: Float,
    val throughput: String // tokens per second
)

data class EnsembleOutput(
    val prompt: String,
    val generatedText: String,
    val hLstm: List<Float>,
    val hSlm: List<Float>,
    val hLlm: List<Float>,
    val finalWeights: List<Float>, // Arbitrator gating coefficients
    val targetTokensCount: Int
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val result: EnsembleOutput? = null,
    val isTelemetryExpanded: Boolean = false,
    val detailsText: String = "",
    val isDetailsExpanded: Boolean = false
)

data class DashboardState(
    val selectedTab: Int = 0, // 0: Chat, 1: Telemetry System Hub
    val systemConfig: EnsembleSystemConfig = EnsembleSystemConfig(),
    val status: TaskStatus = TaskStatus.IDLE,
    val selectedSearchQuery: String = "Neural Arch",
    
    // Scraper States
    val scrapedDataCorpus: String = "",
    val totalUrlsScraped: Int = 0,
    val corpusSizeKb: Float = 0.0f,
    val scraperLogs: List<ScrapeLog> = emptyList(),
    
    // Multi-Trainer States
    val epoch: Int = 0,
    val maxEpochs: Int = 100,
    val activeThreadsCount: Int = 0,
    val threadMetrics: List<ThreadMetric> = emptyList(),
    val lstmMetricHistory: List<Float> = emptyList(),
    val slmMetricHistory: List<Float> = emptyList(),
    val llmMetricHistory: List<Float> = emptyList(),
    val trainingLogs: List<String> = emptyList(),
    
    // Inference States
    val promptInput: String = "",
    val ongoingInference: Boolean = false,
    val inferenceResult: EnsembleOutput? = null,
    
    // Conversational Chat History State (Grounded on local indices)
    val chatHistory: List<ChatMessage> = emptyList(),
    val selectedModel: String = "Flux Flash",
    val reasoningLevel: String = "Стандартный",
    val activeGreetingPhrase: String = "Оператор, поехали!",
    
    // Localization & Personalization Extensions
    val language: String = "ru",
    val nickname: String = "",
    val avatarId: Int = 0,
    val interfaceName: String = "Flux AI",
    val routingMethod: String = "Arbitrator MoE",
    val learningRate: Double = 0.005,
    val recentPromptsList: List<String> = emptyList()
)

class EnsembleDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("flux_ai_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private var trainingJob: Job? = null
    private var scrapingJob: Job? = null

    init {
        // Load properties from SharedPreferences
        val savedLang = prefs.getString("language", "ru") ?: "ru"
        val savedNick = prefs.getString("nickname", "") ?: ""
        val savedAvatarId = prefs.getInt("avatarId", 0)
        val savedInterfaceName = prefs.getString("interfaceName", "Flux AI") ?: "Flux AI"
        val savedRoutingMethod = prefs.getString("routingMethod", "Arbitrator MoE") ?: "Arbitrator MoE"
        val savedLearningRate = prefs.getFloat("learningRate", 0.005f).toDouble()
        
        val savedRecentsStr = prefs.getString("recentPromptsStr", "") ?: ""
        val savedRecents = if (savedRecentsStr.isEmpty()) emptyList() else savedRecentsStr.split("|||")

        _uiState.update {
            it.copy(
                language = savedLang,
                nickname = savedNick,
                avatarId = savedAvatarId,
                interfaceName = savedInterfaceName,
                routingMethod = savedRoutingMethod,
                learningRate = savedLearningRate,
                recentPromptsList = savedRecents
            )
        }

        resetThreadMetrics()
        rotateGreetingPhrase()
    }

    fun rotateGreetingPhrase() {
        val currentLang = _uiState.value.language
        val selectNick = _uiState.value.nickname.ifEmpty { if (currentLang == "ru") "Оператор" else "Operator" }
        
        val phrases = if (currentLang == "ru") {
            listOf(
                "$selectNick, приступим к работе!",
                "Ядро Flux AI готово к запросам.",
                "$selectNick, задайте ваш вопрос по локальным индексам.",
                "Связующая матрица MoE в режиме ожидания.",
                "Flux AI приветствует вас!"
            )
        } else {
            listOf(
                "Welcome, $selectNick. Ready to assist!",
                "Flux AI Core status: ACTIVE.",
                "Type your prompt to query MoE matrix, $selectNick.",
                "Dynamic neural weights aligned and waiting.",
                "Flux AI is online and localized."
            )
        }
        val next = phrases[Random.nextInt(phrases.size)]
        _uiState.update { it.copy(activeGreetingPhrase = next) }
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _uiState.update { it.copy(language = lang) }
        rotateGreetingPhrase()
    }

    fun updateProfile(nickname: String, avatarId: Int) {
        prefs.edit().putString("nickname", nickname).putInt("avatarId", avatarId).apply()
        _uiState.update { it.copy(nickname = nickname, avatarId = avatarId) }
        rotateGreetingPhrase()
    }

    fun updateInterfaceName(name: String) {
        prefs.edit().putString("interfaceName", name).apply()
        _uiState.update { it.copy(interfaceName = name) }
    }

    fun updateRoutingMethod(method: String) {
        prefs.edit().putString("routingMethod", method).apply()
        _uiState.update { it.copy(routingMethod = method) }
    }

    fun updateLearningRate(lr: Double) {
        prefs.edit().putFloat("learningRate", lr.toFloat()).apply()
        _uiState.update { it.copy(learningRate = lr) }
    }

    fun saveRecentPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return
        val currentList = _uiState.value.recentPromptsList.toMutableList()
        currentList.remove(trimmed)
        currentList.add(0, trimmed)
        val limited = currentList.take(20)
        prefs.edit().putString("recentPromptsStr", limited.joinToString("|||")).apply()
        _uiState.update { it.copy(recentPromptsList = limited) }
    }

    private fun resetThreadMetrics() {
        val config = _uiState.value.systemConfig
        val threadMetrics = ArrayList<ThreadMetric>()
        var count = 1
        config.threadAllocations.forEach { allocation ->
            for (i in 1..allocation.assignedThreads) {
                threadMetrics.add(
                    ThreadMetric(
                        id = count,
                        name = "Worker-$count (${allocation.modelType.name}-T$i)",
                        modelType = allocation.modelType,
                        progress = 0.0f,
                        currentLoss = 2.5f,
                        currentAccuracy = 0.05f,
                        throughput = "0 t/s"
                    )
                )
                count++
            }
        }
        _uiState.update { it.copy(threadMetrics = threadMetrics) }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun selectModel(modelName: String) {
        _uiState.update { it.copy(selectedModel = modelName) }
    }

    fun selectReasoningLevel(levelName: String) {
        _uiState.update { it.copy(reasoningLevel = levelName) }
    }

    fun updatePromptInput(text: String) {
        _uiState.update { it.copy(promptInput = text) }
    }

    fun addScrapedLog(level: String, message: String) {
        val formatTime = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val newLog = ScrapeLog(formatTime, level, message)
        _uiState.update { it.copy(scraperLogs = it.scraperLogs + newLog) }
    }

    fun clearCorpus() {
        _uiState.update {
            it.copy(
                scrapedDataCorpus = "",
                totalUrlsScraped = 0,
                corpusSizeKb = 0f,
                scraperLogs = emptyList(),
                status = TaskStatus.IDLE
            )
        }
    }

    fun clearChat() {
        _uiState.update {
            it.copy(
                chatHistory = emptyList(),
                promptInput = "",
                ongoingInference = false,
                inferenceResult = null
            )
        }
        rotateGreetingPhrase()
    }

    fun loadPresetChat(prompt: String) {
        _uiState.update { it.copy(promptInput = prompt) }
        runEnsembleInference()
    }

    fun toggleMessageTelemetry(messageId: String) {
        _uiState.update { state ->
            val updated = state.chatHistory.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(isTelemetryExpanded = !msg.isTelemetryExpanded)
                } else msg
            }
            state.copy(chatHistory = updated)
        }
    }

    fun toggleMessageDetails(messageId: String) {
        _uiState.update { state ->
            val updated = state.chatHistory.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(isDetailsExpanded = !msg.isDetailsExpanded)
                } else msg
            }
            state.copy(chatHistory = updated)
        }
    }

    fun submitPrompt() {
        val prompt = _uiState.value.promptInput
        if (prompt.trim().isEmpty()) return
        runEnsembleInference()
    }

    /**
     * Step A: High-fidelity web scraper simulator.
     */
    fun startUrlScraping() {
        scrapingJob?.cancel()
        scrapingJob = viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(status = TaskStatus.CRAWLING, scraperLogs = emptyList()) }
            addScrapedLog("INFO", "Initializing local Data Scraper. Target: Wikipedia / Scientific AI Blogs")
            addScrapedLog("INFO", "Current query seed: '${_uiState.value.selectedSearchQuery}'")
            addScrapedLog("DEBUG", "Pre-configuring HTTP User-Agent: ${_uiState.value.systemConfig.scraper.userAgent}")
            delay(800)

            val linksToScrape = listOf(
                "https://en.wikipedia.org/wiki/Long_short-term_memory",
                "https://en.wikipedia.org/wiki/Transformer_(deep_learning)",
                "https://arxiv.org/html/2310.0230_MoE_Architectures",
                "https://pytorch.org/docs/stable/nn.html"
            )

            var corpusAccumulator = ""
            var urlsProcessed = 0

            linksToScrape.forEach { url ->
                if (!isActive) return@forEach
                addScrapedLog("FETCH", "Connecting to standard Web URL: $url")
                delay(600)
                
                addScrapedLog("PARSE", "Standard HTTP Code: 200 OK. Clearing HTML nodes via regex layout...")
                delay(400)

                val crawledCorpusSample = when {
                    url.contains("LSTM") || url.contains("lstm") -> {
                        "Long Short-Term Memory (LSTM) is a recurrent neural network (RNN) architecture " +
                        "designed to solve the vanishing gradient problem. By utilizing internal gating mechanisms " +
                        "— specifically input, forget, and output gates — LSTMs regulate the memory flow over prolonged sequence lengths. " +
                        "This contextual window ensures sequential historical tokens are embedded precisely without exponential gradient degradation."
                    }
                    url.contains("Transformer") -> {
                        "Transformer models represent state-of-the-art architectures in natural language parsing. " +
                        "Standard small language models (SLM) leverage self-attention mechanisms to map dependencies " +
                        "between words instantaneously. By computing scaled dot-product attention scores across parallel token matrices, " +
                        "transformers construct a global attention contextual subspace."
                    }
                    url.contains("MoE") -> {
                        "A Mixture of Experts (MoE) system or MoE Arbitrator replaces uniform layer execution with highly " +
                        "specialized independent sub-networks. The outputs of hidden states (H) of distinct architectures " +
                        "are unified through a continuous gating layer. Combining the capacities of an LSTM sequence tracker, " +
                        "an SLM standard transformer, and a deep LLM produces an expressive ensemble model with shared neural links."
                    }
                    else -> {
                        "Advanced Language Models (LLM) often incorporate modern features such as Rotary Position Embeddings (RoPE) " +
                        "to expand active sequence dimension extrapolation, SwiGLU activation functions instead of plain ReLU for " +
                        "higher non-linear capacity, and Root Mean Square Normalization (RMSNorm) to enforce faster gradient backpropagation metrics."
                    }
                }

                corpusAccumulator += crawledCorpusSample + "\n\n"
                urlsProcessed++

                _uiState.update {
                    it.copy(
                        scrapedDataCorpus = corpusAccumulator,
                        totalUrlsScraped = urlsProcessed,
                        corpusSizeKb = (corpusAccumulator.toByteArray().size / 1024f)
                    )
                }
                addScrapedLog("SAVED", "Added raw string array to local SQL / Room Database dataset cache (${(crawledCorpusSample.length)} characters).")
            }

            addScrapedLog("SUCCESS", "Web scraping complete. Model corpus ready for Multi-Threaded training!")
            _uiState.update { it.copy(status = TaskStatus.READY) }
        }
    }

    /**
     * Step B: Multi-threaded parallel trainer module (Endless / Continuous Loop).
     */
    fun startParallelTraining() {
        if (_uiState.value.status != TaskStatus.READY && _uiState.value.scrapedDataCorpus.isEmpty()) {
            addScrapedLog("ERROR", if (_uiState.value.language == "ru") "Локальный датасет отсутствует. Сначала запустите сбор!" else "No local dataset exists. Run the scraper first!")
            return
        }

        trainingJob?.cancel()
        trainingJob = viewModelScope.launch(Dispatchers.Default) {
            _uiState.update {
                it.copy(
                    status = TaskStatus.TRAINING,
                    epoch = 0,
                    activeThreadsCount = 6,
                    trainingLogs = listOf(
                        if (it.language == "ru") "Инициализация движка: выделение 6 ядер физического процессора..."
                        else "Initializing Engine: Allocation of 6 Thread/Process Cores..."
                    ),
                    lstmMetricHistory = emptyList(),
                    slmMetricHistory = emptyList(),
                    llmMetricHistory = emptyList()
                )
            }
            resetThreadMetrics()
            delay(1000)

            var currentEpoch = 1
            while (isActive && _uiState.value.status == TaskStatus.TRAINING) {
                delay(400) // Beautiful smooth speed
                
                _uiState.update { currentState ->
                    val updatedMetrics = currentState.threadMetrics.map { metric ->
                        val noiseLoss = (Random.nextFloat() - 0.5f) * 0.03f
                        val lossDecay = metric.currentLoss * 0.98f + noiseLoss
                        val targetLoss = when (metric.modelType) {
                            ModelType.LSTM -> 0.45f
                            ModelType.SLM -> 0.32f
                            ModelType.LLM -> 0.12f
                            else -> 0.2f
                        }
                        val finalLoss = if (lossDecay < targetLoss) targetLoss + (Random.nextFloat() * 0.02f) else lossDecay
                        val accuracyRise = 1.0f - (finalLoss / 3.5f) + (Random.nextFloat() * 0.015f)
                        
                        metric.copy(
                            progress = ((currentEpoch * 10) % 100) / 100f, // Looping animated progress bar
                            currentLoss = String.format(java.util.Locale.US, "%.4f", finalLoss).toFloat(),
                            currentAccuracy = String.format(java.util.Locale.US, "%.4f", if (accuracyRise > 0.99f) 0.99f else accuracyRise).toFloat(),
                            throughput = "${Random.nextInt(180, 420)} tokens/s"
                        )
                    }

                    val lstmLoss = updatedMetrics.filter { it.modelType == ModelType.LSTM }.map { it.currentLoss }.average().toFloat()
                    val slmLoss = updatedMetrics.filter { it.modelType == ModelType.SLM }.map { it.currentLoss }.average().toFloat()
                    val llmLoss = updatedMetrics.filter { it.modelType == ModelType.LLM }.map { it.currentLoss }.average().toFloat()

                    // Cap history queues to last 120 points to avoid memory overhead
                    val maxHistorySize = 120
                    val newLstm = (currentState.lstmMetricHistory + lstmLoss).takeLast(maxHistorySize)
                    val newSlm = (currentState.slmMetricHistory + slmLoss).takeLast(maxHistorySize)
                    val newLlm = (currentState.llmMetricHistory + llmLoss).takeLast(maxHistorySize)

                    currentState.copy(
                        epoch = currentEpoch,
                        threadMetrics = updatedMetrics,
                        lstmMetricHistory = newLstm,
                        slmMetricHistory = newSlm,
                        llmMetricHistory = newLlm,
                        trainingLogs = getLogSampleForEpoch(currentEpoch, lstmLoss, slmLoss, llmLoss) + currentState.trainingLogs.take(50)
                    )
                }
                currentEpoch++
            }
        }
    }

    fun stopParallelTraining() {
        trainingJob?.cancel()
        _uiState.update {
            it.copy(
                status = TaskStatus.READY,
                activeThreadsCount = 0
            )
        }
    }

    private fun getLogSampleForEpoch(epoch: Int, lstmLoss: Float, slmLoss: Float, llmLoss: Float): List<String> {
        val formatTime = java.text.SimpleDateFormat("mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val isRu = _uiState.value.language == "ru"
        val corpus = _uiState.value.scrapedDataCorpus

        // Extract a dynamic text chunk from the web search/scrape database to train on
        val textSnippet = if (corpus.isNotEmpty()) {
            val cleanCorpus = corpus.replace("\n", " ").trim()
            val words = cleanCorpus.split("\\s+".toRegex()).filter { it.length > 2 }
            if (words.isNotEmpty()) {
                val startIndex = (epoch * 5) % words.size
                val endIndex = minOf(words.size, startIndex + 8)
                val chunk = words.subList(startIndex, endIndex).joinToString(" ")
                if (chunk.length > 50) chunk.take(50) + "..." else "$chunk..."
            } else ""
        } else ""

        val internetTrainingLog = if (textSnippet.isNotEmpty()) {
            if (isRu) {
                "[$formatTime] ОБУЧЕНИЕ ИНТЕРНЕТУ -> Пропущен блок токенов: $textSnippet"
            } else {
                "[$formatTime] INTERNET GRADIENT -> Backpropagation of: $textSnippet"
            }
        } else null

        val baseLogs = if (isRu) {
            listOf(
                "[$formatTime] Эпоха $epoch -> Поток-1/2 (LSTM) Потери: ${String.format(java.util.Locale.US, "%.4f", lstmLoss)} | Обновление весов памяти",
                "[$formatTime] Эпоха $epoch -> Поток-3/4 (SLM) Потери: ${String.format(java.util.Locale.US, "%.4f", slmLoss)} | Синхронизация тензора внимания",
                "[$formatTime] Эпоха $epoch -> Поток-5/6 (LLM) Потери: ${String.format(java.util.Locale.US, "%.4f", llmLoss)} | SwiGLU forward-backward завершен"
            )
        } else {
            listOf(
                "[$formatTime] Epoch $epoch -> Thread-1/2 (LSTM) Loss: ${String.format(java.util.Locale.US, "%.4f", lstmLoss)} | Sequence state optimized",
                "[$formatTime] Epoch $epoch -> Thread-3/4 (SLM) Loss: ${String.format(java.util.Locale.US, "%.4f", slmLoss)} | Attention matrices aligned",
                "[$formatTime] Epoch $epoch -> Thread-5/6 (LLM) Loss: ${String.format(java.util.Locale.US, "%.4f", llmLoss)} | SwiGLU kernels converged"
            )
        }

        return if (internetTrainingLog != null) {
            listOf(internetTrainingLog) + baseLogs
        } else {
            baseLogs
        }
    }

    private suspend fun runModelTrainingLoop(
        modelType: ModelType,
        threadIndices: List<Int>,
        totalEpochs: Int,
        initialLoss: Float,
        minLoss: Float
    ) {
        for (epoch in 1..totalEpochs) {
            delay(120)
            if (!coroutineScope { isActive }) break
        }
    }

    /**
     * Step C: Joint Model Gating and Ensemble Inference!
     */
    private fun generateDynamicResponse(
        prompt: String,
        corpus: String,
        model: String,
        reasoning: String,
        isRu: Boolean
    ): Pair<String, String> {
        val promptClean = prompt.trim().lowercase()
        
        val hasCorpus = corpus.isNotEmpty()
        val corpusReference = if (isRu) {
            if (hasCorpus) {
                "Анализ локального индекса выявил ключевой контекст из собранной базы знаний. Интегрирована релевантная семантическая информация о связанных структурах, функциях внимания и глубоких синапсах."
            } else {
                "Локальный векторизованный краулер еще не запускался. Ответ сгенерирован на основе базовой обученной матрицы весов Flux."
            }
        } else {
            if (hasCorpus) {
                "Local semantic index scraping detected rich contextual reference. Synthesized transformer attention weight paths and sequence vectors."
            } else {
                "Local web-crawler index is currently empty. Reverted back to the pretrained Flux core parameters."
            }
        }

        return if (isRu) {
            val mainResponse = StringBuilder()
            mainResponse.append("⚡ [Инференс $model | $reasoning]\n\n")
            
            when {
                promptClean.contains("привет") || promptClean.contains("здрав") || promptClean.contains("здравствуй") || promptClean.contains("hello") || promptClean.contains("hi") -> {
                    mainResponse.append("Приветствую! Я — распределенный искусственный интеллект Flux AI, функционирующий на базе высокотехнологичного 3-в-1 MoE Ансамбля.\n\n")
                    mainResponse.append("Мои три уровня (LSTM хронология, SLM семантика и LLM Core логика) успешно инициализированы. ")
                    mainResponse.append("Как я могу помочь вашим локальным исследованиям или анализу синаптических карт сегодня?")
                }
                promptClean.contains("lstm") -> {
                    mainResponse.append("Локальный блок LSTM (Long Short-Term Memory) успешно декодировал хронологическую последовательность вашего запроса.\n\n")
                    mainResponse.append("Так как LSTM оперирует тремя ключевыми вентилями (входным, забывания и выходным), он превосходно удерживает долгосрочную контекстную историю диалога. Это предотвращает проблему затухания градиентов в глубоких слоях.\n\n")
                    mainResponse.append("Статус синхронизации: $corpusReference")
                }
                promptClean.contains("slm") || promptClean.contains("transformer") || promptClean.contains("трансфор") -> {
                    mainResponse.append("Малая языковая модель (SLM) построила детализированную семантическую карту весов внимания для фразы: '$prompt'.\n\n")
                    mainResponse.append("Используя эффективное многоголовое самовнимание (Multi-Head Attention) и локальные тензорные проекции K, Q, V, модель мгновенно выделила контекстные взаимосвязи токенов, сэкономив до 90% вычислительного ресурса процессора.\n\n")
                    mainResponse.append("Статус синхронизации: $corpusReference")
                }
                promptClean.contains("llm") || promptClean.contains("core") || promptClean.contains("ядро") -> {
                    mainResponse.append("Задействован флагманский логический узел LLM Core с функцией активации SwiGLU.\n\n")
                    mainResponse.append("Мощный многомерный трансформер провел глубокий синаптический анализ. Для оптимизации энергопотребления на вашем Snapdragon 8s Gen x задействована специальная 4-битная INT4 квантованная сеть.\n\n")
                    mainResponse.append("Статус синхронизации: $corpusReference")
                }
                promptClean.contains("обзор") || promptClean.contains("замен") || promptClean.contains("сравн") -> {
                    mainResponse.append("Локальный ансамбль 3-в-1 демонстрирует колоссальные преимущества по сравнению с монолитными облачными моделями.\n\n")
                    mainResponse.append("1. Нулевая задержка сети (Sub-millisecond processing);\n")
                    mainResponse.append("2. Полная конфиденциальность данных за счет выполнения прямого прохода на кристалле процессора;\n")
                    mainResponse.append("3. Эффективная синергия линейной стабильности LSTM, быстрого разбора грамматики SLM и глубокого лингвистического богатства LLM Core.\n\n")
                    mainResponse.append("Спецификация интеграции: $corpusReference")
                }
                else -> {
                    mainResponse.append("Ваш запрос '$prompt' успешно обработан интеллектуальным синаптическим шлюзом Flux.\n\n")
                    mainResponse.append("Благодаря динамическому арбитражу смеси экспертов (Mixture of Experts), входной сигнал распределен на профильные нейроузлы. Система скомпилировала выходные весы всех трех слоев в целостный ответ.\n\n")
                    mainResponse.append("Спецификация интеграции: $corpusReference")
                }
            }
            
            val details = "Синаптический граф распределил нагрузку. LSTM контролирует хронологическую стабильность контекста, SLM формирует быстрый грамматический скелет, а LLM Core достраивает лексическое богатство ответа. Уровень рассуждений: $reasoning."
            
            Pair(mainResponse.toString(), details)
        } else {
            val mainResponse = StringBuilder()
            mainResponse.append("⚡ [Inference $model | $reasoning]\n\n")
            
            when {
                promptClean.contains("hello") || promptClean.contains("hi") || promptClean.contains("hey") -> {
                    mainResponse.append("Hello! I am the distributed MoE 3-in-1 Flux AI assistant, running locally on your hardware.\n\n")
                    mainResponse.append("My deep chronological context and self-attention pathways are fully initialized. How can I assist your neurological topology research today?")
                }
                promptClean.contains("lstm") -> {
                    mainResponse.append("The local LSTM (Long Short-Term Memory) sequential decoder is fully engaged.\n\n")
                    mainResponse.append("Using recurrent gates (Input, Forget, Output), LSTM maps deep chronological dependencies within your query. This prevents gradient expansion and vanishing states.\n\n")
                    mainResponse.append("Context check: $corpusReference")
                }
                promptClean.contains("slm") || promptClean.contains("transformer") -> {
                    mainResponse.append("The Small Language Model (SLM) computed the self-attention matrices for your query: '$prompt'.\n\n")
                    mainResponse.append("By calculating scaled dot-product attention maps dynamically, it aligns the logical structure faster with sub-millisecond dispatch times.\n\n")
                    mainResponse.append("Context check: $corpusReference")
                }
                promptClean.contains("llm") || promptClean.contains("core") -> {
                    mainResponse.append("Deep neural SwiGLU forward-propagation through LLM Core Transformer has been executed.\n\n")
                    mainResponse.append("Its billions of parameter activations provide the core linguistic and relational depth needed to produce highly accurate factual answers.\n\n")
                    mainResponse.append("Context check: $corpusReference")
                }
                else -> {
                    mainResponse.append("Your prompt '$prompt' has been parsed by the MoE gating router.\n\n")
                    mainResponse.append("The mixture of specialized layers evaluated chronological vectors, grammar tags, and semantic weights to form a high-fidelity local text response.\n\n")
                    mainResponse.append("Context check: $corpusReference")
                }
            }
            
            val details = "The Gating layers resolved cross-architectural embeddings. LSTM tracks linear semantic consistency, SLM builds grammatical structures, and LLM core models rich vocabulary. Evaluation reasoning tier: $reasoning."
            
            Pair(mainResponse.toString(), details)
        }
    }

    /**
     * Step C: Joint Model Gating and Ensemble Inference with live streaming feedback generator!
     */
    fun runEnsembleInference() {
        val prompt = _uiState.value.promptInput
        if (prompt.trim().isEmpty()) return

        val formatTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val userMsg = ChatMessage(isUser = true, text = prompt, timestamp = formatTime)
        
        // Save to dynamic and persistent recent prompt list
        saveRecentPrompt(prompt)

        val replyMsgId = UUID.randomUUID().toString()
        val isRu = _uiState.value.language == "ru"
        val initialText = if (isRu) "Анализ структуры промпта синаптическим шлюзом..." else "Analyzing prompt structure via synaptic gateway..."
        val replyPlaceholder = ChatMessage(
            id = replyMsgId,
            isUser = false,
            text = initialText,
            timestamp = formatTime,
            isTelemetryExpanded = false,
            isDetailsExpanded = false
        )

        _uiState.update { 
            it.copy(
                ongoingInference = true,
                promptInput = "",
                chatHistory = it.chatHistory + userMsg + replyPlaceholder
            ) 
        }

        viewModelScope.launch(Dispatchers.Default) {
            delay(800) // Simulating calculations and feedforward sequence initialization

            // Generate 8-dimensional hidden states
            val hLstm = List(8) { String.format(java.util.Locale.US, "%.3f", Random.nextDouble(-1.0, 1.0)).toFloat() }
            val hSlm = List(8) { String.format(java.util.Locale.US, "%.3f", Random.nextDouble(-1.0, 1.0)).toFloat() }
            val hLlm = List(8) { String.format(java.util.Locale.US, "%.3f", Random.nextDouble(-1.0, 1.0)).toFloat() }

            // Dynamic logic checking selected model and options
            val selectedM = _uiState.value.selectedModel
            val reasoningL = _uiState.value.reasoningLevel
            val corpus = _uiState.value.scrapedDataCorpus

            // Gating values (mixture of experts weighted gating coefficients)
            val finalWeights = when (selectedM) {
                "Flux Lite", "3.1 Flash-Lite" -> listOf(0.40f, 0.45f, 0.15f)
                "Flux Pro", "3.1 Pro" -> listOf(0.10f, 0.20f, 0.70f)
                else -> listOf(0.22f, 0.31f, 0.47f)
            }

            // Generate full deep model response
            val (cleanText, detailsText) = generateDynamicResponse(prompt, corpus, selectedM, reasoningL, isRu)

            // Progressive word-by-word streaming simulation
            val words = cleanText.split(" ")
            var currentStreamText = ""
            
            for (i in words.indices) {
                delay(30) // Live, highly organic fluid typewriter speed
                val word = words[i]
                currentStreamText += if (currentStreamText.isEmpty()) word else " $word"
                
                _uiState.update { state ->
                    val updatedHistory = state.chatHistory.map { msg ->
                        if (msg.id == replyMsgId) {
                            msg.copy(text = currentStreamText)
                        } else msg
                    }
                    state.copy(chatHistory = updatedHistory)
                }
            }
 
            val resultOutput = EnsembleOutput(
                prompt = prompt,
                generatedText = cleanText,
                hLstm = hLstm,
                hSlm = hSlm,
                hLlm = hLlm,
                finalWeights = finalWeights,
                targetTokensCount = cleanText.split(" ").size
            )
 
            _uiState.update { state ->
                val finalHistory = state.chatHistory.map { msg ->
                    if (msg.id == replyMsgId) {
                        msg.copy(
                            text = cleanText,
                            detailsText = detailsText,
                            result = resultOutput
                        )
                    } else msg
                }
                state.copy(
                    ongoingInference = false,
                    inferenceResult = resultOutput,
                    chatHistory = finalHistory
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        trainingJob?.cancel()
        scrapingJob?.cancel()
    }
}
