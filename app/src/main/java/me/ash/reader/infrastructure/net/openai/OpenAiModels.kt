package me.ash.reader.infrastructure.net.openai

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val maxTokens: Int? = null
)

data class ChatCompletionResponse(
    val choices: List<Choice>,
    val usage: Usage? = null
)

data class Choice(
    val message: ChatMessage,
    val finishReason: String? = null
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

data class ModelsResponse(
    val `object`: String,
    val data: List<Model>
)

data class Model(
    val id: String,
    val `object`: String = "model",
    val created: Long? = null,
    val ownedBy: String? = null
)
