package labs.claucookie.pasbuk.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.data.local.entity.JourneyEntity
import labs.claucookie.pasbuk.data.local.entity.JourneyPassCrossRef
import labs.claucookie.pasbuk.data.local.entity.JourneyWithPasses

@Dao
interface JourneyDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(journey: JourneyEntity): Long

    @Update
    suspend fun update(journey: JourneyEntity)

    @Delete
    suspend fun delete(journey: JourneyEntity)

    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getById(id: Long): JourneyEntity?

    @Query("SELECT * FROM journeys WHERE name = :name")
    suspend fun getByName(name: String): JourneyEntity?

    @Query("SELECT * FROM journeys ORDER BY createdAt DESC")
    fun getAll(): Flow<List<JourneyEntity>>

    @Transaction
    @Query("SELECT * FROM journeys WHERE id = :id")
    suspend fun getJourneyWithPasses(id: Long): JourneyWithPasses?

    @Transaction
    @Query("SELECT * FROM journeys ORDER BY createdAt DESC")
    fun getAllJourneysWithPasses(): Flow<List<JourneyWithPasses>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneyPassCrossRef(crossRef: JourneyPassCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneyPassCrossRefs(crossRefs: List<JourneyPassCrossRef>)

    @Delete
    suspend fun deleteJourneyPassCrossRef(crossRef: JourneyPassCrossRef)

    @Query("DELETE FROM journey_pass_cross_ref WHERE journeyId = :journeyId")
    suspend fun deleteAllPassesFromJourney(journeyId: Long)

    @Query("DELETE FROM journeys")
    suspend fun deleteAll()
}
