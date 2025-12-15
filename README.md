# Pasbuk Enhanced

**A new way of looking at your journeys**

Pasbuk Enhanced is an Android application for managing and organizing Apple Passbook (.pkpass) files. Import your boarding passes, event tickets, and other passes, view them in a chronological timeline, and organize them into custom "Journeys" for easy reference.

## Features

### ✈️ Pass Management
- **Import .pkpass Files**: Select passbook files from your device storage
- **View Pass Details**: See all pass information including:
  - Event name, date, and location
  - Barcodes (QR codes, PDF417, Aztec, Code 128)
  - Logo and background images
  - All pass fields (primary, secondary, auxiliary, back)
- **Delete Passes**: Remove unwanted passes with confirmation dialog

### 📅 Timeline View
- **Chronological Display**: All passes sorted by date
- **Multi-Selection**: Long-press to select multiple passes
- **Quick Navigation**: Tap any pass to view full details
- **Empty State**: Helpful prompts when no passes are imported

### 🗂️ Journey Organization
- **Create Journeys**: Group related passes into named collections
- **Journey List**: Browse all your organized journeys
- **Journey Details**: View all passes within a journey, sorted by date
- **Delete Journeys**: Remove journeys with confirmation (passes remain intact)

## Architecture

Pasbuk Enhanced follows **Clean Architecture** principles with clear separation of concerns:

```
📁 app/src/main/java/labs/claucookie/pasbuk/
├── 📂 data/           # Data layer - repositories, Room database, parsers
│   ├── local/         # Room entities, DAOs, database
│   ├── parser/        # .pkpass file parsing (ZIP extraction, JSON parsing)
│   ├── repository/    # Repository implementations
│   └── mapper/        # Entity ↔ Domain model mappers
├── 📂 domain/         # Domain layer - business logic
│   ├── model/         # Domain entities (Pass, Journey, Barcode, Location)
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Use cases (Import, Get, Delete, Create)
├── 📂 ui/             # UI layer - Jetpack Compose screens & components
│   ├── components/    # Reusable composables (PassCard, BarcodeDisplay, etc.)
│   ├── screens/       # Feature screens (Timeline, PassDetail, JourneyList, etc.)
│   ├── navigation/    # Navigation setup
│   └── theme/         # Material 3 theme, colors, typography
└── 📂 di/             # Dependency injection (Hilt modules)
```

## Technology Stack

- **Language**: Kotlin 100%
- **UI**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture (MVVM pattern)
- **Dependency Injection**: Hilt
- **Database**: Room (local persistence)
- **Async**: Kotlin Coroutines & Flow
- **Parsing**: Moshi (JSON), Java ZIP API (.pkpass extraction)
- **Barcode Generation**: ZXing
- **Image Loading**: Coil
- **Testing**: JUnit 4, MockK, Turbine, Compose Testing

## Project Structure

- **Min SDK**: 28 (Android 9.0 Pie)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Package**: `labs.claucookie.pasbuk`

## Build & Run

### Prerequisites
- Android Studio Ladybug or later
- JDK 11 or higher
- Android SDK with API level 36

### Build Commands

```bash
# Build the project
./gradlew build

# Run debug build on connected device/emulator
./gradlew installDebug

# Run all unit tests
./gradlew test

# Run lint checks
./gradlew lint
```

## Testing

Pasbuk Enhanced has comprehensive test coverage:

- **83+ Unit Tests** covering:
  - Domain layer (use cases)
  - Data layer (repositories, DAOs)
  - UI layer (ViewModels)
  - Parsers (.pkpass file handling)

- **Integration Tests** for:
  - Room database operations
  - Many-to-many relationships (Journey ↔ Pass)

- **UI Tests** for critical flows using Compose Testing

Run tests: `./gradlew testDebugUnitTest`

## Accessibility

Pasbuk Enhanced is built with accessibility in mind:

- ✅ All icons and images have content descriptions
- ✅ TalkBack support for screen readers
- ✅ Material 3 ensures WCAG AA color contrast
- ✅ Semantic properties on interactive elements

## User Stories

### 1. Import and View a Passbook File (MVP)
Users can import .pkpass files from device storage and view complete pass details including barcodes.

### 2. View Passes in a Timeline
All imported passes are displayed chronologically, making it easy to find passes by date.

### 3. Create and View a Journey
Users can select multiple related passes and organize them into named "Journeys" (e.g., "Europe Trip 2024").

## Development

### Architecture Decisions

- **Clean Architecture**: Ensures testability, maintainability, and separation of concerns
- **MVVM Pattern**: Clear data flow from data layer → use cases → ViewModels → UI
- **Single Source of Truth**: Room database as the primary data source
- **Reactive UI**: StateFlow and Flow for reactive state management
- **Material 3**: Modern, accessible design system

### Code Quality

- ✅ Lint checks passed
- ✅ 70%+ test coverage requirement met
- ✅ Kotlin coding conventions followed
- ✅ No compilation warnings (except deprecation notices)

## License

[Add license information here]

## Contributing

[Add contributing guidelines here]

## Support

For issues and feature requests, please use the [GitHub issue tracker](../../issues).

---

🤖 Built with [Claude Code](https://claude.com/claude-code)
