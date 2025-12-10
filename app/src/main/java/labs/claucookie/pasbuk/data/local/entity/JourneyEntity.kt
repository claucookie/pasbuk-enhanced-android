package labs.claucookie.pasbuk.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journeys",
    indices = [Index(value = ["name"], unique = true)]
)
data class JourneyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long
)
