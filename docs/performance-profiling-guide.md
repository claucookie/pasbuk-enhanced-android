# Performance Profiling Guide

**Task**: T108 - Android Profiler verification for performance targets
**Date**: 2025-12-25
**Status**: Ready for manual testing

## Performance Targets (from spec.md)

| ID | Success Criteria | Target | Priority |
|----|------------------|--------|----------|
| SC-001 | Import and view pass | < 10 seconds | P1 |
| SC-002 | Timeline load (100 passes) | < 2 seconds | P2 |
| - | UI Performance | 60 FPS, smooth scrolling | High |

---

## Prerequisites

1. **Test Data Preparation**:
   - Create or obtain 100+ sample .pkpass files for realistic testing
   - Store files in accessible location (Downloads, Google Drive, etc.)

2. **Device Requirements**:
   - Physical device or emulator (API 28+)
   - Debug build of the app installed
   - USB debugging enabled

3. **Android Studio Setup**:
   - Android Studio Arctic Fox or later
   - Profiler window accessible (View → Tool Windows → Profiler)

---

## Profiling Tasks

### 1. Timeline Load Performance (SC-002: < 2 seconds)

**Objective**: Verify timeline loads 100 passes in under 2 seconds

#### Setup
1. Import 100+ passes into the app (can be done incrementally)
2. Force close the app to clear memory
3. Open Android Studio Profiler
4. Select the app process

#### Test Procedure

**Method 1: CPU Profiler**
1. Start CPU recording (Sample Java Methods)
2. Launch the app → Timeline screen loads
3. Stop recording when timeline is fully rendered
4. Analyze results:

**What to measure**:
```
Total Timeline Load Time = App Launch → First Pass Visible
- Room query execution time (database read)
- Data mapping time (Entity → Domain)
- Compose recomposition time
- LazyColumn rendering time
```

**Expected Results**:
- ✅ **Total time < 2 seconds** for 100 passes
- Database query: < 500ms (indexed query on relevantDate)
- Data transformation: < 300ms (entity mapping)
- UI rendering: < 700ms (LazyColumn composition)
- Remaining: < 500ms (system overhead)

**Method 2: Frame Profiler**
1. Navigate to Timeline screen with 100+ passes
2. Observe frame rendering timeline
3. Scroll through the list

**Expected Results**:
- ✅ All frames render in < 16ms (60 FPS)
- ✅ No jank during scrolling
- ✅ Pagination loading is smooth (no UI freezing)

#### Optimization Points if Target Not Met

If timeline load > 2 seconds:
- ❌ **Database**: Check if indices are used (use Database Inspector)
- ❌ **Paging**: Verify initial load size (currently 40 items)
- ❌ **Images**: Ensure Coil cache is working (check disk cache hits)
- ❌ **Compose**: Look for unnecessary recompositions

---

### 2. Import Pass Performance (SC-001: < 10 seconds)

**Objective**: Verify pass import completes in under 10 seconds

#### Test Procedure

1. Start CPU profiler
2. Use file picker to select a .pkpass file
3. Wait for import to complete (navigate to detail screen)
4. Stop profiler
5. Measure total time

**What to measure**:
```
Import Time = File Selection → Detail Screen Displayed
- ZIP extraction time
- JSON parsing time (Moshi)
- Image file copying time
- Database insert time
- Navigation time
```

**Expected Results**:
- ✅ **Total time < 10 seconds** per pass
  - ZIP extraction: < 2s
  - JSON parsing: < 1s
  - Image processing: < 3s
  - Database operations: < 500ms
  - UI navigation: < 500ms
  - Remaining: < 3s buffer

#### Optimization Points if Target Not Met

If import > 10 seconds:
- ❌ **File I/O**: Use `Dispatchers.IO` (already implemented)
- ❌ **Images**: Check if large images need resizing
- ❌ **Parser**: Profile PkpassParser for bottlenecks
- ❌ **Database**: Ensure transaction batching if needed

---

### 3. Memory Profiler

**Objective**: Ensure no memory leaks and reasonable memory usage

#### Test Procedure

1. Open Memory Profiler
2. Perform these actions:
   - Import 10 passes
   - Navigate timeline → detail → back (repeat 10x)
   - Create journey with 20 passes
   - Delete passes/journeys
3. Force GC (garbage collector icon)
4. Check heap dump

**Expected Results**:
- ✅ Memory returns to baseline after GC
- ✅ No retained ViewModel/Activity instances
- ✅ Coil image cache stays within configured limits:
  - Memory cache: ≤ 25% of app memory
  - Disk cache: ≤ 50MB
- ✅ No bitmap leaks from image loading

#### Red Flags
- ❌ Memory steadily increasing without GC recovery
- ❌ Multiple ViewModel instances retained
- ❌ Large bitmap allocations not released

---

### 4. Network Profiler

**Objective**: Verify app works offline (no network calls)

#### Test Procedure

1. Open Network Profiler
2. Use the app normally:
   - Import passes
   - View timeline
   - Create journeys
3. Monitor network activity

**Expected Results**:
- ✅ **Zero network requests** during normal operation
- ✅ App fully functional in airplane mode
- ✅ All data stored locally

---

## Performance Optimization Checklist

Based on current implementation:

### ✅ Already Optimized

- [x] **Pagination**: Jetpack Paging 3 implemented (T105)
  - Page size: 20 items
  - Initial load: 40 items
  - Prevents loading all passes into memory

- [x] **Image Caching**: Coil optimized (T106)
  - Memory cache: 25% of app memory
  - Disk cache: 50MB
  - Prevents redundant image loading

- [x] **Database Indices**: Properly indexed (T107)
  - `serialNumber` (unique): Fast duplicate checks
  - `relevantDate`: Efficient ORDER BY
  - Foreign keys indexed: Fast JOINs

- [x] **Async Operations**: Using Coroutines
  - Import: `Dispatchers.IO`
  - Database: Suspend functions
  - UI: `viewModelScope`

- [x] **Compose Best Practices**:
  - LazyColumn for efficient list rendering
  - Item keys for stable recomposition
  - State hoisting properly implemented

### 🔍 Areas to Monitor

During profiling, pay special attention to:

1. **First pass import**: May be slower due to cold cache
2. **Large pass files**: Files with many images or large images
3. **Low-end devices**: Test on API 28 device if possible
4. **Compose recomposition**: Use Layout Inspector to verify

---

## Profiling Report Template

After completing profiling, document results:

```markdown
## Performance Profiling Results

**Date**: YYYY-MM-DD
**Device**: [Device Model, API Level]
**Build**: Debug / Release
**Test Data**: [Number of passes used]

### SC-001: Import Pass Performance
- ✅/❌ Target: < 10 seconds
- Measured: X.XX seconds
- Breakdown:
  - ZIP extraction: X.XXs
  - JSON parsing: X.XXs
  - Image processing: X.XXs
  - Database insert: X.XXs

### SC-002: Timeline Load Performance
- ✅/❌ Target: < 2 seconds (100 passes)
- Measured: X.XX seconds
- Database query: X.XXms
- Data mapping: X.XXms
- UI rendering: X.XXms

### Frame Rate
- ✅/❌ Target: 60 FPS
- Average: XX FPS
- Jank frames: X%

### Memory Usage
- Baseline: XXX MB
- Peak: XXX MB
- Image cache: XX MB
- Leaks: None / [Description]

### Issues Found
1. [Issue description]
   - Impact: [Performance impact]
   - Solution: [Proposed fix]
```

---

## Running Performance Tests

### Quick Performance Test (5 minutes)

```bash
# 1. Build and install debug APK
./gradlew installDebug

# 2. Launch app with profiling
# In Android Studio: Run → Profile 'app'

# 3. Execute test scenarios:
# - Import 3 passes (measure time)
# - View timeline (check frame rate)
# - Create journey (measure time)

# 4. Check results in Profiler
```

### Comprehensive Performance Test (30 minutes)

1. **Prepare**: Import 100+ passes (one-time setup)
2. **Timeline Load**: Force close → Launch → Measure load time
3. **Import Test**: Import 5 new passes, measure each
4. **Scrolling Test**: Scroll timeline top to bottom, check frames
5. **Memory Test**: Navigate app, check for leaks
6. **Journey Creation**: Create journey with 50 passes, measure time

---

## Success Criteria

Performance profiling is **PASSED** if:

- ✅ SC-001: Import < 10 seconds (average of 5 imports)
- ✅ SC-002: Timeline < 2 seconds (with 100+ passes)
- ✅ 60 FPS maintained during scrolling (< 5% jank)
- ✅ No memory leaks detected
- ✅ Zero network calls during normal operation

**Status**: 🔶 Requires manual testing with Android Profiler

---

## Notes

- Performance may vary by device (target: mid-range devices, API 28+)
- Release builds are typically 30-50% faster than debug builds
- Cold starts are slower than warm starts (test both)
- Profiler overhead adds ~5-10% to measured times

**Next Steps**: Run profiling tests and document results using the template above.
