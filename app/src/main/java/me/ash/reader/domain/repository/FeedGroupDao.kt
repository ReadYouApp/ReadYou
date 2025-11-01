package me.ash.reader.domain.repository

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feedgroup.FeedGroup
import me.ash.reader.domain.model.group.Group

@Dao
interface FeedGroupDao {

    /**
     * Insert a feed-group association.
     * If the association already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg feedGroup: FeedGroup)

    /**
     * Insert multiple feed-group associations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(feedGroups: List<FeedGroup>)

    /**
     * Delete a specific feed-group association.
     */
    @Delete
    suspend fun delete(vararg feedGroup: FeedGroup)

    /**
     * Delete a specific feed-group association by IDs.
     */
    @Query(
        """
        DELETE FROM feed_group
        WHERE feedId = :feedId
        AND groupId = :groupId
        AND accountId = :accountId
        """
    )
    suspend fun deleteByIds(feedId: String, groupId: String, accountId: Int)

    /**
     * Delete all associations for a specific feed.
     * Useful when removing a feed from all groups.
     */
    @Query(
        """
        DELETE FROM feed_group
        WHERE feedId = :feedId
        AND accountId = :accountId
        """
    )
    suspend fun deleteByFeedId(feedId: String, accountId: Int)

    /**
     * Delete all associations for a specific group.
     * Called when a group is deleted to clean up associations.
     */
    @Query(
        """
        DELETE FROM feed_group
        WHERE groupId = :groupId
        AND accountId = :accountId
        """
    )
    suspend fun deleteByGroupId(groupId: String, accountId: Int)

    /**
     * Delete all associations for an account.
     * Called when an account is deleted.
     */
    @Query(
        """
        DELETE FROM feed_group
        WHERE accountId = :accountId
        """
    )
    suspend fun deleteByAccountId(accountId: Int)

    /**
     * Get all group IDs associated with a specific feed.
     */
    @Query(
        """
        SELECT groupId FROM feed_group
        WHERE feedId = :feedId
        AND accountId = :accountId
        """
    )
    suspend fun getGroupIdsByFeed(feedId: String, accountId: Int): List<String>

    /**
     * Get all feed IDs associated with a specific group.
     */
    @Query(
        """
        SELECT feedId FROM feed_group
        WHERE groupId = :groupId
        AND accountId = :accountId
        """
    )
    suspend fun getFeedIdsByGroup(groupId: String, accountId: Int): List<String>

    /**
     * Get all groups associated with a specific feed.
     */
    @Query(
        """
        SELECT g.* FROM `group` g
        INNER JOIN feed_group fg ON g.id = fg.groupId
        WHERE fg.feedId = :feedId
        AND fg.accountId = :accountId
        """
    )
    suspend fun getGroupsByFeed(feedId: String, accountId: Int): List<Group>

    /**
     * Get all feeds associated with a specific group.
     * This is the many-to-many equivalent of FeedDao.queryByGroupId
     */
    @Query(
        """
        SELECT f.* FROM feed f
        INNER JOIN feed_group fg ON f.id = fg.feedId
        WHERE fg.groupId = :groupId
        AND fg.accountId = :accountId
        """
    )
    suspend fun getFeedsByGroup(groupId: String, accountId: Int): List<Feed>

    /**
     * Get all feeds associated with a specific group as a Flow.
     * Useful for reactive UI updates.
     */
    @Query(
        """
        SELECT f.* FROM feed f
        INNER JOIN feed_group fg ON f.id = fg.feedId
        WHERE fg.groupId = :groupId
        AND fg.accountId = :accountId
        """
    )
    fun getFeedsByGroupAsFlow(groupId: String, accountId: Int): Flow<List<Feed>>

    /**
     * Get all feed-group associations for an account.
     */
    @Query(
        """
        SELECT * FROM feed_group
        WHERE accountId = :accountId
        """
    )
    suspend fun getAllByAccount(accountId: Int): List<FeedGroup>

    /**
     * Check if a feed-group association exists.
     */
    @Query(
        """
        SELECT COUNT(*) FROM feed_group
        WHERE feedId = :feedId
        AND groupId = :groupId
        AND accountId = :accountId
        """
    )
    suspend fun exists(feedId: String, groupId: String, accountId: Int): Int

    /**
     * Get the count of feeds in a specific group.
     */
    @Query(
        """
        SELECT COUNT(*) FROM feed_group
        WHERE groupId = :groupId
        AND accountId = :accountId
        """
    )
    suspend fun getFeedCountByGroup(groupId: String, accountId: Int): Int

    /**
     * Get the count of groups a feed belongs to.
     */
    @Query(
        """
        SELECT COUNT(*) FROM feed_group
        WHERE feedId = :feedId
        AND accountId = :accountId
        """
    )
    suspend fun getGroupCountByFeed(feedId: String, accountId: Int): Int

    /**
     * Move a feed from one group to another.
     * This updates the association, maintaining many-to-many semantics.
     * If you want to add to a new group while keeping existing associations,
     * use insert() instead.
     */
    @Transaction
    suspend fun moveFeed(
        feedId: String,
        fromGroupId: String,
        toGroupId: String,
        accountId: Int
    ) {
        deleteByIds(feedId, fromGroupId, accountId)
        insert(FeedGroup(feedId = feedId, groupId = toGroupId, accountId = accountId))
    }

    /**
     * Replace all groups for a feed with a new set of groups.
     * Useful for bulk updates during sync operations.
     */
    @Transaction
    suspend fun replaceGroupsForFeed(feedId: String, groupIds: List<String>, accountId: Int) {
        deleteByFeedId(feedId, accountId)
        insertList(groupIds.map { groupId ->
            FeedGroup(feedId = feedId, groupId = groupId, accountId = accountId)
        })
    }

    /**
     * Replace all feeds for a group with a new set of feeds.
     * Useful for bulk updates during sync operations.
     */
    @Transaction
    suspend fun replaceFeedsForGroup(groupId: String, feedIds: List<String>, accountId: Int) {
        deleteByGroupId(groupId, accountId)
        insertList(feedIds.map { feedId ->
            FeedGroup(feedId = feedId, groupId = groupId, accountId = accountId)
        })
    }
}
