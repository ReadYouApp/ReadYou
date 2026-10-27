package me.ash.reader.infrastructure.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

interface NetworkDataSource {

    @GET
    suspend fun getReleaseLatest(@Url url: String): Response<LatestRelease>

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody

    companion object {

        private var instance: NetworkDataSource? = null

        fun getInstance(): NetworkDataSource {
            return instance ?: synchronized(this) {
                instance ?: Retrofit.Builder()
                    .baseUrl("https://api.github.com/")
                    .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json; charset=UTF8".toMediaType()))
                    .build().create(NetworkDataSource::class.java).also {
                        instance = it
                    }
            }
        }
    }
}

fun ResponseBody.downloadToFileWithProgress(saveFile: File): Flow<Download> =
    flow {
        emit(Download.Progress(0))

        // flag to delete file if download errors or is cancelled
        var deleteFile = true

        try {
            byteStream().use { inputStream ->
                saveFile.outputStream().use { outputStream ->
                    val totalBytes = contentLength()
                    val data = ByteArray(8_192)
                    var progressBytes = 0L

                    while (true) {
                        val bytes = inputStream.read(data)

                        if (bytes == -1) {
                            break
                        }

                        outputStream.channel
                        outputStream.write(data, 0, bytes)
                        progressBytes += bytes

                        emit(Download.Progress(percent = ((progressBytes * 100) / totalBytes).toInt()))
                    }

                    when {
                        progressBytes < totalBytes ->
                            throw Exception("missing bytes")

                        progressBytes > totalBytes ->
                            throw Exception("too many bytes")

                        else ->
                            deleteFile = false
                    }
                }
            }

            emit(Download.Finished(saveFile))
        } finally {
            // check if download was successful

            if (deleteFile) {
                saveFile.delete()
            }
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

@Serializable
data class LatestRelease(
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("tag_name") val tagName: String? = null,
    val name: String? = null,
    val draft: Boolean? = null,
    val prerelease: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val assets: List<AssetsItem>? = null,
    val body: String? = null,
)

@Serializable
data class AssetsItem(
    val name: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    val size: Int? = null,
    @SerialName("download_count") val downloadCount: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("browser_download_url") val browserDownloadUrl: String? = null,
)

sealed class Download {
    object NotYet : Download()
    data class Progress(val percent: Int) : Download()
    data class Finished(val file: File) : Download()
}
