package me.ash.reader.domain.data

import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import kotlin.math.abs

private val STOPWORDS = setOf(
    "the", "a", "an", "in", "on", "at", "to", "for", "of", "and", "or", "is", "are",
    "was", "were", "be", "been", "this", "that", "it", "its", "with", "as", "by",
    "from", "up", "about", "into", "he", "she", "we", "they", "i", "you", "new",
    "says", "say", "said", "report", "reports", "after", "over", "more", "has", "have",
    "had", "not", "but", "his", "her", "their", "our", "all", "will", "can", "just",
)

private const val SIMILARITY_THRESHOLD = 0.35f
private const val MIN_COMMON_WORDS = 2
private const val WINDOW_MS = 30 * 60 * 1000L

data class FeedSyncResult(val feed: Feed, val newArticles: List<Article>)

object ArticleSimilarity {

    fun deduplicateByRank(syncResults: List<FeedSyncResult>): List<FeedSyncResult> {
        val allEntries: List<Pair<Feed, Article>> = syncResults.flatMap { r ->
            r.newArticles.map { r.feed to it }
        }
        if (allEntries.size <= 1) return syncResults

        // Assign integer cluster IDs
        val clusterOf = mutableMapOf<String, Int>() // articleId -> clusterId
        var nextCluster = 0

        for (i in allEntries.indices) {
            val (feedA, articleA) = allEntries[i]
            if (articleA.id in clusterOf) continue
            val clusterId = nextCluster++
            clusterOf[articleA.id] = clusterId

            for (j in i + 1 until allEntries.size) {
                val (feedB, articleB) = allEntries[j]
                if (feedA.id == feedB.id || articleB.id in clusterOf) continue
                if (abs(articleA.date.time - articleB.date.time) > WINDOW_MS) continue
                if (isSimilar(articleA, articleB)) clusterOf[articleB.id] = clusterId
            }
        }

        // Find the highest-ranked notification-enabled feed per cluster
        val clusterWinner = mutableMapOf<Int, Feed>()
        for ((articleId, clusterId) in clusterOf) {
            val feed = allEntries.first { it.second.id == articleId }.first
            if (!feed.isNotification) continue
            val current = clusterWinner[clusterId]
            if (current == null || ranksBetter(feed, current)) clusterWinner[clusterId] = feed
        }

        return syncResults.map { result ->
            val filtered = result.newArticles.filter { article ->
                val clusterId = clusterOf[article.id] ?: return@filter true
                val winner = clusterWinner[clusterId]
                // Keep if this feed won, or no notification-enabled feed won (suppress anyway
                // since an unranked feed shouldn't override a ranked one in a multi-source cluster)
                winner?.id == result.feed.id
            }
            result.copy(newArticles = filtered)
        }
    }

    // rank=1 beats rank=2; rank=0 (unranked) is always lowest
    private fun ranksBetter(challenger: Feed, incumbent: Feed): Boolean {
        val a = if (challenger.rank == 0) Int.MAX_VALUE else challenger.rank
        val b = if (incumbent.rank == 0) Int.MAX_VALUE else incumbent.rank
        return a < b
    }

    private fun isSimilar(a: Article, b: Article): Boolean {
        val wordsA = tokenize("${a.title} ${a.shortDescription}")
        val wordsB = tokenize("${b.title} ${b.shortDescription}")
        val common = wordsA intersect wordsB
        if (common.size < MIN_COMMON_WORDS) return false
        return common.size.toFloat() / (wordsA union wordsB).size.toFloat() >= SIMILARITY_THRESHOLD
    }

    private fun tokenize(text: String): Set<String> = text
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .split(Regex("\\s+"))
        .filter { it.length > 2 && it !in STOPWORDS }
        .toSet()
}
