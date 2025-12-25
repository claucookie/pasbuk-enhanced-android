# TalkBack Testing Guide

**Task**: T111 - Test navigation with TalkBack enabled
**Date**: 2025-12-25
**Status**: Ready for manual testing

## Overview

TalkBack is Android's built-in screen reader for users with visual impairments. This guide provides comprehensive testing procedures to ensure the app is fully accessible with TalkBack enabled.

---

## Prerequisites

### 1. Enable TalkBack

**Method 1: Settings**
```
Settings → Accessibility → TalkBack → Toggle ON
```

**Method 2: Volume Key Shortcut** (Android 8+)
```
Press and hold both volume keys for 3 seconds
```

**Method 3: ADB Command**
```bash
adb shell settings put secure enabled_accessibility_services \
  com.google.android.marvin.talkback/.TalkBackService
```

### 2. TalkBack Gestures

| Gesture | Action |
|---------|--------|
| **Tap** | Focus element |
| **Double-tap** | Activate focused element |
| **Swipe right** | Next element |
| **Swipe left** | Previous element |
| **Swipe down then right** | Read from top |
| **Swipe up then down** | Reading controls menu |
| **Two-finger swipe down** | Scroll down |
| **Two-finger swipe up** | Scroll up |

---

## Testing Checklist

### Screen 1: Timeline Screen

#### Layout & Structure
- [ ] **Top App Bar**
  - [ ] "Pasbuk" title is announced
  - [ ] "Journeys" button announces function and state
  - [ ] In selection mode: "Clear selection" button is announced
  - [ ] In selection mode: "Create journey" button is announced
  - [ ] Selected count is announced (e.g., "3 selected")

#### Pass Cards
- [ ] **Each pass card announces**:
  - [ ] Card is identified as button/clickable
  - [ ] Organization name is read
  - [ ] Pass description/title is read
  - [ ] Relevant date is read (if present)
  - [ ] Selection state: "Selected" or "Not selected"

- [ ] **Focus order**:
  - [ ] Cards read in chronological order (newest first)
  - [ ] No focus traps

- [ ] **Actions**:
  - [ ] Double-tap activates card (navigates to detail)
  - [ ] Long-press announced as "Long press to select"
  - [ ] In selection mode: double-tap toggles selection

#### Empty State
- [ ] When no passes:
  - [ ] Empty state icon has content description: "Empty state: No passes"
  - [ ] "No passes yet" heading is announced
  - [ ] Instructions text is read: "Tap the Import button..."

#### Floating Action Button (FAB)
- [ ] FAB announces: "Import pass" (when idle)
- [ ] FAB announces: "Importing..." (during import)
- [ ] FAB is accessible via swipe navigation
- [ ] Double-tap activates file picker

#### Loading States
- [ ] Progress indicator announces: "Loading" or "Loading passes"
- [ ] Loading overlay is announced during import

---

### Screen 2: Pass Detail Screen

#### Top App Bar
- [ ] Back button announces: "Navigate up" or "Back"
- [ ] Delete button announces: "Delete pass"
- [ ] Title shows pass description

#### Pass Content
- [ ] **Organization name** is announced
- [ ] **Description/title** is announced clearly
- [ ] **Logo image** has description: "Pass logo" (from T109)
- [ ] **Relevant date** is announced with full format

#### Barcode Section
- [ ] Barcode image has description (e.g., "QR Code", "PDF417")
- [ ] Barcode alt code is announced if available

#### Pass Fields
- [ ] Each field label and value announced separately
- [ ] Fields in logical reading order (header → primary → secondary → auxiliary → back)

#### Delete Confirmation Dialog
- [ ] Dialog title announced: "Delete Pass"
- [ ] Warning message is read
- [ ] "Cancel" button identified and functional
- [ ] "Delete" button identified and functional
- [ ] Focus returns to previous screen after cancel

---

### Screen 3: Journey List Screen

#### Top App Bar
- [ ] "Journeys" title is announced
- [ ] Back/navigation button works with TalkBack

#### Journey Cards
- [ ] Each journey card announces:
  - [ ] Journey name
  - [ ] Pass count (e.g., "5 passes")
  - [ ] Card identified as clickable/button

- [ ] Cards in logical order
- [ ] Double-tap navigates to journey detail

#### Empty State
- [ ] When no journeys:
  - [ ] Empty state message is announced
  - [ ] Instructions provided

---

### Screen 4: Journey Detail Screen

#### Top App Bar
- [ ] Journey name as title is announced
- [ ] Back button works
- [ ] Delete button announces "Delete journey"

#### Pass List
- [ ] Passes announced in chronological order
- [ ] Each pass card has all information read
- [ ] Focus flows naturally through list

#### Delete Confirmation
- [ ] "Delete Journey" dialog title announced
- [ ] Confirmation message read
- [ ] Button labels clear

---

### Screen 5: Import Flow

#### File Picker
- [ ] File picker screen is navigable
- [ ] Selected file name is announced
- [ ] Confirm/cancel buttons work

#### Progress & Feedback
- [ ] "Importing..." status announced
- [ ] Success: "Pass imported successfully" announced
- [ ] Error messages are clear and announced
- [ ] Duplicate error: "This pass has already been imported"
- [ ] Invalid file: "Invalid or corrupted pass file"

---

### Screen 6: Create Journey Flow

#### Selection Mode
- [ ] Entering selection mode announced
- [ ] Number of selected passes updated as user selects
- [ ] "Create Journey" button becomes available

#### Journey Name Dialog
- [ ] Dialog title announced: "Create Journey" or similar
- [ ] Text field label announced: "Journey name"
- [ ] Current text value read when focused
- [ ] Keyboard input is announced
- [ ] "Cancel" button works
- [ ] "Create" button works
- [ ] Error announced if name exists: "A journey with this name already exists"

#### Success Feedback
- [ ] "Journey created successfully" announced
- [ ] Navigates to journey detail automatically
- [ ] Focus set to meaningful element

---

## Accessibility Features Verification

### Content Descriptions (T109)

Verify these elements have proper content descriptions:

- [x] **Icons**:
  - [x] Import FAB icon: "Import pass"
  - [x] Journeys icon: "Journeys"
  - [x] Delete icon: "Delete"
  - [x] Back arrow: "Navigate up"
  - [x] Clear selection: "Clear selection"
  - [x] Create journey: "Create journey"

- [x] **Images**:
  - [x] Pass logos: "Pass logo"
  - [x] Barcodes: Barcode type (e.g., "QR Code")
  - [x] Empty state icon: "Empty state: No passes"

### Semantic Properties (T112)

- [x] **PassCard**: Has role = Role.Button
- [x] **PassCard**: State description indicates selection state
- [x] **Interactive elements**: All marked with proper semantics

### Labels & Hints

- [ ] All buttons have clear labels (not just icons)
- [ ] Text fields have labels and hints
- [ ] Error messages are associated with fields
- [ ] Success messages are announced

---

## Common Issues to Check

### Focus Issues
- [ ] **Focus order is logical** (top-to-bottom, left-to-right)
- [ ] **No focus traps** (user can navigate away from all elements)
- [ ] **Focus visible** after actions (e.g., after deleting, focus moves to valid element)
- [ ] **No orphaned focus** (focus on elements that no longer exist)

### Announcement Issues
- [ ] **No silent elements** (all interactive elements announce something)
- [ ] **No redundant announcements** (e.g., "button button")
- [ ] **Clear action labels** (not "click here" or "tap")
- [ ] **State changes announced** (loading, success, errors)

### Navigation Issues
- [ ] **Can reach all elements** via swipe gestures
- [ ] **Can activate all buttons** with double-tap
- [ ] **Dialogs can be dismissed** with TalkBack
- [ ] **Back navigation works** consistently

### Content Issues
- [ ] **Decorative images hidden** from TalkBack (contentDescription = null or "")
- [ ] **Meaningful images described** properly
- [ ] **Text truncation avoided** or handled gracefully
- [ ] **Dynamic content changes announced**

---

## Testing Scenarios

### Scenario 1: First-Time User Flow

1. Launch app with TalkBack enabled
2. Navigate through empty state
3. Activate "Import pass" button
4. Select a pass file
5. Wait for import to complete
6. Verify success announcement
7. Navigate pass detail screen
8. Return to timeline

**Expected**: All steps are clear, logical, and accessible.

### Scenario 2: Creating a Journey

1. Import 3-5 passes
2. Return to timeline
3. Long-press a pass card to enter selection mode
4. Select multiple passes
5. Activate "Create journey" button
6. Enter journey name in dialog
7. Confirm creation
8. Verify navigation to journey detail

**Expected**: All interactions work smoothly with TalkBack.

### Scenario 3: Managing Content

1. Navigate to Journeys screen
2. Select a journey
3. Review journey contents
4. Delete a journey (with confirmation)
5. Navigate back to timeline
6. Delete a pass (with confirmation)

**Expected**: All delete flows have clear confirmations and announcements.

### Scenario 4: Error Handling

1. Try importing an invalid file
2. Try creating journey with duplicate name
3. Try importing duplicate pass

**Expected**: Error messages are clear and announced immediately.

---

## Automated Accessibility Testing

### Espresso Accessibility Tests (Optional)

Add to androidTest:

```kotlin
@Test
fun testTimelineScreenAccessibility() {
    // Enable AccessibilityChecks
    AccessibilityChecks.enable()
        .setRunChecksFromRootView(true)

    // Navigate to timeline
    onView(withId(R.id.timeline_screen))
        .check(matches(isDisplayed()))
        .check(AccessibilityChecks())
}
```

### Android Accessibility Scanner

1. Install from Google Play Store
2. Enable scanner
3. Navigate through all screens
4. Review flagged issues

---

## Success Criteria

TalkBack testing is **PASSED** if:

- ✅ All screens can be navigated completely using TalkBack gestures
- ✅ All interactive elements can be activated via double-tap
- ✅ All important content is announced (text, state, errors)
- ✅ All images have appropriate content descriptions (T109)
- ✅ All interactive elements have semantic roles (T112)
- ✅ Focus order is logical on all screens
- ✅ No focus traps exist
- ✅ Error messages are clearly announced
- ✅ Success feedback is provided
- ✅ Dialogs can be dismissed and navigated
- ✅ No confusing or redundant announcements

---

## Known Good Implementations

Based on code review, these features are already TalkBack-ready:

### ✅ Content Descriptions Added (T109)
- Empty state icons
- Navigation icons (back, journeys, delete)
- FAB icon

### ✅ Semantic Properties Added (T112)
- PassCard has Role.Button
- PassCard has stateDescription for selection state

### ✅ Material 3 Components
- TopAppBar: Automatically accessible
- Buttons: Built-in accessibility support
- Text fields: Proper labels and hints
- Dialogs: Focus management included
- LazyColumn: Scroll announcements automatic

---

## Issues to Address (If Found)

### If focus order is incorrect:
```kotlin
// Use traversalIndex or reorder composables
Modifier.semantics { traversalIndex = 1f }
```

### If element not focusable:
```kotlin
// Add semantic properties
Modifier.semantics {
    contentDescription = "Description"
    role = Role.Button
}
```

### If state not announced:
```kotlin
// Add state description
Modifier.semantics {
    stateDescription = if (isSelected) "Selected" else "Not selected"
}
```

### If decorative image announced:
```kotlin
// Hide from TalkBack
Icon(
    ...,
    contentDescription = null  // Decorative only
)
```

---

## Resources

### Documentation
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)
- [TalkBack Gestures](https://support.google.com/accessibility/android/answer/6151827)

### Tools
- [Accessibility Scanner](https://play.google.com/store/apps/details?id=com.google.android.apps.accessibility.auditor)
- [Accessibility Test Framework](https://github.com/google/Accessibility-Test-Framework-for-Android)

### Testing Guides
- [TalkBack Testing Checklist](https://developer.android.com/guide/topics/ui/accessibility/testing)
- [Compose Accessibility Testing](https://developer.android.com/jetpack/compose/accessibility/testing)

---

## Next Steps

1. **Manual Testing**: Follow all test scenarios with TalkBack enabled
2. **Document Findings**: Note any issues discovered
3. **Fix Issues**: Address accessibility problems found
4. **Retest**: Verify fixes work with TalkBack
5. **User Testing**: Ideally test with actual TalkBack users

**Status**: 🔶 Requires manual testing with TalkBack enabled

**Estimated Time**: 30-45 minutes for complete testing

---

## Testing Report Template

After testing, document results:

```markdown
## TalkBack Testing Results

**Date**: YYYY-MM-DD
**Device**: [Model, API Level]
**TalkBack Version**: X.X.X

### Screens Tested
- [ ] Timeline Screen
- [ ] Pass Detail Screen
- [ ] Journey List Screen
- [ ] Journey Detail Screen
- [ ] Import Flow
- [ ] Create Journey Flow

### Issues Found
1. [Issue description]
   - Severity: High / Medium / Low
   - Steps to reproduce
   - Expected behavior
   - Actual behavior

### Pass/Fail Summary
- Total elements tested: XX
- Accessible: XX
- Issues found: XX
- Pass rate: XX%

### Overall Result
✅ PASS / ❌ FAIL

### Recommendations
- [List of improvements]
```
