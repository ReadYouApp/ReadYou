package me.ash.reader.infrastructure.translation

import android.content.Context
import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.preference.SettingsProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val LANGUAGE_ID_SAMPLE_LENGTH = 500
private const val BATCH_SEPARATOR = "\n\uFFFF\n"
private const val TRANSLATION_CACHE_SIZE = 30
private const val CHUNK_SIZE = 50

@Singleton
class TranslationService @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
) {

    private val modelManager = RemoteModelManager.getInstance()
    private val languageIdentifier = LanguageIdentification.getClient()
    private val translatorCache = mutableMapOf<String, Translator>()

    /** Cache of translated content keyed by "articleId_targetLanguage". */
    private val translationCache = LruCache<String, String>(TRANSLATION_CACHE_SIZE)

    /**
     * Get the target language tag based on the current app language setting.
     * Falls back to device locale if "Use Device Languages" is selected.
     */
    fun getTargetLanguageTag(): String {
        val targetPref = settingsProvider.settings.translateTargetLanguage
        if (targetPref.value != 0) {
            return targetPref.toLocale()?.let {
                TranslateLanguage.fromLanguageTag(it.toLanguageTag())
            } ?: TranslateLanguage.ENGLISH
        }

        val languagePref = settingsProvider.settings.languages
        val locale = languagePref.toLocale() ?: Locale.getDefault()
        return TranslateLanguage.fromLanguageTag(locale.toLanguageTag())
            ?: TranslateLanguage.fromLanguageTag(locale.language)
            ?: TranslateLanguage.ENGLISH
    }

    /**
     * Identify the language of HTML content by stripping tags and decoding entities first.
     * This is the preferred method when working with article HTML content,
     * because raw HTML entities (e.g. &#xFC;) confuse ML Kit's language identifier.
     */
    suspend fun identifyHtmlLanguage(html: String): String {
        if (html.isBlank()) return "und"
        val plainText = Jsoup.parse(html).text()
        return identifyLanguage(plainText)
    }

    /**
     * Identify the source language of a text using ML Kit.
     * Uses only the first [LANGUAGE_ID_SAMPLE_LENGTH] chars for speed.
     * Returns "und" if undetermined.
     */
    suspend fun identifyLanguage(text: String): String = suspendCancellableCoroutine { cont ->
        if (text.isBlank()) {
            cont.resume("und")
            return@suspendCancellableCoroutine
        }
        val sample = text.take(LANGUAGE_ID_SAMPLE_LENGTH)
        languageIdentifier.identifyLanguage(sample)
            .addOnSuccessListener { languageCode ->
                cont.resume(languageCode)
            }
            .addOnFailureListener {
                cont.resume("und")
            }
    }

    /**
     * Check whether the required translation models (source & target) are already downloaded.
     * Returns true only if both models are available locally.
     */
    suspend fun areModelsDownloaded(sourceLanguage: String, targetLanguage: String): Boolean =
        withContext(ioDispatcher) {
            coroutineScope {
                val sourceDownloaded = async { isModelDownloaded(sourceLanguage) }
                val targetDownloaded = async { isModelDownloaded(targetLanguage) }
                sourceDownloaded.await() && targetDownloaded.await()
            }
        }

    /**
     * Get a cached translation result, or null if not cached.
     */
    fun getCachedTranslation(articleId: String, targetLanguage: String): String? {
        return translationCache.get("${articleId}_$targetLanguage")
    }

    /**
     * Translate HTML content, preserving HTML structure.
     * Only text nodes are translated; tags and attributes are kept intact.
     * Uses batch translation for efficiency.
     */
    suspend fun translateHtml(
        htmlContent: String,
        targetLanguage: String = getTargetLanguageTag(),
        articleId: String? = null,
        onProgress: (Float) -> Unit = {},
    ): Result<String> = withContext(ioDispatcher) {
        // Check cache first
        if (articleId != null) {
            val cached = getCachedTranslation(articleId, targetLanguage)
            if (cached != null) {
                Timber.d("Translation cache hit for article=$articleId, lang=$targetLanguage")
                return@withContext Result.success(cached)
            }
        }
        runCatching {
            val document = Jsoup.parseBodyFragment(htmlContent)
            val bodyText = document.body().text()
            val sourceLanguage = identifyLanguage(bodyText)
            if (sourceLanguage == "und" || sourceLanguage == targetLanguage) {
                return@withContext Result.success(htmlContent)
            }

            translateElement(document.body(), sourceLanguage, targetLanguage, onProgress)
            val result = document.body().html()

            // Store in cache
            if (articleId != null) {
                translationCache.put("${articleId}_$targetLanguage", result)
            }
            result
        }
    }

    /**
     * Translate plain text.
     */
    suspend fun translateText(
        text: String,
        targetLanguage: String = getTargetLanguageTag(),
    ): Result<String> = withContext(ioDispatcher) {
        if (text.isBlank()) return@withContext Result.success(text)
        runCatching {
            val sourceLanguage = identifyLanguage(text)
            if (sourceLanguage == "und" || sourceLanguage == targetLanguage) {
                return@withContext Result.success(text)
            }

            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
            ensureModelDownloaded(translator)
            suspendCancellableCoroutine { cont ->
                translator.translate(text)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(text) }
            }
        }
    }

    /**
     * Translate plain text with an explicit source language (skips language detection).
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): Result<String> = withContext(ioDispatcher) {
        if (text.isBlank()) return@withContext Result.success(text)
        runCatching {
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
            ensureModelDownloaded(translator)
            suspendCancellableCoroutine { cont ->
                translator.translate(text)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(text) }
            }
        }
    }

    /**
     * Download a language model for offline use.
     */
    suspend fun downloadModel(languageTag: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(languageTag)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            suspendCancellableCoroutine { cont ->
                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { throw it }
            }
            translator.close()
        }
    }

    /**
     * Check if a language model has been downloaded.
     */
    suspend fun isModelDownloaded(languageTag: String): Boolean = withContext(ioDispatcher) {
        suspendCancellableCoroutine { cont ->
            val model = TranslateRemoteModel.Builder(languageTag).build()
            modelManager.isModelDownloaded(model)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(false) }
        }
    }

    /**
     * Delete a downloaded language model.
     */
    suspend fun deleteModel(languageTag: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val model = TranslateRemoteModel.Builder(languageTag).build()
            suspendCancellableCoroutine { cont ->
                modelManager.deleteDownloadedModel(model)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { throw it }
            }
        }
    }

    /**
     * Get a list of downloaded language models.
     */
    suspend fun getDownloadedModels(): List<String> = withContext(ioDispatcher) {
        suspendCancellableCoroutine { cont ->
            modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { models ->
                    cont.resume(models.map { it.language })
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
    }

    /**
     * Download models and then translate. Called after user confirms the download.
     */
    suspend fun downloadAndTranslateHtml(
        htmlContent: String,
        sourceLanguage: String,
        targetLanguage: String,
        articleId: String? = null,
        onProgress: (Float) -> Unit = {},
    ): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
            ensureModelDownloaded(translator)

            val document = Jsoup.parseBodyFragment(htmlContent)
            translateElement(document.body(), sourceLanguage, targetLanguage, onProgress)
            val result = document.body().html()

            if (articleId != null) {
                translationCache.put("${articleId}_$targetLanguage", result)
            }
            result
        }
    }

    private fun getOrCreateTranslator(sourceLanguage: String, targetLanguage: String): Translator {
        val key = "${sourceLanguage}_$targetLanguage"
        return translatorCache.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
            Translation.getClient(options)
        }
    }

    private suspend fun ensureModelDownloaded(translator: Translator) {
        val wifiOnly = settingsProvider.settings.translateWifiOnly.value
        val conditions = if (wifiOnly) {
            DownloadConditions.Builder().requireWifi().build()
        } else {
            DownloadConditions.Builder().build()
        }
        suspendCancellableCoroutine { cont ->
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { throw it }
        }
    }

    /**
     * Translate all text nodes in an element using chunked batch translation.
     * Splits text nodes into groups of [CHUNK_SIZE], translates each chunk as a batch,
     * and reports progress after each chunk. DOM is modified in-place; the caller
     * extracts the final HTML only after this method completes (no flickering).
     */
    private suspend fun translateElement(
        element: Element,
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Float) -> Unit = {},
    ) {
        val textNodes = mutableListOf<TextNode>()
        collectTextNodes(element, textNodes)

        if (textNodes.isEmpty()) return

        val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
        ensureModelDownloaded(translator)

        val nonBlankNodes = textNodes.filter { it.wholeText.isNotBlank() }
        if (nonBlankNodes.isEmpty()) return

        // Split nodes into chunks for progress reporting
        val chunks = nonBlankNodes.chunked(CHUNK_SIZE)
        val totalChunks = chunks.size

        chunks.forEachIndexed { chunkIndex, chunk ->
            translateChunk(chunk, translator)
            onProgress((chunkIndex + 1).toFloat() / totalChunks)
        }
    }

    /**
     * Translate a single chunk of text nodes using batch concatenation.
     * Falls back to one-by-one if the separator is corrupted during translation.
     */
    private suspend fun translateChunk(nodes: List<TextNode>, translator: Translator) {
        val batchText = nodes.joinToString(BATCH_SEPARATOR) { it.wholeText }
        val translatedBatch = suspendCancellableCoroutine { cont ->
            translator.translate(batchText)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(batchText) }
        }

        val translatedParts = translatedBatch.split(BATCH_SEPARATOR)

        if (translatedParts.size == nodes.size) {
            nodes.forEachIndexed { index, node ->
                node.text(translatedParts[index])
            }
        } else {
            // Fallback: translate one-by-one if batch separator was corrupted
            Timber.w("Batch split mismatch: expected ${nodes.size}, got ${translatedParts.size}. Falling back.")
            for (node in nodes) {
                val translated = suspendCancellableCoroutine { cont ->
                    translator.translate(node.wholeText)
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resume(node.wholeText) }
                }
                node.text(translated)
            }
        }
    }

    private fun collectTextNodes(element: Element, result: MutableList<TextNode>) {
        for (child in element.childNodes()) {
            when (child) {
                is TextNode -> if (child.wholeText.isNotBlank()) result.add(child)
                is Element -> collectTextNodes(child, result)
            }
        }
    }

    fun close() {
        translatorCache.values.forEach { it.close() }
        translatorCache.clear()
        languageIdentifier.close()
    }
}
