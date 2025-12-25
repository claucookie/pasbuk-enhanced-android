# Tasks: Passbook Timeline and Journeys

**Input**: Design documents from `/specs/001-passbook-journeys/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests are included as per constitution requirement for 70%+ coverage. Unit tests for use cases and repositories, integration tests for Room DB, UI tests for critical flows.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android app**: `app/src/main/java/labs/claucookie/pasbuk/`, `app/src/test/java/labs/claucookie/pasbuk/`, `app/src/androidTest/java/labs/claucookie/pasbuk/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and dependency configuration

- [x] T001 Update `build.gradle.kts` (project level) to add KSP and Hilt plugins
- [x] T002 Update `app/build.gradle.kts` with Hilt, Room, Moshi, ZXing, Coil, and MockK dependencies per research.md
- [x] T003 [P] Create `PasbukApplication.kt` in `app/src/main/java/labs/claucookie/pasbuk/` with @HiltAndroidApp annotation
- [x] T004 [P] Update `AndroidManifest.xml` to reference PasbukApplication
- [x] T005 [P] Update `MainActivity.kt` in `app/src/main/java/labs/claucookie/pasbuk/` with @AndroidEntryPoint annotation
- [x] T006 Sync Gradle and verify all dependencies resolve correctly

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Domain Layer Foundation

- [x] T007 [P] Create domain model enums in `app/src/main/java/labs/claucookie/pasbuk/domain/model/` (PassType.kt, BarcodeFormat.kt, TextAlignment.kt)
- [x] T008 [P] Create `Barcode.kt` data class in `app/src/main/java/labs/claucookie/pasbuk/domain/model/`
- [x] T009 [P] Create `Location.kt` data class in `app/src/main/java/labs/claucookie/pasbuk/domain/model/`
- [x] T010 [P] Create `PassField.kt` data class in `app/src/main/java/labs/claucookie/pasbuk/domain/model/`
- [x] T011 Create `Pass.kt` domain model in `app/src/main/java/labs/claucookie/pasbuk/domain/model/` (depends on T007-T010)
- [x] T012 [P] Create `Journey.kt` domain model in `app/src/main/java/labs/claucookie/pasbuk/domain/model/`

### Repository Interfaces

- [x] T013 [P] Create custom exceptions in `app/src/main/java/labs/claucookie/pasbuk/domain/repository/` (InvalidPassException.kt, DuplicatePassException.kt, DuplicateJourneyNameException.kt)
- [x] T014 [P] Create `PassRepository.kt` interface in `app/src/main/java/labs/claucookie/pasbuk/domain/repository/`
- [x] T015 [P] Create `JourneyRepository.kt` interface in `app/src/main/java/labs/claucookie/pasbuk/domain/repository/`

### Data Layer Foundation

- [x] T016 [P] Create `PassTypeConverter.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/` with Moshi-based type converters
- [x] T017 [P] Create `PassEntity.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/` with @Entity annotation
- [x] T018 [P] Create `JourneyEntity.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/`
- [x] T019 [P] Create `JourneyPassCrossRef.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/` with foreign keys
- [x] T020 [P] Create `JourneyWithPasses.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/local/entity/` with @Relation annotations
- [x] T021 [P] Create `PassDao.kt` interface in `app/src/main/java/labs/claucookie/pasbuk/data/local/dao/` with Room queries
- [x] T022 [P] Create `JourneyDao.kt` interface in `app/src/main/java/labs/claucookie/pasbuk/data/local/dao/` with Room queries
- [x] T023 Create `AppDatabase.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/local/` (depends on T017-T022)

### Data Mappers

- [x] T024 [P] Create `PassMapper.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/mapper/` with toDomain() and toEntity() extension functions
- [x] T025 [P] Create `JourneyMapper.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/mapper/` with toDomain() and toEntity() extension functions

### Dependency Injection Modules

- [x] T026 [P] Create `DatabaseModule.kt` in `app/src/main/java/labs/claucookie/pasbuk/di/` providing AppDatabase, PassDao, JourneyDao
- [x] T027 [P] Create `MoshiModule.kt` in `app/src/main/java/labs/claucookie/pasbuk/di/` providing Moshi instance
- [x] T028 [P] Create `RepositoryModule.kt` in `app/src/main/java/labs/claucookie/pasbuk/di/` (placeholder, will bind repositories in later phases)
- [x] T029 [P] Create `UseCaseModule.kt` in `app/src/main/java/labs/claucookie/pasbuk/di/` (placeholder, will provide use cases in later phases)

### Navigation Foundation

- [x] T030 [P] Create `Navigation.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/navigation/` with sealed Screen class and NavHost composable
- [x] T031 Update `MainActivity.kt` to use PasbukNavigation composable

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Import and View a Passbook File (Priority: P1) 🎯 MVP

**Goal**: Enable users to import a .pkpass file from device storage and view its details including barcode

**Independent Test**: User selects a .pkpass file from device, app parses it and displays pass detail screen with event name, date, and barcode

### Tests for User Story 1 (TDD Approach)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T032 [P] [US1] Unit test for PkpassParser in `app/src/test/java/labs/claucookie/pasbuk/data/parser/PkpassParserTest.kt` (test parsing valid .pkpass)
- [ ] T033 [P] [US1] Unit test for PkpassParser in `app/src/test/java/labs/claucookie/pasbuk/data/parser/PkpassParserTest.kt` (test parsing corrupted .pkpass throws exception)
- [ ] T034 [P] [US1] Unit test for ImportPassUseCase in `app/src/test/java/labs/claucookie/pasbuk/domain/usecase/ImportPassUseCaseTest.kt`
- [ ] T035 [P] [US1] Unit test for GetPassDetailUseCase in `app/src/test/java/labs/claucookie/pasbuk/domain/usecase/GetPassDetailUseCaseTest.kt`
- [ ] T036 [P] [US1] Unit test for PassRepositoryImpl in `app/src/test/java/labs/claucookie/pasbuk/data/repository/PassRepositoryImplTest.kt` using MockK
- [ ] T037 [P] [US1] Integration test for PassDao in `app/src/androidTest/java/labs/claucookie/pasbuk/data/local/dao/PassDaoTest.kt` with in-memory database
- [ ] T038 [P] [US1] UI test for PassDetailScreen in `app/src/androidTest/java/labs/claucookie/pasbuk/ui/screens/passdetail/PassDetailScreenTest.kt` using Compose testing

### Implementation for User Story 1

#### .pkpass Parser

- [x] T039 [US1] Create `PassJson.kt` data classes in `app/src/main/java/labs/claucookie/pasbuk/data/parser/` with Moshi @JsonClass annotations for pass.json structure
- [x] T040 [US1] Implement `PkpassParser.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/parser/` to extract ZIP, parse pass.json, save images to internal storage (depends on T039)

#### Repository Implementation

- [x] T041 [US1] Implement `PassRepositoryImpl.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/repository/` with importPass(), getPassById(), deletePass() methods (depends on T040)
- [x] T042 [US1] Update `RepositoryModule.kt` to bind PassRepository to PassRepositoryImpl

#### Use Cases

- [x] T043 [P] [US1] Implement `ImportPassUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T042)
- [x] T044 [P] [US1] Implement `GetPassDetailUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T042)
- [x] T045 [P] [US1] Implement `DeletePassUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T042)
- [x] T046 [US1] Update `UseCaseModule.kt` to provide ImportPassUseCase, GetPassDetailUseCase, DeletePassUseCase

#### UI Components

- [x] T047 [P] [US1] Create `BarcodeDisplay.kt` composable in `app/src/main/java/labs/claucookie/pasbuk/ui/components/` using ZXing to render barcodes
- [x] T048 [P] [US1] Create `PassCard.kt` composable in `app/src/main/java/labs/claucookie/pasbuk/ui/components/` for pass summary display with Coil for images

#### Pass Detail Screen

- [x] T049 [US1] Create `PassDetailUiState.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/passdetail/` with Loading, Success, Error states
- [x] T050 [US1] Implement `PassDetailViewModel.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/passdetail/` with @HiltViewModel, inject GetPassDetailUseCase (depends on T046)
- [x] T051 [US1] Implement `PassDetailScreen.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/passdetail/` displaying pass fields and barcode (depends on T047-T050)
- [x] T052 [US1] Add PassDetail route to Navigation.kt and wire up navigation parameter (passId)

#### Import Flow (File Picker Integration)

- [x] T053 [US1] Add file picker launcher to PassDetailViewModel or create ImportViewModel for handling file selection
- [x] T054 [US1] Create temporary import screen or dialog to trigger file picker and show import progress/errors
- [x] T055 [US1] Wire import flow to navigate to PassDetailScreen after successful import

#### Error Handling & Edge Cases

- [x] T056 [US1] Add error handling for invalid .pkpass files (show user-friendly error message)
- [x] T057 [US1] Add error handling for corrupted .pkpass files (show "file unreadable" message)
- [x] T058 [US1] Implement duplicate detection by serialNumber (prevent duplicate imports)

**Checkpoint**: At this point, User Story 1 should be fully functional - user can import a pass and view its details

---

## Phase 4: User Story 2 - View Passes in a Timeline (Priority: P2)

**Goal**: Display all imported passes in a chronological timeline sorted by relevantDate

**Independent Test**: After importing multiple passes with different dates, user sees them displayed on main screen sorted with most recent first

### Tests for User Story 2

- [x] T059 [P] [US2] Unit test for GetTimelineUseCase in `app/src/test/java/labs/claucookie/pasbuk/domain/usecase/GetTimelineUseCaseTest.kt`
- [x] T060 [P] [US2] Unit test for TimelineViewModel in `app/src/test/java/labs/claucookie/pasbuk/ui/screens/timeline/TimelineViewModelTest.kt` with MockK
- [x] T061 [P] [US2] Integration test for PassDao.getAllSortedByDate() in `app/src/androidTest/java/labs/claucookie/pasbuk/data/local/dao/PassDaoTest.kt`
- [x] T062 [P] [US2] UI test for TimelineScreen in `app/src/androidTest/java/labs/claucookie/pasbuk/ui/screens/timeline/TimelineScreenTest.kt` verifying chronological order

### Implementation for User Story 2

#### Repository Enhancement

- [x] T063 [US2] Add getAllPassesSortedByDate() implementation to PassRepositoryImpl.kt returning Flow<List<Pass>>

#### Use Case

- [x] T064 [US2] Implement `GetTimelineUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T063)
- [x] T065 [US2] Update UseCaseModule.kt to provide GetTimelineUseCase

#### Timeline Screen

- [x] T066 [US2] Create `TimelineUiState.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/timeline/` with Loading, Success(passes), Empty states
- [x] T067 [US2] Implement `TimelineViewModel.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/timeline/` with @HiltViewModel, inject GetTimelineUseCase (depends on T065)
- [x] T068 [US2] Implement `TimelineScreen.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/timeline/` with LazyColumn displaying PassCard items (depends on T048, T067)
- [x] T069 [US2] Add Timeline route to Navigation.kt and set as start destination
- [x] T070 [US2] Wire up PassCard click navigation to PassDetailScreen with passId parameter

#### Import Button Integration

- [x] T071 [US2] Add FloatingActionButton to TimelineScreen for triggering import flow
- [x] T072 [US2] Connect FAB to file picker launcher and import flow from User Story 1

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - user can import passes and see them in timeline

---

## Phase 5: User Story 3 - Create and View a Journey (Priority: P3)

**Goal**: Allow users to select multiple passes from timeline and group them into a named Journey

**Independent Test**: User selects multiple passes from timeline, creates a journey with a name, navigates to Journeys list and views the journey details with passes sorted by date

### Tests for User Story 3

- [ ] T073 [P] [US3] Unit test for CreateJourneyUseCase in `app/src/test/java/labs/claucookie/pasbuk/domain/usecase/CreateJourneyUseCaseTest.kt` (test name validation)
- [ ] T074 [P] [US3] Unit test for GetAllJourneysUseCase in `app/src/test/java/labs/claucookie/pasbuk/domain/usecase/GetAllJourneysUseCaseTest.kt`
- [ ] T075 [P] [US3] Unit test for GetJourneyDetailUseCase in `app/src/test/java/labs/claucookie/pasbuk/domain/usecase/GetJourneyDetailUseCaseTest.kt`
- [ ] T076 [P] [US3] Unit test for JourneyRepositoryImpl in `app/src/test/java/labs/claucookie/pasbuk/data/repository/JourneyRepositoryImplTest.kt`
- [ ] T077 [P] [US3] Integration test for JourneyDao in `app/src/androidTest/java/labs/claucookie/pasbuk/data/local/dao/JourneyDaoTest.kt` with in-memory database
- [ ] T078 [P] [US3] Integration test for many-to-many relationship (JourneyPassCrossRef) in `app/src/androidTest/java/labs/claucookie/pasbuk/data/local/dao/JourneyDaoTest.kt`
- [ ] T079 [P] [US3] UI test for JourneyListScreen in `app/src/androidTest/java/labs/claucookie/pasbuk/ui/screens/journey/JourneyListScreenTest.kt`
- [ ] T080 [P] [US3] UI test for JourneyDetailScreen in `app/src/androidTest/java/labs/claucookie/pasbuk/ui/screens/journey/JourneyDetailScreenTest.kt`

### Implementation for User Story 3

#### Repository Implementation

- [ ] T081 [US3] Implement `JourneyRepositoryImpl.kt` in `app/src/main/java/labs/claucookie/pasbuk/data/repository/` with createJourney(), getAllJourneys(), getJourneyById(), deleteJourney() methods
- [ ] T082 [US3] Update RepositoryModule.kt to bind JourneyRepository to JourneyRepositoryImpl

#### Use Cases

- [ ] T083 [P] [US3] Implement `CreateJourneyUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` with name validation (depends on T082)
- [ ] T084 [P] [US3] Implement `GetAllJourneysUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T082)
- [ ] T085 [P] [US3] Implement `GetJourneyDetailUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T082)
- [ ] T086 [P] [US3] Implement `DeleteJourneyUseCase.kt` in `app/src/main/java/labs/claucookie/pasbuk/domain/usecase/` (depends on T082)
- [ ] T087 [US3] Update UseCaseModule.kt to provide CreateJourneyUseCase, GetAllJourneysUseCase, GetJourneyDetailUseCase, DeleteJourneyUseCase

#### UI Components

- [ ] T088 [P] [US3] Create `JourneyCard.kt` composable in `app/src/main/java/labs/claucookie/pasbuk/ui/components/` displaying journey name and pass count

#### Journey List Screen

- [ ] T089 [US3] Create `JourneyListUiState.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/journey/` with Loading, Success(journeys), Empty states
- [ ] T090 [US3] Implement `JourneyListViewModel.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/journey/` with @HiltViewModel (depends on T087)
- [ ] T091 [US3] Implement `JourneyListScreen.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/journey/` with LazyColumn of JourneyCard items (depends on T088, T090)
- [ ] T092 [US3] Add JourneyList route to Navigation.kt
- [ ] T093 [US3] Add navigation to JourneyList from Timeline (e.g., bottom navigation or top app bar action)

#### Journey Detail Screen

- [ ] T094 [US3] Create `JourneyDetailUiState.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/journey/` with Loading, Success(journey with passes), Error states
- [ ] T095 [US3] Implement `JourneyDetailViewModel.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/journey/` with @HiltViewModel (depends on T087)
- [ ] T096 [US3] Implement `JourneyDetailScreen.kt` in `app/src/main/java/labs/claucookie/pasbuk/ui/screens/journey/` showing journey name and sorted pass list (depends on T095)
- [ ] T097 [US3] Add JourneyDetail route to Navigation.kt with journeyId parameter
- [ ] T098 [US3] Wire up JourneyCard click to navigate to JourneyDetailScreen

#### Create Journey Flow (Multi-Selection)

- [ ] T099 [US3] Add multi-selection mode to TimelineScreen with Checkbox overlays on PassCard items
- [ ] T100 [US3] Add "Create Journey" action button (FAB or top bar action) that appears when passes are selected
- [ ] T101 [US3] Create journey name input dialog composable in TimelineViewModel or separate dialog
- [ ] T102 [US3] Wire Create Journey button to call CreateJourneyUseCase with selected pass IDs and user-provided name
- [ ] T103 [US3] Navigate to JourneyDetailScreen after successful journey creation
- [ ] T104 [US3] Add success/error snackbar messages for journey creation

**Checkpoint**: All user stories should now be independently functional - import, timeline, and journeys all working

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and final quality gates

### Performance Optimization

- [ ] T105 [P] Add pagination to TimelineScreen for large pass lists (100+ passes)
- [ ] T106 [P] Optimize image loading with Coil memory/disk caching configuration
- [ ] T107 [P] Add database indices verification (already defined in schema, verify with Database Inspector)
- [ ] T108 [P] Profile app with Android Profiler to verify <2s timeline load and <10s import targets (SC-001, SC-002)

### Accessibility

- [x] T109 [P] Add content descriptions to all images and icons for TalkBack
- [ ] T110 [P] Verify color contrast ratios meet WCAG AA standards (Material3 should handle this)
- [ ] T111 [P] Test navigation with TalkBack enabled
- [ ] T112 [P] Add semantic properties to interactive elements (buttons, cards)

### Error Handling & Edge Cases

- [ ] T113 [P] Add global error handling for uncaught exceptions
- [ ] T114 [P] Add retry mechanism for failed imports
- [x] T115 [P] Add confirmation dialog for delete operations (pass and journey)
- [ ] T116 [P] Handle low storage scenarios gracefully

### Code Quality

- [x] T117 [P] Run lint checks and fix all errors: `./gradlew lint`
- [x] T118 [P] Verify test coverage meets 70%+ target: `./gradlew testDebugUnitTestCoverage`
- [ ] T119 [P] Code review for Kotlin conventions adherence
- [ ] T120 [P] Add KDoc comments to public APIs

### Documentation

- [x] T121 [P] Update README.md with feature overview and screenshots
- [ ] T122 [P] Create sample .pkpass files for testing in `app/src/androidTest/assets/`
- [ ] T123 [P] Document Room database schema export

### Final Validation

- [ ] T124 Run full test suite: `./gradlew test connectedAndroidTest`
- [ ] T125 Verify all acceptance scenarios from spec.md manually
- [ ] T126 Run quickstart.md verification checklist
- [ ] T127 Performance validation: Import pass <10s, Timeline load <2s with 100 passes
- [ ] T128 Create demo video or screenshots for PR

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phases 3-5)**: All depend on Foundational phase completion
  - User Story 1 (P1): Can start after Phase 2 - No dependencies on other stories
  - User Story 2 (P2): Can start after Phase 2 - Integrates with US1 but independently testable
  - User Story 3 (P3): Can start after Phase 2 - Integrates with US1/US2 but independently testable
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Import and view pass - Foundation for all other stories
- **User Story 2 (P2)**: Timeline view - Uses passes from US1, but can be tested independently by creating test passes
- **User Story 3 (P3)**: Create journeys - Uses passes from US1 and timeline from US2, but independently testable

### Within Each User Story

For TDD approach:
1. Write tests FIRST (marked [P] can run in parallel)
2. Ensure tests FAIL
3. Implement models (can be parallel)
4. Implement repositories/use cases (sequential, depend on models)
5. Implement ViewModels (depend on use cases)
6. Implement UI screens (depend on ViewModels)
7. Verify tests PASS

### Parallel Opportunities

**Phase 1 (Setup)**: T003, T004, T005 can run in parallel after T002 completes

**Phase 2 (Foundational)**:
- T007-T012 (domain models) can all run in parallel
- T013-T015 (repository interfaces) can run in parallel
- T016-T022 (data layer entities and DAOs) can all run in parallel
- T024-T025 (mappers) can run in parallel
- T026-T029 (DI modules) can run in parallel
- T030-T031 (navigation) can run in parallel

**Phase 3 (User Story 1)**:
- All tests T032-T038 can run in parallel (TDD)
- T047-T048 (UI components) can run in parallel
- T043-T045 (use cases) can run in parallel after T042

**Phase 4 (User Story 2)**:
- Tests T059-T062 can run in parallel

**Phase 5 (User Story 3)**:
- Tests T073-T080 can run in parallel
- Use cases T083-T086 can run in parallel after T082

**Phase 6 (Polish)**:
- Most tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1

```bash
# After writing tests (T032-T038), launch implementation in parallel:
Task T039: "Create PassJson.kt data classes"
Task T047: "Create BarcodeDisplay.kt composable"
Task T048: "Create PassCard.kt composable"

# After T042 completes, launch use cases in parallel:
Task T043: "Implement ImportPassUseCase.kt"
Task T044: "Implement GetPassDetailUseCase.kt"
Task T045: "Implement DeletePassUseCase.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T006)
2. Complete Phase 2: Foundational (T007-T031) - CRITICAL PATH
3. Complete Phase 3: User Story 1 (T032-T058)
4. **STOP and VALIDATE**: Import a .pkpass file and verify pass detail screen works
5. Deploy/demo MVP if ready

**Estimated MVP**: ~58 tasks focusing on core import and view functionality

### Incremental Delivery

1. **Foundation** (Phase 1+2): Setup + Foundational → ~31 tasks
2. **MVP** (Phase 3): Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. **Timeline** (Phase 4): Add User Story 2 → Timeline view working → Deploy/Demo
4. **Journeys** (Phase 5): Add User Story 3 → Full feature set → Deploy/Demo
5. **Polish** (Phase 6): Performance, accessibility, final validation

### Parallel Team Strategy

With multiple developers:

1. **Together**: Complete Setup + Foundational (Phase 1+2)
2. **Once Foundational done** (parallel tracks):
   - Developer A: User Story 1 (import/view pass)
   - Developer B: User Story 2 (timeline) - Can use test fixtures initially
   - Developer C: User Story 3 (journeys) - Can use test fixtures initially
3. Stories integrate and test together after individual completion

---

## Task Summary

**Total Tasks**: 128
- **Phase 1 (Setup)**: 6 tasks
- **Phase 2 (Foundational)**: 25 tasks (BLOCKS everything)
- **Phase 3 (User Story 1)**: 27 tasks (7 tests + 20 implementation)
- **Phase 4 (User Story 2)**: 14 tasks (4 tests + 10 implementation)
- **Phase 5 (User Story 3)**: 32 tasks (8 tests + 24 implementation)
- **Phase 6 (Polish)**: 24 tasks

**Test Tasks**: 19 unit tests, integration tests, and UI tests (meeting constitution requirement for comprehensive testing)

**Parallel Opportunities**: ~60 tasks marked [P] can run in parallel within their phases

**MVP Scope**: Phases 1-3 only (58 tasks) delivers import and view functionality

**Independent Test Criteria Met**:
- US1: Can import and view a pass independently
- US2: Can view timeline independently (with test passes)
- US3: Can create and view journeys independently (with test passes)

---

## Notes

- All tasks follow strict checklist format: `- [ ] [ID] [P?] [Story?] Description with file path`
- [P] = Parallelizable (different files, no dependencies)
- [Story] = US1, US2, US3 labels for traceability
- Each user story is independently completable and testable
- TDD approach: Write tests first, ensure they fail, then implement
- Stop at any checkpoint to validate story works independently
- Commit after each task or logical group for easy rollback
