package com.example.config

/**
 * Configuration schemas and hyper-parameters for the 3-in-1 Local Ensemble System.
 * Translates architectural specifications directly to concrete strongly-typed models.
 */

data class ScraperConfig(
    val defaultKeywords: List<String> = listOf("Wikipedia AI", "Neural Networks", "Deep Learning", "Transformer Attention"),
    val scraperThreads: Int = 1,
    val maxUrlsPerKeyword: Int = 3,
    val userAgent: String = "Mozilla/5.0 (Android; Mobile; rv:100.0) LocalEnsemble/1.0",
    val timeoutMs: Int = 5000,
    val stripHtmlTags: Boolean = true
)

enum class ModelType {
    LSTM,
    SLM,
    LLM,
    ARBITRATOR_MOE
}

data class ModelHyperParameters(
    val vocabSize: Int = 256, // Working character-level vocab
    val embeddingDim: Int = 64,
    val hiddenDim: Int = 128,
    val numLayers: Int = 2,
    val numHeads: Int = 4, // Applied to SLM & LLM
    val learningRate: Double = 0.005,
    val l2Regularization: Double = 1e-4,
    val dropout: Float = 0.1f
)

data class ThreadAllocation(
    val modelType: ModelType,
    val assignedThreads: Int,
    val coreIds: List<Int>,
    val processPriority: Int // Thread priority mapping
)

data class EnsembleSystemConfig(
    val lstmConfig: ModelHyperParameters = ModelHyperParameters(
        vocabSize = 256,
        embeddingDim = 48,
        hiddenDim = 64,
        numLayers = 2,
        learningRate = 0.01
    ),
    val slmConfig: ModelHyperParameters = ModelHyperParameters(
        vocabSize = 256,
        embeddingDim = 64,
        hiddenDim = 96,
        numLayers = 2,
        numHeads = 4,
        learningRate = 0.005
    ),
    val llmConfig: ModelHyperParameters = ModelHyperParameters( // Advanced features: SwiGLU, RoPE, RMSNorm
        vocabSize = 256,
        embeddingDim = 96,
        hiddenDim = 128,
        numLayers = 3,
        numHeads = 6,
        learningRate = 0.003
    ),
    val arbitratorConfig: ModelHyperParameters = ModelHyperParameters(
        embeddingDim = 288, // 48 (LSTM) + 96 (SLM) + 144 (LLM hidden concat dimensions) or adaptive
        hiddenDim = 64,
        learningRate = 0.01
    ),
    val scraper: ScraperConfig = ScraperConfig(),
    val threadAllocations: List<ThreadAllocation> = listOf(
        ThreadAllocation(ModelType.LSTM, assignedThreads = 2, coreIds = listOf(0, 1), processPriority = 5),
        ThreadAllocation(ModelType.SLM, assignedThreads = 2, coreIds = listOf(2, 3), processPriority = 5),
        ThreadAllocation(ModelType.LLM, assignedThreads = 2, coreIds = listOf(4, 5), processPriority = -10) // High priority for deep transformer
    ),
    val totalParallelThreads: Int = 6
)
