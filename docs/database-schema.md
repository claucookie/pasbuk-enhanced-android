# Room Database Schema Documentation

## Overview

The Pasbuk Enhanced app uses Room Persistence Library for local data storage. This document describes the database schema, export configuration, and migration strategy.

## Database Configuration

**Database Name**: `pasbuk_database`
**Current Version**: 1
**Location**: `app/src/main/java/labs/claucookie/pasbuk/data/local/AppDatabase.kt`

## Entities

### 1. PassEntity
**Table Name**: `passes`

Stores imported .pkpass file data with parsed metadata.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | TEXT | PRIMARY KEY | Unique identifier (UUID) |
| serialNumber | TEXT | NOT NULL, UNIQUE | Pass serial number from pass.json |
| passTypeIdentifier | TEXT | NOT NULL | Pass type identifier |
| organizationName | TEXT | NOT NULL | Organization name |
| description | TEXT | NOT NULL | Pass description |
| teamIdentifier | TEXT | NOT NULL | Team identifier |
| relevantDate | INTEGER | NULLABLE | Relevant date (Unix timestamp) |
| expirationDate | INTEGER | NULLABLE | Expiration date (Unix timestamp) |
| barcode | TEXT | NULLABLE | Barcode JSON (BarcodeFormat, message, encoding) |
| logoText | TEXT | NULLABLE | Logo text |
| backgroundColor | TEXT | NULLABLE | Background color (hex) |
| foregroundColor | TEXT | NULLABLE | Foreground color (hex) |
| logoImagePath | TEXT | NULLABLE | Path to logo image in internal storage |
| iconImagePath | TEXT | NULLABLE | Path to icon image in internal storage |
| originalPkpassPath | TEXT | NOT NULL | Path to original .pkpass file |
| passType | TEXT | NOT NULL | PassType enum (BOARDING_PASS, EVENT_TICKET, etc.) |
| createdAt | INTEGER | NOT NULL | Creation timestamp |
| modifiedAt | INTEGER | NOT NULL | Last modification timestamp |

**Indices**:
- `index_passes_relevantDate` on `relevantDate` (for timeline sorting)
- `index_passes_serialNumber` on `serialNumber` (for duplicate detection)

### 2. JourneyEntity
**Table Name**: `journeys`

Stores user-created journey collections.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Journey ID |
| name | TEXT | NOT NULL, UNIQUE | Journey name |
| createdAt | INTEGER | NOT NULL | Creation timestamp |
| modifiedAt | INTEGER | NOT NULL | Last modification timestamp |

**Indices**:
- `index_journeys_name` on `name` (for duplicate name detection)

### 3. JourneyPassCrossRef
**Table Name**: `journey_pass_cross_ref`

Many-to-many relationship between journeys and passes.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| journeyId | INTEGER | PRIMARY KEY, FOREIGN KEY | References journeys(id) ON DELETE CASCADE |
| passId | TEXT | PRIMARY KEY, FOREIGN KEY | References passes(id) ON DELETE CASCADE |

**Composite Primary Key**: `(journeyId, passId)`

**Foreign Keys**:
- `journeyId` → `journeys(id)` with CASCADE delete
- `passId` → `passes(id)` with CASCADE delete

**Indices**:
- `index_journey_pass_cross_ref_journeyId` on `journeyId`
- `index_journey_pass_cross_ref_passId` on `passId`

## Type Converters

Location: `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/PassTypeConverter.kt`

Converts complex types to/from JSON for database storage:
- `Instant` ↔ `Long` (Unix timestamp in milliseconds)
- `Barcode` ↔ `String` (JSON)
- `PassType` ↔ `String` (enum name)
- `BarcodeFormat` ↔ `String` (enum name)

## Schema Export Configuration

### Gradle Configuration

In `app/build.gradle.kts`:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

### Export Location

Schema files are exported to: `app/schemas/`

**Current Schema Version**: `app/schemas/labs.claucookie.pasbuk.data.local.AppDatabase/1.json`

### Why Export Schemas?

1. **Version Control**: Track database structure changes over time
2. **Migration Testing**: Room can auto-generate migration tests using exported schemas
3. **Documentation**: JSON schema serves as machine-readable documentation
4. **CI/CD**: Automated tests can verify schema compatibility

### Viewing the Schema

The exported schema is a JSON file describing:
- Database version
- All entities (tables)
- All columns with types and constraints
- All indices
- All foreign keys

Example:
```bash
cat app/schemas/labs.claucookie.pasbuk.data.local.AppDatabase/1.json
```

## Database Migrations

### Current Version: 1

This is the initial schema version. No migrations exist yet.

### Migration Strategy

When the schema changes (e.g., adding a new column):

1. **Increment version** in `AppDatabase.kt`:
   ```kotlin
   @Database(version = 2)
   ```

2. **Export new schema**: Build the project to generate `2.json`

3. **Create migration**:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL("ALTER TABLE passes ADD COLUMN newField TEXT")
       }
   }
   ```

4. **Add migration** to `DatabaseModule.kt`:
   ```kotlin
   Room.databaseBuilder(context, AppDatabase::class.java, "pasbuk_database")
       .addMigrations(MIGRATION_1_2)
       .build()
   ```

5. **Test migration**: Use `MigrationTestHelper` in instrumented tests

### Migration Testing

Example test (to be added when migrations are needed):

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2() {
        // Create database with version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data
            close()
        }

        // Run migration and validate
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    }
}
```

## Accessing the Database

### Via Database Inspector (Android Studio)

1. Run the app on an emulator or rooted device
2. Open **View > Tool Windows > App Inspection**
3. Select **Database Inspector** tab
4. Explore tables, run queries, and view data in real-time

### Via ADB

```bash
# Pull database file from device
adb shell run-as labs.claucookie.pasbuk cat /data/data/labs.claucookie.pasbuk/databases/pasbuk_database > pasbuk_database

# Open with SQLite browser
sqlite3 pasbuk_database
```

### Debugging Queries

Enable SQL logging in debug builds by adding to `AppDatabase`:

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "pasbuk_database")
    .setQueryCallback({ sqlQuery, bindArgs ->
        Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
    }, Executors.newSingleThreadExecutor())
    .build()
```

## Performance Considerations

### Indices

All frequently queried columns have indices:
- `passes.relevantDate` - Used for timeline sorting
- `passes.serialNumber` - Used for duplicate detection
- `journeys.name` - Used for duplicate name validation
- `journey_pass_cross_ref` composite key - Optimizes join queries

### Query Optimization

- Use `Flow<List<T>>` for observing data changes (reactive)
- Avoid `LiveData` in favor of `Flow` for better Kotlin coroutine integration
- Use `@Transaction` for complex queries with relations (e.g., `JourneyWithPasses`)
- Leverage Room's compile-time SQL verification

### Pagination

For large datasets (100+ passes), use Paging 3:
- `PagingSource` already implemented in `PassDao`
- Displays timeline efficiently with `LazyColumn`

## Backup and Restore

To implement backup/restore functionality:

1. **Export database**:
   ```kotlin
   context.getDatabasePath("pasbuk_database").copyTo(backupFile)
   ```

2. **Import database**:
   ```kotlin
   backupFile.copyTo(context.getDatabasePath("pasbuk_database"))
   ```

3. **Validate schema version** before import to prevent corruption

## References

- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Room Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Database Inspector](https://developer.android.com/studio/inspect/database)
- [Testing Room Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions#test)

---

**Last Updated**: 2025-12-25
**Database Version**: 1
**Schema Location**: `app/schemas/labs.claucookie.pasbuk.data.local.AppDatabase/`
