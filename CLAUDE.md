# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pasbuk Enhanced is an Android application for managing Apple Passbook (.pkpass) files. Users can import passes, view them in a timeline, and organize them into named "Journeys" (grouped collections sorted by date/time).

**Package**: `labs.claucookie.pasbuk`
**Min SDK**: 28 (Android 9.0)
**Target SDK**: 36
**Language**: Kotlin only

## Essential Commands

### Build & Run
```bash
# Build the project
./gradlew build

# Clean and rebuild
./gradlew clean build

# Run debug build on connected device/emulator
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run specific unit test
./gradlew test --tests labs.claucookie.pasbuk.SpecificTestClass

# Run all instrumented (Android) tests
./gradlew connectedAndroidTest

# Run specific instrumented test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=labs.claucookie.pasbuk.SpecificTestClass

# Generate test coverage report (after configuring coverage)
./gradlew testDebugUnitTestCoverage
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Format code (if ktlint/spotless configured)
./gradlew ktlintFormat
```

## Architecture & Standards

### Mandatory Principles (from constitution.md v1.0.0)

All code MUST adhere to the project constitution at `.specify/memory/constitution.md`. Key requirements:

1. **Code Quality**:
   - All code MUST be Kotlin following official conventions
   - UI MUST use Jetpack Compose
   - Async operations MUST use Coroutines and Flow
   - DI MUST use Hilt or Koin
   - Architecture MUST follow official "Guide to app architecture" with clear UI/Domain/Data layer separation

2. **Testing**:
   - TDD strongly encouraged
   - Unit tests REQUIRED for all business logic (ViewModels, UseCases, Repositories)
   - Minimum 70% test coverage
   - Integration tests SHOULD cover layer interactions
   - UI tests REQUIRED for critical flows using Compose testing APIs

3. **UX Standards**:
   - MUST follow Material Design 3 guidelines
   - Components SHOULD be reusable
   - MUST be responsive across screen sizes/orientations
   - Accessibility REQUIRED (TalkBack support, color contrast)

4. **Performance**:
   - No jank or freezes
   - Minimize startup time
   - Monitor and optimize memory usage
   - Efficient network operations with caching

### Expected Layer Structure

The app should follow clean architecture:

```
app/src/main/java/labs/claucookie/pasbuk/
├── data/           # Data layer - repositories, data sources, Room DB
│   ├── local/      # Room entities, DAOs
│   ├── repository/ # Repository implementations
│   └── model/      # Data models
├── domain/         # Domain layer - use cases, business logic
│   ├── model/      # Domain entities (Pass, Journey)
│   └── usecase/    # Use case classes
├── ui/             # UI layer - Compose screens, ViewModels
│   ├── screens/    # Screen composables (timeline, detail, journey)
│   ├── components/ # Reusable UI components
│   ├── navigation/ # Navigation setup
│   └── theme/      # Theme, colors, typography (currently exists)
└── di/             # Dependency injection modules
```

## Development Workflow

### Pull Request Requirements
- ALL changes MUST go through PR
- MUST have at least one approval
- MUST pass CI checks (lint, tests, build)

### Working with Specs

This project uses **spec-kit** for feature specification and implementation. Specs are in `specs/[feature-branch-name]/`:

- **spec.md**: Feature specification with user stories, requirements, success criteria
- **plan.md**: Technical implementation plan with architecture decisions
- **tasks.md**: Detailed task breakdown for implementation
- **checklists/**: Validation checklists (requirements, testing, etc.)

#### Spec-kit Slash Commands (in `.claude/commands/`)
Available via `/speckit.*` commands:
- `/speckit.analyze`: Analyze spec requirements
- `/speckit.plan`: Generate implementation plan
- `/speckit.tasks`: Break down implementation into tasks
- `/speckit.implement`: Execute implementation from tasks
- `/speckit.checklist`: Manage validation checklists
- `/speckit.clarify`: Clarify requirements
- `/speckit.constitution`: Review project standards
- `/speckit.specify`: Create new feature spec

## Current Feature: Passbook Journeys

**Branch**: `001-passbook-journeys`
**Spec**: `specs/001-passbook-journeys/spec.md`

### Core Entities
- **Pass**: Parsed .pkpass file with event details, date, barcode, images
- **Journey**: Named collection of passes sorted by date/time

### Functional Requirements (from spec.md)
- FR-001: Import .pkpass files from device storage
- FR-002: Parse and display pass contents (name, date, location, barcode)
- FR-003: Display passes in chronological timeline
- FR-004: Select multiple passes
- FR-005: Create named Journey from selected passes
- FR-006: List all Journeys
- FR-007: Display Journey contents sorted chronologically
- FR-008: Persist passes and journeys locally

### Performance Targets
- Import/view pass: < 10 seconds
- Timeline load (100 passes): < 2 seconds
- Journey creation success rate: 95%

## Key Dependencies

From `app/build.gradle.kts`:
- Kotlin with Compose
- AndroidX Core KTX, Lifecycle Runtime KTX
- Jetpack Compose (BOM, UI, Material3, Activity Compose)
- JUnit for unit tests
- Espresso and Compose UI Test for instrumented tests

## Notes

- The project is currently at initialization stage with minimal boilerplate
- No DI framework configured yet (REQUIRED: add Hilt or Koin)
- No Room database setup yet (REQUIRED for FR-008)
- No passbook parsing library integrated yet
- Test directory structure exists but needs actual test implementation

## Active Technologies
- Kotlin (latest stable, targeting JVM 11) (001-passbook-journeys)

## Recent Changes
- 001-passbook-journeys: Added Kotlin (latest stable, targeting JVM 11)
