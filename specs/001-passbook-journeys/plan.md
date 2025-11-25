# Implementation Plan: Passbook Timeline and Journeys

**Branch**: `001-passbook-journeys` | **Date**: 2025-11-21 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-passbook-journeys/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build an Android application that enables users to import Apple Passbook (.pkpass) files, view them in a chronological timeline, and organize them into named "Journeys" (grouped collections of passes sorted by date/time). The app will provide three core capabilities: importing .pkpass files from device storage, displaying pass details with barcodes, and creating/managing journey collections.

## Technical Context

**Language/Version**: Kotlin (latest stable, targeting JVM 11)
**Primary Dependencies**: Jetpack Compose (BOM), Coroutines, Flow, NEEDS CLARIFICATION: DI framework (Hilt vs Koin), NEEDS CLARIFICATION: .pkpass parsing library
**Storage**: Room (for Pass and Journey persistence), NEEDS CLARIFICATION: File storage approach for .pkpass binary data
**Testing**: JUnit, NEEDS CLARIFICATION: Mocking framework (Mockito vs MockK), Espresso, Jetpack Compose Testing
**Target Platform**: Android API 28+ (minSdk: 28, targetSdk: 36)
**Project Type**: Mobile (Android)
**Performance Goals**: Import/view pass <10 seconds, Timeline load (100 passes) <2 seconds, 60 FPS UI, smooth scrolling
**Constraints**: Offline-first, local-only storage, no cloud sync in v1
**Scale/Scope**: Single-user device application, expected ~100-500 passes per user

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Code Quality & MAD**: ✅ PASS
  - Kotlin-first: Yes, all code will be Kotlin
  - Jetpack Compose: Yes, UI built with Compose (Material3)
  - Structured architecture: Yes, will follow UI/Domain/Data layer separation
  - DI framework: To be selected in Phase 0 (Hilt vs Koin)
  - Coroutines/Flow: Yes, for async operations

- **II. Comprehensive Testing**: ✅ PASS (Planned)
  - Unit tests: Required for ViewModels, UseCases, Repositories (parsing, journey creation logic)
  - Integration tests: Required for Room DB interactions, pass import flow
  - UI tests: Required for critical flows (import pass, create journey, view timeline)
  - Target: 70%+ coverage as per constitution

- **III. Consistent UX**: ✅ PASS (Planned)
  - Material Design 3: Yes, using androidx.compose.material3
  - Reusable components: Will create PassCard, JourneyCard, BarcodeDisplay components
  - Responsive design: Must support portrait/landscape, various screen sizes
  - Accessibility: TalkBack support, color contrast compliance required

- **IV. Performance & Optimization**: ✅ PASS (Addressed)
  - Specific performance targets defined in spec: <10s import, <2s timeline load
  - Room DB will handle efficient querying for timeline
  - Image loading optimization needed for pass images/barcodes
  - Memory management: proper cleanup of .pkpass file streams
  - Will monitor with Profiler during development

**Initial Gate Result**: ✅ PASS - All principles addressed in plan. No violations requiring justification.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/labs/claucookie/pasbuk/
├── data/                       # Data Layer
│   ├── local/
│   │   ├── dao/               # Room DAOs
│   │   │   ├── PassDao.kt
│   │   │   └── JourneyDao.kt
│   │   ├── entity/            # Room entities
│   │   │   ├── PassEntity.kt
│   │   │   ├── JourneyEntity.kt
│   │   │   └── JourneyPassCrossRef.kt
│   │   └── AppDatabase.kt
│   ├── repository/            # Repository implementations
│   │   ├── PassRepositoryImpl.kt
│   │   └── JourneyRepositoryImpl.kt
│   └── parser/                # .pkpass parsing
│       └── PkpassParser.kt
│
├── domain/                     # Domain Layer
│   ├── model/                 # Domain entities
│   │   ├── Pass.kt
│   │   └── Journey.kt
│   ├── repository/            # Repository interfaces
│   │   ├── PassRepository.kt
│   │   └── JourneyRepository.kt
│   └── usecase/               # Use cases
│       ├── ImportPassUseCase.kt
│       ├── GetTimelineUseCase.kt
│       ├── CreateJourneyUseCase.kt
│       └── GetJourneyDetailsUseCase.kt
│
├── ui/                         # UI Layer
│   ├── screens/
│   │   ├── timeline/
│   │   │   ├── TimelineScreen.kt
│   │   │   └── TimelineViewModel.kt
│   │   ├── passdetail/
│   │   │   ├── PassDetailScreen.kt
│   │   │   └── PassDetailViewModel.kt
│   │   └── journey/
│   │       ├── JourneyListScreen.kt
│   │       ├── JourneyDetailScreen.kt
│   │       └── JourneyViewModel.kt
│   ├── components/            # Reusable UI components
│   │   ├── PassCard.kt
│   │   ├── JourneyCard.kt
│   │   └── BarcodeDisplay.kt
│   ├── navigation/
│   │   └── Navigation.kt
│   └── theme/                 # (Already exists)
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── di/                         # Dependency Injection
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
│
└── MainActivity.kt            # (Already exists)

app/src/test/java/labs/claucookie/pasbuk/
├── domain/usecase/            # Unit tests for use cases
├── data/repository/           # Unit tests for repositories
└── data/parser/               # Unit tests for .pkpass parser

app/src/androidTest/java/labs/claucookie/pasbuk/
├── data/local/                # Room DB integration tests
└── ui/                        # Compose UI tests
```

**Structure Decision**: Standard Android clean architecture with three layers (UI/Domain/Data). This aligns with the constitution's requirement to follow the official "Guide to app architecture." The structure separates concerns clearly: Data layer handles persistence and parsing, Domain layer contains business logic in use cases, and UI layer manages Compose screens with ViewModels.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations. Constitution check passed.

---

## Post-Design Constitution Re-check

*Re-evaluation after Phase 1 (Research, Data Model, Contracts) completion*

### I. Code Quality & MAD: ✅ PASS
- **Kotlin-first**: Confirmed - all entities, repositories, use cases in Kotlin
- **Jetpack Compose**: Confirmed - UI layer uses Compose (Material3)
- **Structured architecture**: Confirmed - clear UI/Domain/Data separation with contracts
- **DI framework**: ✅ RESOLVED - Hilt selected (research.md)
- **Coroutines/Flow**: Confirmed - repositories use suspend functions and Flow

### II. Comprehensive Testing: ✅ PASS
- **Unit tests**: Defined for use cases (MockK), repositories, parser
- **Integration tests**: Planned for Room DAOs with in-memory database
- **UI tests**: Planned for critical flows (import, create journey, timeline)
- **Target coverage**: 70%+ defined in quickstart.md verification checklist

### III. Consistent UX: ✅ PASS
- **Material Design 3**: Confirmed in quickstart.md setup
- **Reusable components**: PassCard, JourneyCard, BarcodeDisplay defined
- **Responsive design**: Mentioned in Constitution Check
- **Accessibility**: TalkBack testing in verification checklist

### IV. Performance & Optimization: ✅ PASS
- **Performance targets**: Maintained from spec (SC-001: <10s, SC-002: <2s)
- **Database optimization**: Indices defined in data-model.md
- **File storage strategy**: Internal storage with Room storing paths (performance-optimized)
- **Image loading**: Coil with lazy loading mentioned in quickstart.md
- **Memory management**: Profiler checks in verification checklist

**Final Gate Result**: ✅ PASS - All constitution principles satisfied. Design is complete and ready for task generation (`/speckit.tasks`).
