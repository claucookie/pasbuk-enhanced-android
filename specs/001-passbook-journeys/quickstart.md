# Quickstart Guide: Passbook Timeline and Journeys

**Feature**: 001-passbook-journeys
**Branch**: `001-passbook-journeys`
**Audience**: Developers implementing this feature

## Overview

This guide walks you through implementing the Passbook Timeline and Journeys feature from scratch. Follow these steps in order for a smooth development experience.

---

## Prerequisites

- [ ] Android Studio (latest stable version)
- [ ] JDK 11 or higher
- [ ] Android SDK with API 28+ support
- [ ] Git configured with access to repository
- [ ] Familiarity with Kotlin, Jetpack Compose, Room, and Coroutines

---

## Phase 1: Project Setup (1-2 hours)

### Step 1: Update Dependencies

Add required dependencies to `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"  // Update to match Kotlin version
    id("com.google.dagger.hilt.android") version "2.57.1"
}

dependencies {
    // Existing dependencies...

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Barcode rendering
    implementation("com.google.zxing:core:3.5.2")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation("io.mockk:mockk-android:1.14.5")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
}
```

Update `gradle/libs.versions.toml` if needed, then sync Gradle.

### Step 2: Enable Hilt

In `app/src/main/java/labs/claucookie/pasbuk/PasbukApplication.kt`:

```kotlin
package labs.claucookie.pasbuk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PasbukApplication : Application()
```

Update `AndroidManifest.xml`:

```xml
<application
    android:name=".PasbukApplication"
    ...>
```

Update `MainActivity.kt`:

```kotlin
package labs.claucookie.pasbuk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import labs.claucookie.pasbuk.ui.theme.PasbukTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasbukTheme {
                // Navigation will go here
            }
        }
    }
}
```

---

## Phase 2: Domain Layer (3-4 hours)

### Step 3: Create Domain Models

Create `app/src/main/java/labs/claucookie/pasbuk/domain/model/`:

**Pass.kt**:
```kotlin
package labs.claucookie.pasbuk.domain.model

import java.time.Instant

data class Pass(
    val id: String,
    val serialNumber: String,
    val passTypeIdentifier: String,
    val organizationName: String,
    val description: String,
    val teamIdentifier: String,
    val relevantDate: Instant?,
    val expirationDate: Instant?,
    val barcode: Barcode?,
    val logoText: String?,
    val backgroundColor: String?,
    val foregroundColor: String?,
    val logoImagePath: String?,
    val iconImagePath: String?,
    val originalPkpassPath: String,
    val passType: PassType,
    val createdAt: Instant,
    val modifiedAt: Instant
)

enum class PassType {
    BOARDING_PASS, EVENT_TICKET, COUPON, STORE_CARD, GENERIC
}

data class Barcode(
    val message: String,
    val format: BarcodeFormat,
    val messageEncoding: String = "iso-8859-1"
)

enum class BarcodeFormat {
    QR, PDF417, AZTEC, CODE128
}
```

**Journey.kt**:
```kotlin
package labs.claucookie.pasbuk.domain.model

import java.time.Instant

data class Journey(
    val id: Long,
    val name: String,
    val passes: List<Pass>,
    val createdAt: Instant,
    val modifiedAt: Instant
)
```

### Step 4: Create Repository Interfaces

Create `app/src/main/java/labs/claucookie/pasbuk/domain/repository/`:

**PassRepository.kt**: See `contracts/repository-interfaces.md`

**JourneyRepository.kt**: See `contracts/repository-interfaces.md`

### Step 5: Create Use Cases

Create `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/`:

Implement use cases from `contracts/use-case-interfaces.md`:
- `ImportPassUseCase.kt`
- `GetTimelineUseCase.kt`
- `CreateJourneyUseCase.kt`
- `GetAllJourneysUseCase.kt`
- `GetPassDetailUseCase.kt`
- `GetJourneyDetailUseCase.kt`

---

## Phase 3: Data Layer (4-6 hours)

### Step 6: Create Room Entities

Create `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/`:

**PassEntity.kt**, **JourneyEntity.kt**, **JourneyPassCrossRef.kt**: See `data-model.md`

### Step 7: Create DAOs

Create `app/src/main/java/labs/claucookie/pasbuk/data/local/dao/`:

**PassDao.kt**:
```kotlin
package labs.claucookie.pasbuk.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.data.local.entity.PassEntity

@Dao
interface PassDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pass: PassEntity)

    @Query("SELECT * FROM passes WHERE id = :passId")
    suspend fun getById(passId: String): PassEntity?

    @Query("SELECT * FROM passes ORDER BY relevantDate DESC")
    fun getAllSortedByDate(): Flow<List<PassEntity>>

    @Query("SELECT * FROM passes WHERE id IN (:passIds)")
    suspend fun getByIds(passIds: List<String>): List<PassEntity>

    @Delete
    suspend fun delete(pass: PassEntity)

    @Query("DELETE FROM passes WHERE id = :passId")
    suspend fun deleteById(passId: String)
}
```

**JourneyDao.kt**:
```kotlin
package labs.claucookie.pasbuk.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.data.local.entity.*

@Dao
interface JourneyDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(journey: JourneyEntity): Long

    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    suspend fun getById(journeyId: Long): JourneyEntity?

    @Query("SELECT * FROM journeys ORDER BY createdAt DESC")
    fun getAll(): Flow<List<JourneyEntity>>

    @Transaction
    @Query("SELECT * FROM journeys WHERE id = :journeyId")
    suspend fun getJourneyWithPasses(journeyId: Long): JourneyWithPasses?

    @Transaction
    @Query("SELECT * FROM journeys ORDER BY createdAt DESC")
    fun getAllJourneysWithPasses(): Flow<List<JourneyWithPasses>>

    @Update
    suspend fun update(journey: JourneyEntity)

    @Delete
    suspend fun delete(journey: JourneyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertJourneyPassCrossRef(crossRef: JourneyPassCrossRef)

    @Delete
    suspend fun deleteJourneyPassCrossRef(crossRef: JourneyPassCrossRef)

    @Query("DELETE FROM journey_pass_cross_ref WHERE journeyId = :journeyId AND passId IN (:passIds)")
    suspend fun removePassesFromJourney(journeyId: Long, passIds: List<String>)
}
```

### Step 8: Create Database

Create `app/src/main/java/labs/claucookie/pasbuk/data/local/AppDatabase.kt`:

```kotlin
package labs.claucookie.pasbuk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import labs.claucookie.pasbuk.data.local.dao.JourneyDao
import labs.claucookie.pasbuk.data.local.dao.PassDao
import labs.claucookie.pasbuk.data.local.entity.*

@Database(
    entities = [
        PassEntity::class,
        JourneyEntity::class,
        JourneyPassCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(PassTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passDao(): PassDao
    abstract fun journeyDao(): JourneyDao
}
```

### Step 9: Create Parser

Create `app/src/main/java/labs/claucookie/pasbuk/data/parser/PkpassParser.kt`:

```kotlin
package labs.claucookie.pasbuk.data.parser

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import labs.claucookie.pasbuk.domain.model.Pass
import java.util.zip.ZipInputStream

class PkpassParser(
    private val context: Context,
    private val moshi: Moshi
) {
    suspend fun parse(uri: Uri): Pass {
        // 1. Open ZIP stream from URI
        // 2. Extract pass.json
        // 3. Parse JSON to Pass model
        // 4. Extract images to internal storage
        // 5. Save original .pkpass file
        // Implementation in tasks phase
        TODO("Implement in Phase 4")
    }
}
```

### Step 10: Implement Repositories

Create `app/src/main/java/labs/claucookie/pasbuk/data/repository/`:

**PassRepositoryImpl.kt**: Implement `PassRepository` interface using `PassDao` and `PkpassParser`

**JourneyRepositoryImpl.kt**: Implement `JourneyRepository` interface using `JourneyDao`

---

## Phase 4: Dependency Injection (1-2 hours)

### Step 11: Create Hilt Modules

Create `app/src/main/java/labs/claucookie/pasbuk/di/`:

**DatabaseModule.kt**:
```kotlin
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
            "pasbuk_database"
        ).build()
    }

    @Provides
    fun providePassDao(database: AppDatabase): PassDao = database.passDao()

    @Provides
    fun provideJourneyDao(database: AppDatabase): JourneyDao = database.journeyDao()
}
```

**RepositoryModule.kt**, **UseCaseModule.kt**: Provide repository and use case instances.

---

## Phase 5: UI Layer (6-8 hours)

### Step 12: Create Navigation

Create `app/src/main/java/labs/claucookie/pasbuk/ui/navigation/Navigation.kt`:

```kotlin
package labs.claucookie.pasbuk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Timeline : Screen("timeline")
    object PassDetail : Screen("pass/{passId}") {
        fun createRoute(passId: String) = "pass/$passId"
    }
    object JourneyList : Screen("journeys")
    object JourneyDetail : Screen("journey/{journeyId}") {
        fun createRoute(journeyId: Long) = "journey/$journeyId"
    }
}

@Composable
fun PasbukNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Timeline.route) {
        composable(Screen.Timeline.route) { /* TimelineScreen */ }
        composable(Screen.PassDetail.route) { /* PassDetailScreen */ }
        composable(Screen.JourneyList.route) { /* JourneyListScreen */ }
        composable(Screen.JourneyDetail.route) { /* JourneyDetailScreen */ }
    }
}
```

### Step 13: Create ViewModels and Screens

For each screen:
1. Create ViewModel in `ui/screens/{screen}/`
2. Create Screen composable
3. Create UI state data class
4. Wire up use cases via Hilt injection

**Example**: `TimelineViewModel.kt`, `TimelineScreen.kt`

### Step 14: Create Reusable Components

Create `app/src/main/java/labs/claucookie/pasbuk/ui/components/`:
- `PassCard.kt`: Display pass summary in list
- `JourneyCard.kt`: Display journey summary
- `BarcodeDisplay.kt`: Render barcode using ZXing

---

## Phase 6: Testing (4-6 hours)

### Step 15: Write Unit Tests

**Use Case Tests**: Test business logic validation
**Repository Tests**: Test with in-memory Room database

### Step 16: Write Integration Tests

Test Room DAO operations with `@RunWith(AndroidJUnit4::class)`

### Step 17: Write UI Tests

Test critical flows with Compose Testing APIs:
- Import pass flow
- Create journey flow
- View timeline

---

## Development Tips

### Debugging
- Use Android Studio's Database Inspector to view Room data
- Enable SQL logging: `setQueryCallback()` on RoomDatabase.Builder
- Use Logcat filters: `tag:Pasbuk`

### Testing .pkpass Files
- Download sample .pkpass files from Apple's Passbook documentation
- Create test fixtures in `androidTest/assets/`
- Use MockK to simulate file picker in unit tests

### Performance
- Profile timeline scrolling with Android Profiler
- Use `LazyColumn` for timeline (not `Column`)
- Load images with Coil's placeholder/error handling

### Common Issues
- **Hilt compilation errors**: Clean build, invalidate caches
- **Room schema export errors**: Enable `exportSchema = true` and add schema directory
- **File not found**: Check `context.filesDir` permissions

---

## Verification Checklist

Before submitting PR:

- [ ] All unit tests pass (`./gradlew test`)
- [ ] All instrumented tests pass (`./gradlew connectedAndroidTest`)
- [ ] Lint checks pass (`./gradlew lint`)
- [ ] Can import a .pkpass file
- [ ] Timeline displays passes sorted by date
- [ ] Can create a journey with multiple passes
- [ ] Journey detail shows passes chronologically
- [ ] TalkBack accessibility tested
- [ ] No memory leaks (Profiler check)
- [ ] App meets performance targets (SC-001, SC-002)

---

## Next Steps

1. **Start with Phase 1**: Set up dependencies and Hilt
2. **Build incrementally**: Domain → Data → UI
3. **Test continuously**: Don't wait until the end
4. **Refer to specs**: `spec.md`, `data-model.md`, `contracts/`

For detailed task breakdown, see `tasks.md` (generated by `/speckit.tasks`).

---

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Apple Passbook Format](https://developer.apple.com/library/archive/documentation/UserExperience/Conceptual/PassKit_PG/)
- [Project Constitution](../../.specify/memory/constitution.md)
