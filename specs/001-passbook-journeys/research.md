# Research: Passbook Timeline and Journeys

**Date**: 2025-11-21
**Feature**: 001-passbook-journeys
**Purpose**: Resolve technical unknowns from implementation plan

## Research Questions

This document resolves the following NEEDS CLARIFICATION items from plan.md:
1. DI framework selection (Hilt vs Koin)
2. .pkpass parsing library
3. File storage approach for .pkpass binary data
4. Mocking framework (Mockito vs MockK)

---

## 1. Dependency Injection Framework

### Decision: **Hilt**

### Rationale:
- **Official Google Recommendation**: Hilt is Android's official DI solution, built on Dagger with native Jetpack integration
- **Compile-Time Safety**: Catches DI errors at build time rather than runtime, aligning with constitution's code quality requirements
- **Jetpack Integration**: Native support for ViewModel, Navigation, and Compose - all critical for this app
- **Performance**: Better runtime performance than Koin due to compile-time resolution
- **Modern Setup**: With KSP (Kotlin Symbol Processing), Hilt now has up to 2x faster build times vs KAPT

### Alternatives Considered:
- **Koin**: Runtime-based DI with Kotlin DSL. Easier to learn but lacks compile-time safety. Good for rapid prototyping but Hilt's guarantees better fit enterprise-grade requirements.
- **Manual DI**: Too much boilerplate for multi-layer architecture (UI/Domain/Data)

### Dependencies Required:
```kotlin
// build.gradle.kts (project)
plugins {
    id("com.google.devtools.ksp") version "1.9.x-x.x.x"
    id("com.google.dagger.hilt.android") version "2.57.1" apply false
}

// build.gradle.kts (app)
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
}
```

---

## 2. .pkpass Parsing Library

### Decision: **Custom Implementation using Android Standard Libraries**

### Rationale:
- **No Mature Kotlin Library**: No actively maintained, production-ready Kotlin library exists for .pkpass parsing as of 2025
- **Simple File Format**: .pkpass files are ZIP archives containing:
  - `pass.json` (all metadata)
  - Image assets (logo.png, icon.png, etc.)
  - `manifest.json` (checksums)
  - `signature` (for validation)
- **Android Built-in Support**: Can handle with standard libraries:
  - `java.util.zip.ZipInputStream` for extraction
  - `kotlinx.serialization` or `Moshi` for JSON parsing
  - Android's `BitmapFactory` for images
- **Reference Implementation**: PassAndroid (ligi/PassAndroid) demonstrates this approach successfully in Kotlin

### Alternatives Considered:
- **passkit4j**: Java library for *generating* .pkpass files, not parsing
- **PassAndroid as Dependency**: Full GPL v3.0 application, not a library. Licensing concerns for commercial use.
- **Dart pkpass Package**: Requires Flutter, not appropriate for native Kotlin app

### Implementation Approach:
```kotlin
dependencies {
    // JSON parsing (recommended: Moshi for codegen)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Barcode rendering
    implementation("com.google.zxing:core:3.5.2")
}
```

**Processing Steps**:
1. Use `ZipInputStream` to extract .pkpass archive
2. Parse `pass.json` to Kotlin data classes
3. Extract image assets to internal storage
4. Optionally validate `signature` for security
5. Store parsed data in Room with file paths to images

---

## 3. File Storage Approach

### Decision: **Store Metadata in Room + Image File Paths + Original .pkpass in Internal Storage**

### Rationale:
- **Android Best Practice**: Storing file paths in Room and binary files on file system is recommended approach (2025)
- **Technical Limitations**: SQLite/Room cursor buffer limited to 4MB. Storing large BLOBs can crash app.
- **Performance**: File system access is faster for large files than BLOB retrieval from SQLite
- **Database Size**: Storing files as BLOBs bloats database, slowing queries, backups, and migrations
- **Flexibility**: Keeping original .pkpass allows re-export without data loss
- **Security**: Internal storage is private to app, no permissions required

### Alternatives Considered:
- **Store Everything as BLOBs in Room**: Violates best practices, risks 4MB limit, poor performance
- **Extract and Discard Original**: Loses ability to re-export or share original file
- **External Storage**: Less secure, requires complex scoped storage handling on Android 11+

### Implementation:
```kotlin
// Storage structure in context.filesDir:
// /data/data/labs.claucookie.pasbuk/files/
//   - /passes/original/{passId}.pkpass
//   - /passes/images/{passId}/logo.png
//   - /passes/images/{passId}/icon.png

// Room entities store paths, not blobs
@Entity(tableName = "passes")
data class PassEntity(
    @PrimaryKey val id: String,
    val serialNumber: String,
    val organizationName: String,
    val description: String,
    val eventDate: Long?,
    val logoImagePath: String?,      // File path, not blob
    val iconImagePath: String?,       // File path, not blob
    val originalPkpassPath: String,   // Keep original file
    val createdAt: Long,
    val modifiedAt: Long
)
```

**Benefits**:
- Automatic cleanup when app uninstalled
- No runtime permissions needed
- Can be included in Auto Backup for Android
- Private to app (perfect for personal passes)

---

## 4. Mocking Framework

### Decision: **MockK**

### Rationale:
- **Kotlin-First Design**: Built specifically for Kotlin with idiomatic syntax (aligns with constitution's "All code MUST be Kotlin")
- **Native Coroutines Support**: Provides `coEvery`, `coVerify` for suspend functions out of the box
- **No Configuration Needed**: Mocks final classes, objects, extension functions without setup (Kotlin makes everything final by default)
- **Active Maintenance**: Version 1.14.5 (July 2025), 5.6k GitHub stars
- **Flow Testing**: Works seamlessly with `kotlinx-coroutines-test` and `Turbine`

### Alternatives Considered:
- **Mockito**: Designed for Java. Requires `mockito-kotlin` wrapper and `mockito-inline` for final classes. More verbose for Kotlin.
- **Mockito-Kotlin**: Wrapper improving Mockito for Kotlin, but lacks first-class coroutine support

### Dependencies Required:
```kotlin
dependencies {
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("app.cash.turbine:turbine:1.1.0") // For Flow testing

    androidTestImplementation("io.mockk:mockk-android:1.14.5")
}
```

### Example Test Pattern:
```kotlin
class ImportPassUseCaseTest {
    private lateinit var passRepository: PassRepository
    private lateinit var useCase: ImportPassUseCase

    @Before
    fun setup() {
        passRepository = mockk()
        useCase = ImportPassUseCase(passRepository)
    }

    @Test
    fun `importing valid pkpass returns success`() = runTest {
        val uri = mockk<Uri>()
        val expectedPass = mockk<Pass>()
        coEvery { passRepository.importPass(uri) } returns Result.success(expectedPass)

        val result = useCase(uri)

        assertTrue(result.isSuccess)
        coVerify { passRepository.importPass(uri) }
    }
}
```

---

## Summary Table

| Decision | Recommendation | Key Benefit | Constitution Alignment |
|----------|---------------|-------------|------------------------|
| **DI Framework** | Hilt | Compile-time safety, official Google support | Code Quality (MAD) |
| **.pkpass Parsing** | Custom with standard libs | Full control, no heavy dependencies | Code Quality |
| **File Storage** | Room metadata + file paths | Performance, Android best practices | Performance & Optimization |
| **Mocking** | MockK | Kotlin-first, coroutines support | Comprehensive Testing |

---

## Impact on Technical Context

All NEEDS CLARIFICATION items now resolved:
- ✅ **DI framework**: Hilt selected
- ✅ **.pkpass parsing library**: Custom implementation with Moshi + ZipInputStream
- ✅ **File storage approach**: Internal storage with Room storing paths
- ✅ **Mocking framework**: MockK selected

These decisions fully align with the project constitution v1.0.0 requirements for Modern Android Development, comprehensive testing, and performance optimization.
