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
        val localGroupIds = queryByIds(groups.map { it.id }).map { it.id }
        val (newGroups, groupsToUpdate) = groups.partition { it.id !in localGroupIds }
        insertAll(newGroups)
        updateAll(groupsToUpdate)
    }
}
