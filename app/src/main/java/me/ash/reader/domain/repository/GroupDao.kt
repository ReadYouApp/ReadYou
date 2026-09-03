package me.ash.reader.domain.repository

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.model.group.GroupWithFeed

@Dao
interface GroupDao {

    @Query(
        """
        SELECT * FROM `group`
        WHERE id = :id
        """
    )
    suspend fun queryById(id: String): Group?

    @Query(
        """
        SELECT * FROM `group`
        WHERE id in (:idList)
        """
    )
    suspend fun queryByIds(idList: List<String>): List<Group>

    @Transaction
    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroupWithFeedAsFlow(accountId: Int): Flow<MutableList<GroupWithFeed>>

    @Transaction
    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    suspend fun queryAllGroupWithFeed(accountId: Int): List<GroupWithFeed>

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    fun queryAllGroup(accountId: Int): Flow<MutableList<Group>>

    @Query(
        """
        DELETE FROM `group`
        WHERE accountId = :accountId
        """
    )
    suspend fun deleteByAccountId(accountId: Int)

    @Query(
        """
        SELECT * FROM `group`
        WHERE accountId = :accountId
        """
    )
    suspend fun queryAll(accountId: Int): List<Group>

    @Insert
    suspend fun insertAll(groups: List<Group>)

    @Update
    suspend fun updateAll(groups: List<Group>)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg group: Group)

    @Update suspend fun update(vararg group: Group)

    @Delete suspend fun delete(vararg group: Group)

    @Transaction
    suspend fun insertOrUpdate(groups: List<Group>) {
        val localGroups = queryByIds(groups.map { it.id }).associateBy { it.id }
        val (newGroups, groupsToUpdate) = groups.partition { !localGroups.contains(it.id) }

        if (newGroups.isNotEmpty()) {
            val accountId = newGroups.first().accountId
            val basePosition = queryAll(accountId).maxOfOrNull { it.position } ?: -1
            var cursor = basePosition
            insertAll(
                newGroups.map { group ->
                    if (group.position == 0) {
                        cursor += 1
                        group.copy(position = cursor)
                    } else {
                        group
                    }
                }
            )
        }

        updateAll(
            groupsToUpdate.mapNotNull { new ->
                val existing = localGroups[new.id] ?: return@mapNotNull null
                if (existing == new) null else new.copy(position = existing.position)
            }
        )
    }
}
