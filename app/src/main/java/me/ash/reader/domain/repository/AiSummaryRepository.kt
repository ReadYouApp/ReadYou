package me.ash.reader.domain.repository

import me.ash.reader.infrastructure.net.ApiResult
import me.ash.reader.infrastructure.net.openai.OpenAiApiService
import me.ash.reader.infrastructure.net.openai.ChatCompletionRequest
import me.ash.reader.infrastructure.net.openai.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSummaryRepository @Inject constructor() {

    suspend fun fetchAvailableModels(
        baseUrl: String,
        apiKey: String
    ): ApiResult<List<String>> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)
            val response = service.getModels()

            if (response.isSuccessful && response.body() != null) {
                val modelIds = response.body()!!.data.map { it.id }
                ApiResult.Success(modelIds)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                ApiResult.BizError(Exception(errorMsg))
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    suspend fun summarizeArticle(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        articleContent: String
    ): ApiResult<String> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)
            
            val messages = listOf(
                ChatMessage(role = "user", content = "$prompt\n\n$articleContent")
            )

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = 0.7,
                maxTokens = 2000
            )

            val response = service.createChatCompletion(request)

            if (response.isSuccessful && response.body() != null) {
                val choices = response.body()!!.choices
                if (choices.isNotEmpty()) {
                    val summary = choices[0].message.content
                    ApiResult.Success(summary)
                } else {
                    ApiResult.BizError(Exception("No choices returned from API"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                ApiResult.BizError(Exception(errorMsg))
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }
}
