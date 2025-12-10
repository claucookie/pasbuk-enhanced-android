package labs.claucookie.pasbuk.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class JourneyWithPasses(
    @Embedded val journey: JourneyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = JourneyPassCrossRef::class,
            parentColumn = "journeyId",
            entityColumn = "passId"
        )
    )
    val passes: List<PassEntity>
)
