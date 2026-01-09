package labs.claucookie.pasbuk.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import labs.claucookie.pasbuk.data.local.AppDatabase
import labs.claucookie.pasbuk.data.local.dao.JourneyDao
import labs.claucookie.pasbuk.data.local.dao.PassDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun providePassDao(database: AppDatabase): PassDao {
        return database.passDao()
    }

    @Provides
    @Singleton
    fun provideJourneyDao(database: AppDatabase): JourneyDao {
        return database.journeyDao()
    }
}
