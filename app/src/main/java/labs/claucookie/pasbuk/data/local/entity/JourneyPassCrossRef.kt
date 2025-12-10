package labs.claucookie.pasbuk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "journey_pass_cross_ref",
    primaryKeys = ["journeyId", "passId"],
    foreignKeys = [
        ForeignKey(
            entity = JourneyEntity::class,
            parentColumns = ["id"],
            childColumns = ["journeyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PassEntity::class,
            parentColumns = ["id"],
            childColumns = ["passId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["journeyId"]),
        Index(value = ["passId"])
    ]
)
data class JourneyPassCrossRef(
    val journeyId: Long,
    val passId: String,
    val sortOrder: Int = 0
)
