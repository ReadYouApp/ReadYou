package me.ash.reader.domain.repository

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleMeta
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.ImportantNum
import java.util.Date

@Dao
interface ArticleDao {

    @Query(
        """
        UPDATE article SET isStarred = :isStarred 
        WHERE accountId = :accountId
        AND id in (:ids)
        """
    )
    fun markAsStarredByIdSet(
        accountId: Int,
        ids: Set<String>,
        isStarred: Boolean,
    ): Int

    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE accountId = :accountId
        AND id in (:ids)
        """
    )
    fun markAsReadByIdSet(
        accountId: Int,
        ids: Set<String>,
        isUnread: Boolean,
    ): Int

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT count(1)
        FROM article
        WHERE feedId = :feedId
        AND isStarred = :isStarred
        AND accountId = :accountId
        """
    )
    fun countByFeedIdWhenIsStarred(
        accountId: Int,
        feedId: String,
        isStarred: Boolean,
    ): Int

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT count(1)
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.isStarred = :isStarred
        AND a.accountId = :accountId
        """
    )
    fun countByGroupIdWhenIsStarred(
        accountId: Int,
        groupId: String,
        isStarred: Boolean,
    ): Int


    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId IN (
            SELECT id FROM feed WHERE groupId = :groupId
        )
        AND isUnread = :isUnread
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleByGroupIdWhenIsUnread(
        accountId: Int,
        text: String,
        groupId: String,
        isUnread: Boolean,
        sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId IN (
            SELECT id FROM feed WHERE groupId = :groupId
        )
        AND isStarred = :isStarred
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleByGroupIdWhenIsStarred(
        accountId: Int,
        text: String,
        groupId: String,
        isStarred: Boolean,
        sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId IN (
            SELECT id FROM feed WHERE groupId = :groupId
        )
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleByGroupIdWhenAll(
        accountId: Int, text: String, groupId: String, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId = :feedId
        AND isUnread = :isUnread
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleByFeedIdWhenIsUnread(
        accountId: Int,
        text: String,
        feedId: String,
        isUnread: Boolean,
        sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId = :feedId
        AND isStarred = :isStarred
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleByFeedIdWhenIsStarred(
        accountId: Int,
        text: String,
        feedId: String,
        isStarred: Boolean,
        sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND feedId = :feedId 
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleByFeedIdWhenAll(
        accountId: Int, text: String, feedId: String, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND isUnread = :isUnread
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleWhenIsUnread(
        accountId: Int, text: String, isUnread: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND isStarred = :isStarred
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleWhenIsStarred(
        accountId: Int, text: String, isStarred: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId 
        AND (
            title LIKE '%' || :text || '%'
            OR shortDescription LIKE '%' || :text || '%'
            OR fullContent LIKE '%' || :text || '%'
        )
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun searchArticleWhenAll(
        accountId: Int, text: String, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>


    @Query(
        """
        DELETE FROM article
        WHERE accountId = :accountId
        AND updateAt < :before
        AND isUnread = 0
        AND isStarred = 0
        """
    )
    suspend fun deleteAllArchivedBeforeThan(
        accountId: Int,
        before: Date,
    )

    @Query(
        """
        select * FROM article
        WHERE accountId = :accountId
        AND updateAt < :before
        AND isUnread = 0
        AND isStarred = 0
        """
    )
    suspend fun queryArchivedArticleBefore(
        accountId: Int,
        before: Date,
    ): List<Article>

    @Transaction
    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE accountId = :accountId
        AND date < :before
        AND isUnread != :isUnread
        """
    )
    suspend fun markAllAsRead(
        accountId: Int,
        isUnread: Boolean,
        before: Date,
    )

    @Transaction
    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE feedId IN (
            SELECT id FROM feed 
            WHERE groupId = :groupId
        )
        AND accountId = :accountId
        AND isUnread != :isUnread
        AND date < :before
        """
    )
    suspend fun markAllAsReadByGroupId(
        accountId: Int,
        groupId: String,
        isUnread: Boolean,
        before: Date,
    )

    @Transaction
    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE feedId = :feedId
        AND accountId = :accountId
        AND isUnread != :isUnread
        AND date < :before
        """
    )
    suspend fun markAllAsReadByFeedId(
        accountId: Int,
        feedId: String,
        isUnread: Boolean,
        before: Date,
    )

    @Transaction
    @Query(
        """
        UPDATE article SET isUnread = :isUnread 
        WHERE id = :articleId
        AND accountId = :accountId
        """
    )
    suspend fun markAsReadByArticleId(
        accountId: Int,
        articleId: String,
        isUnread: Boolean,
    )

    @Query(
        """
        UPDATE article SET isStarred = :isStarred 
        WHERE id = :articleId
        AND accountId = :accountId
        """
    )
    suspend fun markAsStarredByArticleId(
        accountId: Int,
        articleId: String,
        isStarred: Boolean,
    )

    @Query(
        """
        DELETE FROM article
        WHERE accountId = :accountId
        AND feedId = :feedId
        AND (isStarred = :includeStarred OR :includeStarred = 1)
        """
    )
    suspend fun deleteByFeedId(accountId: Int, feedId: String, includeStarred: Boolean = false)

    @Query(
        """
        DELETE FROM article
        WHERE id IN (
            SELECT a.id FROM article AS a, feed AS b, `group` AS c
            WHERE a.accountId = :accountId
            AND a.feedId = b.id
            AND b.groupId = c.id
            AND c.id = :groupId
            AND (a.isStarred = :includeStarred OR :includeStarred = 1)
        )
        """
    )
    suspend fun deleteByGroupId(accountId: Int, groupId: String, includeStarred: Boolean = false)

    @Query(
        """
        DELETE FROM article
        WHERE accountId = :accountId
        """
    )
    suspend fun deleteByAccountId(accountId: Int)


    @Transaction
    @Query(
        """
        SELECT feedId, COUNT(*) AS important
        FROM article
        WHERE isUnread = :isUnread
        AND accountId = :accountId
        GROUP BY feedId
        """
    )
    fun queryImportantCountWhenIsUnread(
        accountId: Int,
        isUnread: Boolean,
    ): Flow<Map<@MapColumn("feedId") String, @MapColumn("important") Int>>

    @Transaction
    @Query(
        """
        SELECT feedId, COUNT(*) AS important
        FROM article
        WHERE isStarred = :isStarred
        AND accountId = :accountId
        GROUP BY feedId
        """
    )
    fun queryImportantCountWhenIsStarred(
        accountId: Int,
        isStarred: Boolean,
    ): Flow<Map<@MapColumn("feedId") String, @MapColumn("important") Int>>

    @Transaction
    @Query(
        """
        SELECT feedId, COUNT(*) AS important
        FROM article
        WHERE accountId = :accountId
        GROUP BY feedId
        """
    )
    fun queryImportantCountWhenIsAll(accountId: Int):
            Flow<Map<@MapColumn("feedId") String, @MapColumn("important") Int>>


    @Transaction
    @Query(
        """
        SELECT * FROM article 
        WHERE accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryArticleWithFeedWhenIsAll(
        accountId: Int, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE isStarred = :isStarred 
        AND accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryArticleWithFeedWhenIsStarred(
        accountId: Int, isStarred: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article 
        WHERE isUnread = :isUnread 
        AND accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryArticleWithFeedWhenIsUnread(
        accountId: Int, isUnread: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.img, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater, a.updateAt 
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN a.date END ASC,
            CASE WHEN :sortAscending = 0 THEN a.date END DESC
        """
    )
    fun queryArticleWithFeedByGroupIdWhenIsAll(
        accountId: Int, groupId: String, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.img, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater, a.updateAt 
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.isStarred = :isStarred
        AND a.accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN a.date END ASC,
            CASE WHEN :sortAscending = 0 THEN a.date END DESC
        """
    )
    fun queryArticleWithFeedByGroupIdWhenIsStarred(
        accountId: Int, groupId: String, isStarred: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.img, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater, a.updateAt 
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.isUnread = :isUnread
        AND a.accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN a.date END ASC,
            CASE WHEN :sortAscending = 0 THEN a.date END DESC
        """
    )
    fun queryArticleWithFeedByGroupIdWhenIsUnread(
        accountId: Int, groupId: String, isUnread: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE feedId = :feedId
        AND accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryArticleWithFeedByFeedIdWhenIsAll(
        accountId: Int, feedId: String, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * from article 
        WHERE feedId = :feedId 
        AND isStarred = :isStarred
        AND accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryArticleWithFeedByFeedIdWhenIsStarred(
        accountId: Int, feedId: String, isStarred: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>

    @Transaction
    @Query(
        """
        SELECT * FROM article 
        WHERE feedId = :feedId 
        AND isUnread = :isUnread
        AND accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryArticleWithFeedByFeedIdWhenIsUnread(
        accountId: Int, feedId: String, isUnread: Boolean, sortAscending: Boolean = false
    ): PagingSource<Int, ArticleWithFeed>


    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT a.id, a.date, a.title, a.author, a.rawDescription, 
        a.shortDescription, a.fullContent, a.img, a.link, a.feedId, 
        a.accountId, a.isUnread, a.isStarred, a.isReadLater, a.updateAt 
        FROM article AS a LEFT JOIN feed AS b 
        ON a.feedId = b.id
        WHERE a.feedId = :feedId 
        AND a.accountId = :accountId
        ORDER BY date DESC LIMIT 1
        """
    )
    suspend fun queryLatestByFeedId(accountId: Int, feedId: String): Article?


    @Query(
        """
        SELECT * from article 
        WHERE link in (:linkList)
        AND feedId = :feedId
        AND accountId = :accountId
        """
    )
    suspend fun queryArticlesByLinks(
        linkList: List<String>,
        feedId: String,
        accountId: Int,
    ): List<Article>

    @Query(
        """
        SELECT a.*
        FROM article AS a
        LEFT JOIN feed AS f ON a.feedId = f.id
        WHERE f.accountId = :accountId
        AND f.isFullContent = 1
        AND a.isUnread = 1
        """
    )
    suspend fun queryUnreadFullContentArticles(
        accountId: Int,
    ): List<Article>

    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE id = :id
        """
    )
    suspend fun queryById(id: String): ArticleWithFeed?


    @Transaction
    @Query(
        """
        SELECT id, isUnread, isStarred FROM article
        WHERE accountId = :accountId
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryMetadataAll(
        accountId: Int, sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT id, isUnread, isStarred FROM article
        WHERE accountId = :accountId
        AND isUnread = :isUnread
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryMetadataAll(
        accountId: Int, isUnread: Boolean, sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT id, isUnread, isStarred FROM article
        WHERE accountId = :accountId
        AND date < :before
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryMetadataAll(
        accountId: Int, before: Date, sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT id, isUnread, isStarred FROM article
        WHERE accountId = :accountId
        AND isUnread = :isUnread
        AND date < :before
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryMetadataAll(
        accountId: Int, isUnread: Boolean, before: Date, sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT id, isUnread, isStarred FROM article
        WHERE accountId = :accountId
        AND feedId = :feedId
        AND isUnread = :isUnread
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryMetadataByFeedId(
        accountId: Int, feedId: String, isUnread: Boolean, sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT id, isUnread, isStarred FROM article
        WHERE accountId = :accountId
        AND feedId = :feedId
        AND isUnread = :isUnread
        AND date < :before
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN date END ASC,
            CASE WHEN :sortAscending = 0 THEN date END DESC
        """
    )
    fun queryMetadataByFeedId(
        accountId: Int,
        feedId: String,
        isUnread: Boolean,
        before: Date,
        sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT a.id, a.isUnread, a.isStarred 
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.accountId = :accountId
        AND a.isUnread = :isUnread
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN a.date END ASC,
            CASE WHEN :sortAscending = 0 THEN a.date END DESC
        """
    )
    fun queryMetadataByGroupIdWhenIsUnread(
        accountId: Int, groupId: String, isUnread: Boolean, sortAscending: Boolean = false
    ): List<ArticleMeta>

    @Transaction
    @Query(
        """
        SELECT a.id, a.isUnread, a.isStarred 
        FROM article AS a
        LEFT JOIN feed AS b ON b.id = a.feedId
        LEFT JOIN `group` AS c ON c.id = b.groupId
        WHERE c.id = :groupId
        AND a.accountId = :accountId
        AND a.isUnread = :isUnread
        AND a.date < :before
        ORDER BY
            CASE WHEN :sortAscending = 1 THEN a.date END ASC,
            CASE WHEN :sortAscending = 0 THEN a.date END DESC
        """
    )
    fun queryMetadataByGroupIdWhenIsUnread(
        accountId: Int,
        groupId: String,
        isUnread: Boolean,
        before: Date,
        sortAscending: Boolean = false
    ): List<ArticleMeta>


    /**
     * query the latest unread articles from account with id, limit count
     */
    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId
        AND isUnread = 1
        ORDER BY date desc
        LIMIT :limit
        """
    )
    fun queryLatestUnreadArticles(accountId: Int, limit: Int = 15): Flow<List<ArticleWithFeed>>


    /**
     * query the latest unread articles from feed with id, limit count
     */
    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE feedId = :feedId
        AND isUnread = 1
        ORDER BY date desc
        LIMIT :limit
        """
    )
    fun queryLatestUnreadArticlesFromFeed(feedId: String, limit: Int = 15): Flow<List<ArticleWithFeed>>


    /**
     * query the latest unread articles from group with id, limit count
     */
    @Transaction
    @Query(
        """
        SELECT a.* FROM article AS a
        LEFT JOIN feed AS f ON a.feedId = f.id
        WHERE f.groupId = :groupId
        AND a.isUnread = 1
        ORDER BY a.date DESC
        LIMIT :limit
        """
    )
    fun queryLatestUnreadArticlesFromGroup(groupId: String, limit: Int = 15): Flow<List<ArticleWithFeed>>


    /**
     * query the latest unread articles from account with id, limit count
     */
    @Transaction
    @Query(
        """
        SELECT * FROM article
        WHERE accountId = :accountId
        AND isUnread = 1
        ORDER BY date desc
        LIMIT :limit
        """
    )
    fun queryLatestUnreadArticleFlow(accountId: Int, limit: Int): Flow<List<ArticleWithFeed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg article: Article)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOnConflictIgnore(vararg article: Article)

    @Insert
    suspend fun insertList(articles: List<Article>)

    @Delete
    suspend fun delete(vararg article: Article)

    @Update
    suspend fun update(vararg article: Article)

    @Transaction
    suspend fun insertListIfNotExist(articles: List<Article>, feed: Feed): List<Article> {
        if (articles.isEmpty()) return articles

        val existingArticles = queryArticlesByLinks(
            linkList = articles.map { it.link },
            feedId = feed.id,
            accountId = feed.accountId
        ).associateBy { it.link }

        return articles.filterNot { existingArticles.containsKey(it.link) }.also { insertList(it) }
    }
}