package me.ash.reader.domain.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.FeedWithKeywordFilter
import me.ash.reader.domain.model.feed.KeywordFilter

@Dao
interface KeywordFilterDao {

    @Query(
        """
           SELECT * FROM `keyword_filter`
           """
    )
    fun queryAll(): Flow<MutableList<KeywordFilter>>

    @Query(
        """
           SELECT * FROM `keyword_filter`
           """
    )
    suspend fun queryAllBlocking(): List<KeywordFilter>

    @Query(
        """
           SELECT * FROM `keyword_filter`
           WHERE feedId = :feedId
           """
    )
    suspend fun queryAllWithFeedId(feedId: String): List<KeywordFilter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg filter: KeywordFilter)

    @Delete
    suspend fun delete(vararg filter: KeywordFilter)

    @Insert
    suspend fun insertList(filters: List<KeywordFilter>)
}