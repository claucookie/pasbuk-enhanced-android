package labs.claucookie.pasbuk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import labs.claucookie.pasbuk.data.local.dao.JourneyDao
import labs.claucookie.pasbuk.data.local.dao.PassDao
import labs.claucookie.pasbuk.data.local.entity.JourneyEntity
import labs.claucookie.pasbuk.data.local.entity.JourneyPassCrossRef
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.data.local.entity.PassTypeConverter

@Database(
    entities = [
        PassEntity::class,
        JourneyEntity::class,
        JourneyPassCrossRef::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(PassTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passDao(): PassDao
    abstract fun journeyDao(): JourneyDao

    companion object {
        const val DATABASE_NAME = "pasbuk_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE journeys ADD COLUMN suggestionsJson TEXT DEFAULT NULL"
                )
            }
        }
    }
}
