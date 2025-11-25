# Data Model: Passbook Timeline and Journeys

**Feature**: 001-passbook-journeys
**Date**: 2025-11-21
**Based on**: spec.md, research.md

## Overview

This document defines the data entities, relationships, and persistence strategy for the Passbook Timeline and Journeys feature. The model follows clean architecture with separate domain and data layer representations.

---

## Domain Layer Entities

These are the business logic entities used throughout the application.

### Pass

Represents a single imported .pkpass file with all its metadata.

```kotlin
package labs.claucookie.pasbuk.domain.model

import java.time.Instant

data class Pass(
    val id: String,                    // UUID or serial number
    val serialNumber: String,          // From pass.json
    val passTypeIdentifier: String,    // e.g., "pass.com.airline.boardingpass"
    val organizationName: String,      // Issuer name
    val description: String,           // Pass title/description
    val teamIdentifier: String,        // Apple Developer Team ID

    // Event/Date information
    val relevantDate: Instant?,        // Primary event date/time
    val expirationDate: Instant?,      // When pass expires

    // Location data (if present)
    val locations: List<Location>,     // Associated locations

    // Visual elements
    val logoText: String?,             // Text displayed near logo
    val backgroundColor: String?,      // RGB hex color
    val foregroundColor: String?,      // RGB hex color
    val labelColor: String?,           // RGB hex color

    // Barcode
    val barcode: Barcode?,             // Primary barcode (or null)

    // File paths (internal storage)
    val logoImagePath: String?,
    val iconImagePath: String?,
    val thumbnailImagePath: String?,
    val stripImagePath: String?,
    val backgroundImagePath: String?,
    val originalPkpassPath: String,    // Original .pkpass file

    // Pass type specific fields
    val passType: PassType,            // Event, Boarding, Store Card, etc.
    val fields: Map<String, PassField>, // All fields from pass.json sections

    // Metadata
    val createdAt: Instant,            // Import timestamp
    val modifiedAt: Instant            // Last update timestamp
)

enum class PassType {
    BOARDING_PASS,
    EVENT_TICKET,
    COUPON,
    STORE_CARD,
    GENERIC
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val relevantText: String?
)

data class Barcode(
    val message: String,               // Barcode data
    val format: BarcodeFormat,         // QR, PDF417, Aztec, Code128
    val messageEncoding: String,       // Usually "iso-8859-1"
    val altText: String?               // Alternative text
)

enum class BarcodeFormat {
    QR,
    PDF417,
    AZTEC,
    CODE128
}

data class PassField(
    val key: String,
    val label: String?,
    val value: String,                 // Can be string, number, or date
    val textAlignment: TextAlignment?
)

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
    NATURAL
}
```

### Journey

Represents a user-created collection of passes grouped by theme/trip.

```kotlin
package labs.claucookie.pasbuk.domain.model

import java.time.Instant

data class Journey(
    val id: Long,                      // Auto-generated ID
    val name: String,                  // User-provided name
    val passes: List<Pass>,            // Contained passes (sorted by date)
    val createdAt: Instant,            // Creation timestamp
    val modifiedAt: Instant            // Last update timestamp
) {
    // Computed properties
    val passCount: Int get() = passes.size
    val dateRange: ClosedRange<Instant>? get() {
        val dates = passes.mapNotNull { it.relevantDate }.sorted()
        return if (dates.isEmpty()) null else dates.first()..dates.last()
    }
}
```

---

## Data Layer Entities (Room)

These entities map to SQLite tables via Room.

### PassEntity

```kotlin
package labs.claucookie.pasbuk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "passes")
@TypeConverters(PassTypeConverter::class)
data class PassEntity(
    @PrimaryKey val id: String,
    val serialNumber: String,
    val passTypeIdentifier: String,
    val organizationName: String,
    val description: String,
    val teamIdentifier: String,

    // Dates stored as epoch milliseconds
    val relevantDate: Long?,
    val expirationDate: Long?,

    // JSON-serialized complex types
    val locationsJson: String?,        // List<Location> as JSON
    val barcodeJson: String?,          // Barcode as JSON
    val fieldsJson: String?,           // Map<String, PassField> as JSON

    // Visual elements
    val logoText: String?,
    val backgroundColor: String?,
    val foregroundColor: String?,
    val labelColor: String?,

    // File paths
    val logoImagePath: String?,
    val iconImagePath: String?,
    val thumbnailImagePath: String?,
    val stripImagePath: String?,
    val backgroundImagePath: String?,
    val originalPkpassPath: String,

    // Pass type
    val passType: String,              // Enum stored as string

    // Metadata
    val createdAt: Long,
    val modifiedAt: Long
)
```

### JourneyEntity

```kotlin
package labs.claucookie.pasbuk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journeys")
data class JourneyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long
)
```

### JourneyPassCrossRef

Many-to-many relationship between Journeys and Passes.

```kotlin
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
    val sortOrder: Int                 // For maintaining custom order within journey
)
```

### JourneyWithPasses (Relationship Query Result)

```kotlin
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
```

---

## Type Converters

Room requires type converters for complex types.

```kotlin
package labs.claucookie.pasbuk.data.local.entity

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.Location
import labs.claucookie.pasbuk.domain.model.PassField

class PassTypeConverter {
    private val moshi = Moshi.Builder().build()

    @TypeConverter
    fun fromLocationsJson(value: String?): List<Location>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, Location::class.java)
        val adapter = moshi.adapter<List<Location>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toLocationsJson(locations: List<Location>?): String? {
        if (locations == null) return null
        val type = Types.newParameterizedType(List::class.java, Location::class.java)
        val adapter = moshi.adapter<List<Location>>(type)
        return adapter.toJson(locations)
    }

    @TypeConverter
    fun fromBarcodeJson(value: String?): Barcode? {
        if (value == null) return null
        val adapter = moshi.adapter(Barcode::class.java)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toBarcodeJson(barcode: Barcode?): String? {
        if (barcode == null) return null
        val adapter = moshi.adapter(Barcode::class.java)
        return adapter.toJson(barcode)
    }

    @TypeConverter
    fun fromFieldsJson(value: String?): Map<String, PassField>? {
        if (value == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            PassField::class.java
        )
        val adapter = moshi.adapter<Map<String, PassField>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toFieldsJson(fields: Map<String, PassField>?): String? {
        if (fields == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            PassField::class.java
        )
        val adapter = moshi.adapter<Map<String, PassField>>(type)
        return adapter.toJson(fields)
    }
}
```

---

## Data Mappers

Convert between domain and data layer entities.

```kotlin
package labs.claucookie.pasbuk.data.mapper

import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import java.time.Instant

fun PassEntity.toDomain(): Pass = Pass(
    id = id,
    serialNumber = serialNumber,
    passTypeIdentifier = passTypeIdentifier,
    organizationName = organizationName,
    description = description,
    teamIdentifier = teamIdentifier,
    relevantDate = relevantDate?.let { Instant.ofEpochMilli(it) },
    expirationDate = expirationDate?.let { Instant.ofEpochMilli(it) },
    locations = locationsJson?.let { /* deserialize */ } ?: emptyList(),
    barcode = barcodeJson?.let { /* deserialize */ },
    logoText = logoText,
    backgroundColor = backgroundColor,
    foregroundColor = foregroundColor,
    labelColor = labelColor,
    logoImagePath = logoImagePath,
    iconImagePath = iconImagePath,
    thumbnailImagePath = thumbnailImagePath,
    stripImagePath = stripImagePath,
    backgroundImagePath = backgroundImagePath,
    originalPkpassPath = originalPkpassPath,
    passType = PassType.valueOf(passType),
    fields = fieldsJson?.let { /* deserialize */ } ?: emptyMap(),
    createdAt = Instant.ofEpochMilli(createdAt),
    modifiedAt = Instant.ofEpochMilli(modifiedAt)
)

fun Pass.toEntity(): PassEntity = PassEntity(
    id = id,
    serialNumber = serialNumber,
    passTypeIdentifier = passTypeIdentifier,
    organizationName = organizationName,
    description = description,
    teamIdentifier = teamIdentifier,
    relevantDate = relevantDate?.toEpochMilli(),
    expirationDate = expirationDate?.toEpochMilli(),
    locationsJson = locations.takeIf { it.isNotEmpty() }?.let { /* serialize */ },
    barcodeJson = barcode?.let { /* serialize */ },
    logoText = logoText,
    backgroundColor = backgroundColor,
    foregroundColor = foregroundColor,
    labelColor = labelColor,
    logoImagePath = logoImagePath,
    iconImagePath = iconImagePath,
    thumbnailImagePath = thumbnailImagePath,
    stripImagePath = stripImagePath,
    backgroundImagePath = backgroundImagePath,
    originalPkpassPath = originalPkpassPath,
    passType = passType.name,
    fieldsJson = fields.takeIf { it.isNotEmpty() }?.let { /* serialize */ },
    createdAt = createdAt.toEpochMilli(),
    modifiedAt = modifiedAt.toEpochMilli()
)
```

---

## Database Schema

```sql
CREATE TABLE passes (
    id TEXT PRIMARY KEY NOT NULL,
    serialNumber TEXT NOT NULL,
    passTypeIdentifier TEXT NOT NULL,
    organizationName TEXT NOT NULL,
    description TEXT NOT NULL,
    teamIdentifier TEXT NOT NULL,
    relevantDate INTEGER,
    expirationDate INTEGER,
    locationsJson TEXT,
    barcodeJson TEXT,
    fieldsJson TEXT,
    logoText TEXT,
    backgroundColor TEXT,
    foregroundColor TEXT,
    labelColor TEXT,
    logoImagePath TEXT,
    iconImagePath TEXT,
    thumbnailImagePath TEXT,
    stripImagePath TEXT,
    backgroundImagePath TEXT,
    originalPkpassPath TEXT NOT NULL,
    passType TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    modifiedAt INTEGER NOT NULL
);

CREATE TABLE journeys (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    modifiedAt INTEGER NOT NULL
);

CREATE TABLE journey_pass_cross_ref (
    journeyId INTEGER NOT NULL,
    passId TEXT NOT NULL,
    sortOrder INTEGER NOT NULL,
    PRIMARY KEY (journeyId, passId),
    FOREIGN KEY (journeyId) REFERENCES journeys(id) ON DELETE CASCADE,
    FOREIGN KEY (passId) REFERENCES passes(id) ON DELETE CASCADE
);

CREATE INDEX index_journey_pass_cross_ref_journeyId ON journey_pass_cross_ref(journeyId);
CREATE INDEX index_journey_pass_cross_ref_passId ON journey_pass_cross_ref(passId);
CREATE INDEX index_passes_relevantDate ON passes(relevantDate);
```

---

## Validation Rules

### Pass
- `id`: Must be unique, non-empty string
- `serialNumber`: Must be unique, non-empty string
- `organizationName`: Non-empty string
- `description`: Non-empty string
- `relevantDate`: Optional, must be valid timestamp if present
- `originalPkpassPath`: Must exist on file system
- `barcode.message`: Non-empty if barcode present

### Journey
- `name`: Non-empty string, max 100 characters
- `passes`: Can be empty list initially, must contain valid Pass references
- Journey name must be unique per user (enforced at repository level)

---

## State Transitions

### Pass Lifecycle
1. **Imported** → User selects .pkpass file
2. **Parsing** → PkpassParser extracts data
3. **Stored** → PassEntity saved to Room, files to internal storage
4. **Displayed** → Shown in timeline or pass detail screen
5. **Deleted** → PassEntity removed, associated files cleaned up

### Journey Lifecycle
1. **Created** → User names and saves selected passes
2. **Modified** → User adds/removes passes
3. **Viewed** → Journey displayed with sorted passes
4. **Deleted** → JourneyEntity removed, passes remain (cascade handled by cross-ref only)

---

## Performance Considerations

### Indexing
- Primary index on `passes.id` (automatic)
- Index on `passes.relevantDate` for timeline sorting
- Composite index on `journey_pass_cross_ref(journeyId, passId)` (automatic via primary key)
- Foreign key indices for efficient joins

### Query Optimization
- Use `@Transaction` annotation for JourneyWithPasses queries
- Limit timeline queries to recent/upcoming passes initially (pagination)
- Cache parsed pass.json data in Room to avoid repeated file I/O
- Load images lazily using Coil with file:// URIs

### File Management
- Clean up orphaned files when Pass deleted (use Room's onDelete callback)
- Compress stored images if size exceeds reasonable limit (500KB per image)
- Use ContentResolver for .pkpass import to handle various URI schemes

---

## Migration Strategy

**Version 1 (Initial)**: Schema defined above

**Future Considerations**:
- Add `lastViewedAt` timestamp for sorting by recency
- Add `isFavorite` flag for quick access
- Add full-text search index on description/organizationName
- Support for pass updates (new version of same serialNumber)
- Sync support (would require server-side schema)
