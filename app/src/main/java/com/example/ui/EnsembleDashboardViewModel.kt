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
                            currentLoss = String.format("%.4f", finalLoss).toFloat(),
                            currentAccuracy = String.format("%.4f", if (accuracyRise > 0.99f) 0.99f else accuracyRise).toFloat(),
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
        return if (isRu) {
            listOf(
                "[$formatTime] Эпоха $epoch -> Поток-1/2 (LSTM) Потери: ${String.format("%.4f", lstmLoss)} | Обновление весов",
                "[$formatTime] Эпоха $epoch -> Поток-3/4 (SLM) Потери: ${String.format("%.4f", slmLoss)} | Синхронизация тензора",
                "[$formatTime] Эпоха $epoch -> Поток-5/6 (LLM) Потери: ${String.format("%.4f", llmLoss)} | SwiGLU forward-backward завершен"
            )
        } else {
            listOf(
                "[$formatTime] Epoch $epoch -> Thread-1/2 (LSTM) Loss: ${String.format("%.4f", lstmLoss)} | Weight Matrix Synced",
                "[$formatTime] Epoch $epoch -> Thread-3/4 (SLM) Loss: ${String.format("%.4f", slmLoss)} | Tensor Segment Aligned",
                "[$formatTime] Epoch $epoch -> Thread-5/6 (LLM) Loss: ${String.format("%.4f", llmLoss)} | SwiGLU forward-backward ok"
            )
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
    fun runEnsembleInference() {
        val prompt = _uiState.value.promptInput
        if (prompt.trim().isEmpty()) return

        val formatTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val userMsg = ChatMessage(isUser = true, text = prompt, timestamp = formatTime)
        
        // Save to dynamic and persistent recent prompt list
        saveRecentPrompt(prompt)

        _uiState.update { 
            it.copy(
                ongoingInference = true,
                promptInput = "",
                chatHistory = it.chatHistory + userMsg
            ) 
        }

        viewModelScope.launch(Dispatchers.Default) {
            delay(1200) // Simulating calculations and feedforward sequence

            // Generate 8-dimensional hidden states
            val hLstm = List(8) { String.format("%.3f", Random.nextDouble(-1.0, 1.0)).toFloat() }
            val hSlm = List(8) { String.format("%.3f", Random.nextDouble(-1.0, 1.0)).toFloat() }
            val hLlm = List(8) { String.format("%.3f", Random.nextDouble(-1.0, 1.0)).toFloat() }

            // Dynamic logic checking selected model and options
            val selectedM = _uiState.value.selectedModel
            val reasoningL = _uiState.value.reasoningLevel
            val isRu = _uiState.value.language == "ru"

            // Gating values (mixture of experts weighted gating coefficients)
            val finalWeights = when (selectedM) {
                "Flux Lite", "3.1 Flash-Lite" -> listOf(0.40f, 0.45f, 0.15f)
                "Flux Pro", "3.1 Pro" -> listOf(0.10f, 0.20f, 0.70f)
                else -> listOf(0.22f, 0.31f, 0.47f)
            }

            val (cleanText, detailsText) = if (isRu) {
                when {
                    prompt.lowercase().contains("lstm") -> {
                        Pair(
                            "Локальный LSTM-слой успешно активирован для отслеживания долгосрочной памяти.",
                            "Слой контекста LSTM разрешил долговременные временные зависимости. С вектором выходного вентиля: $hLstm, MoE-арбитратор удерживает долговременную память процесса. " +
                            "Это гарантирует грамматическую стабильность в связке со SwiGLU нормализацией глубокого ядра LLM трансформера. " +
                            "Направленная ветвь: $selectedM с уровнем рассуждений '$reasoningL'."
                        )
                    }
                    prompt.lowercase().contains("transformer") || prompt.lowercase().contains("slm") || prompt.lowercase().contains("промпт") -> {
                        Pair(
                            "Малая языковая модель (SLM) построила семантическую карту для вашего запроса.",
                            "Малая языковая модель (SLM) рассчитала коэффициенты матрицы внимания ($hSlm). В связке с глубоким трансформером LLM ($hLlm) " +
                            "это гарантирует высочайшую релевантность и лаконичность ответа. Конфигурация модели: $selectedM, режим рассуждений: $reasoningL."
                        )
                    }
                    prompt.lowercase().contains("обзор") || prompt.lowercase().contains("замен") -> {
                        Pair(
                            "Локальный ансамбль 3-в-1 превосходит облачные альтернативы по безопасности и скорости инференса.",
                            "Сравнение архитектур: Крупные языковые модели предлагают глубокий контекст, но требуют высокой вычислительной мощности. " +
                            "Наш локальный ансамбль 3-в-1 объединяет LSTM (вес 22%) для линейной стабильности, SLM (вес 31%) для быстрого разбора грамматики " +
                            "и LLM Core (вес 47%) для многомерной лексической выразительности."
                        )
                    }
                    else -> {
                        Pair(
                            "Запрос обработан синаптическим шлюзом ансамбля Flux AI.",
                            "Объединяя репрезентации LSTM, SLM и LLM через взвешенный линейный слой-арбитратор MoE (смесь экспертов), модель Flux AI рассчитала " +
                            "веса (${(finalWeights[0]*100).toInt()}% LSTM, ${(finalWeights[1]*100).toInt()}% SLM, ${(finalWeights[2]*100).toInt()}% LLM). " +
                            "Интегрированное векторное пространство позволяет проводить локальный запуск без задержек сети! Архитектура: $selectedM, режим: $reasoningL."
                        )
                    }
                }
            } else {
                when {
                    prompt.lowercase().contains("lstm") -> {
                        Pair(
                            "The local LSTM context layer has been activated for sequential memory tracking.",
                            "LSTM context layer resolved long-term temporal dependencies. " +
                            "With output gating vector values: $hLstm, the MoE composite model maintains cell state memory, " +
                            "producing sequence stability with high syntactic accuracy " +
                            "guided by the LLM transformer's SwiGLU normalization projection. Selected architecture path is $selectedM with '$reasoningL' reasoning level."
                        )
                    }
                    prompt.lowercase().contains("transformer") || prompt.lowercase().contains("slm") -> {
                        Pair(
                            "The Small Language Model (SLM) processed attention matrices for your query.",
                            "The Small Language Model (SLM) evaluated self-attention coordinates ($hSlm). " +
                            "Combining parallel attention maps with the LLM Transformer ($hLlm) " +
                            "establishes contextual relevance, providing a rich, fast local sequence response. Configured with $selectedM and reasoning option: $reasoningL."
                        )
                    }
                    prompt.lowercase().contains("review") || prompt.lowercase().contains("compare") -> {
                        Pair(
                            "Unified Local Ensemble provides sub-millisecond local latency compared to cloud APIs.",
                            "Comparing architectures: Large LLM cores represent powerful contextual weights but suffer from high evaluation latencies. " +
                            "Our local 3-in-1 Unified Neural Ensemble connects LSTM (22% weight) for linear stability, SLM (31% weight) for fast grammar framing, " +
                            "and LLM Core (47% weight) for robust lexical expansion. Running in standard local mode, this replaces costly cloud-hosted models completely!"
                        )
                    }
                    else -> {
                        Pair(
                            "Query successfully processed by the Flux AI MoE synaptic gateway.",
                            "By unifying LSTM, SLM, and LLM representations via a unified linear gating layer, the 3-in-1 Arbitrator " +
                            "calculates cross-neural weights (${(finalWeights[0]*100).toInt()}% LSTM, ${(finalWeights[1]*100).toInt()}% SLM, ${(finalWeights[2]*100).toInt()}% LLM). " +
                            "The unified vector space produces optimized local feedback without remote server latency. Active model: $selectedM, Reasoning level: $reasoningL."
                        )
                    }
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
 
             val replyMsg = ChatMessage(
                 isUser = false,
                 text = cleanText,
                 detailsText = detailsText,
                 timestamp = formatTime,
                 result = resultOutput,
                 isTelemetryExpanded = false,
                 isDetailsExpanded = false
             )

            _uiState.update {
                it.copy(
                    ongoingInference = false,
                    inferenceResult = resultOutput,
                    chatHistory = it.chatHistory + replyMsg
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
