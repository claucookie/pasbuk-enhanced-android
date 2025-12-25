package labs.claucookie.pasbuk.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.data.local.entity.PassEntity

@Dao
interface PassDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pass: PassEntity)

    @Update
    suspend fun update(pass: PassEntity)

    @Delete
    suspend fun delete(pass: PassEntity)

    @Query("SELECT * FROM passes WHERE id = :id")
    suspend fun getById(id: String): PassEntity?

    @Query("SELECT * FROM passes WHERE serialNumber = :serialNumber")
    suspend fun getBySerialNumber(serialNumber: String): PassEntity?

    @Query("SELECT * FROM passes ORDER BY CASE WHEN relevantDate IS NULL THEN 1 ELSE 0 END, relevantDate DESC")
    fun getAllSortedByDate(): Flow<List<PassEntity>>

    @Query("SELECT * FROM passes ORDER BY CASE WHEN relevantDate IS NULL THEN 1 ELSE 0 END, relevantDate DESC")
    fun getAllSortedByDatePaged(): PagingSource<Int, PassEntity>

    @Query("SELECT * FROM passes")
    fun getAll(): Flow<List<PassEntity>>

    @Query("DELETE FROM passes")
    suspend fun deleteAll()
}
