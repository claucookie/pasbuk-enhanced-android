# Demo Screenshots & Video Guide

**Feature**: Passbook Timeline and Journeys
**Purpose**: PR documentation and user showcase
**Date**: 2025-12-25

## Overview

This directory contains screenshots and demo videos for the Passbook Timeline and Journeys feature. These assets are used in:
- Pull Request descriptions
- README.md feature showcase
- GitHub release notes
- User documentation

---

## Required Screenshots

### 1. Timeline Screen (Empty State)
**Filename**: `01_timeline_empty.png`

**Description**: Timeline screen when no passes have been imported

**How to Capture**:
1. Clear app data (Settings → Apps → Pasbuk → Clear data)
2. Launch app
3. Screenshot the empty timeline with message

**Expected Content**:
- Empty state illustration or message
- "Import Pass" FAB button
- Bottom navigation bar
- App title in top bar

---

### 2. Import Flow - File Picker
**Filename**: `02_import_file_picker.png`

**Description**: System file picker showing .pkpass file selection

**How to Capture**:
1. Tap the "Import Pass" FAB button
2. Screenshot the file picker dialog
3. Show .pkpass files in the picker

**Expected Content**:
- System file picker UI
- .pkpass files listed
- File selection interface

---

### 3. Pass Detail Screen (Boarding Pass)
**Filename**: `03_pass_detail_boarding.png`

**Description**: Detailed view of an imported boarding pass

**How to Capture**:
1. Import a boarding pass .pkpass file
2. View the pass detail screen
3. Screenshot showing full pass details

**Expected Content**:
- Pass header with logo/icon
- Organization name
- Pass description/event name
- Relevant date and time
- Primary/secondary/auxiliary fields
- Barcode (QR code or other format)
- Background/foreground colors applied
- Delete button in top bar

**Sample Data**:
- Airline: Example Airlines
- Flight: EX 123
- From: SFO → LAX
- Date: 2025-12-26 14:30
- Barcode: QR code

---

### 4. Pass Detail Screen (Event Ticket)
**Filename**: `04_pass_detail_event.png`

**Description**: Detailed view of an event ticket pass

**How to Capture**:
1. Import an event ticket .pkpass file
2. View the pass detail screen
3. Screenshot showing event details

**Expected Content**:
- Event name and venue
- Event date and time
- Ticket barcode
- Seat information (if available)
- Different visual styling from boarding pass

---

### 5. Timeline Screen (Populated)
**Filename**: `05_timeline_populated.png`

**Description**: Timeline showing multiple imported passes sorted by date

**How to Capture**:
1. Import 5-10 passes with different dates
2. View timeline screen
3. Screenshot showing chronological list

**Expected Content**:
- Multiple pass cards in vertical list
- Each card showing:
  - Pass icon/logo
  - Organization name
  - Pass description
  - Relevant date
  - Visual preview
- Passes sorted by date (newest first)
- "Import Pass" FAB visible
- Bottom navigation bar

---

### 6. Timeline - Multi-Selection Mode
**Filename**: `06_timeline_selection.png`

**Description**: Timeline with passes selected for journey creation

**How to Capture**:
1. Long-press on a pass card to enter selection mode
2. Select 2-3 passes by tapping checkboxes
3. Screenshot showing selection UI

**Expected Content**:
- Checkbox overlays on pass cards
- 2-3 passes selected (checkboxes checked)
- Selection count indicator
- "Create Journey" FAB or action button
- Exit selection mode button

---

### 7. Create Journey Dialog
**Filename**: `07_journey_create_dialog.png`

**Description**: Dialog for entering journey name

**How to Capture**:
1. Select multiple passes
2. Tap "Create Journey" button
3. Screenshot the name input dialog

**Expected Content**:
- Dialog title: "Create Journey"
- Text input field for journey name
- Placeholder text: "Enter journey name"
- "Cancel" and "Create" buttons
- Selected pass count visible in background

---

### 8. Journey List Screen (Empty)
**Filename**: `08_journey_list_empty.png`

**Description**: Journeys tab with no journeys created

**How to Capture**:
1. Clear app data or ensure no journeys exist
2. Navigate to Journeys tab (bottom navigation)
3. Screenshot empty state

**Expected Content**:
- Empty state message: "No journeys yet"
- Instructions to create journey from timeline
- Bottom navigation with "Journeys" tab selected

---

### 9. Journey List Screen (Populated)
**Filename**: `09_journey_list_populated.png`

**Description**: Journeys tab showing created journeys

**How to Capture**:
1. Create 2-3 journeys with different names and pass counts
2. Navigate to Journeys tab
3. Screenshot journey list

**Expected Content**:
- Multiple journey cards
- Each card showing:
  - Journey name
  - Pass count (e.g., "5 passes")
  - Creation date or icon
- Journeys sorted by creation date

**Sample Journeys**:
- "Summer Vacation 2025" - 8 passes
- "Business Trip NYC" - 3 passes
- "Concert Tour" - 5 passes

---

### 10. Journey Detail Screen
**Filename**: `10_journey_detail.png`

**Description**: Detailed view of a journey with its passes

**How to Capture**:
1. Tap on a journey from the journey list
2. Screenshot showing journey details

**Expected Content**:
- Journey name in top bar
- List of passes in the journey
- Passes sorted chronologically
- Each pass card showing icon, name, date
- Delete journey button in top bar
- Pass count indicator

---

### 11. Delete Confirmation Dialog
**Filename**: `11_delete_confirmation.png`

**Description**: Confirmation dialog for deleting a pass or journey

**How to Capture**:
1. Tap delete button on a pass or journey
2. Screenshot the confirmation dialog

**Expected Content**:
- Dialog title: "Delete Pass?" or "Delete Journey?"
- Warning message
- "Cancel" and "Delete" buttons
- Destructive action styling (red for delete)

---

### 12. Error State - Invalid File
**Filename**: `12_error_invalid_file.png`

**Description**: Error snackbar when importing invalid file

**How to Capture**:
1. Try to import a non-.pkpass file
2. Screenshot the error message

**Expected Content**:
- Snackbar at bottom with error message
- Error message: "Invalid pass file. Please select a .pkpass file."
- Snackbar dismiss action

---

### 13. Error State - Duplicate Pass
**Filename**: `13_error_duplicate.png`

**Description**: Error snackbar when importing duplicate pass

**How to Capture**:
1. Import the same .pkpass file twice
2. Screenshot the error message

**Expected Content**:
- Snackbar: "This pass has already been imported"
- Timeline unchanged (no duplicate)

---

### 14. Accessibility - TalkBack Mode
**Filename**: `14_accessibility_talkback.png`

**Description**: Screenshot showing TalkBack focus on an element

**How to Capture**:
1. Enable TalkBack in Android settings
2. Navigate through the app
3. Screenshot showing TalkBack focus rectangle

**Expected Content**:
- Green TalkBack focus rectangle around element
- TalkBack announcement visible (if possible)
- Shows accessibility support

---

### 15. Dark Mode (Optional)
**Filename**: `15_timeline_dark_mode.png`

**Description**: Timeline screen in dark theme

**How to Capture**:
1. Enable dark mode in Android settings
2. View timeline screen
3. Screenshot showing dark theme

**Expected Content**:
- Dark background
- Light text
- Material3 dark color scheme
- Pass cards with dark styling

---

## Video Demo

### Required Demo Video
**Filename**: `demo_full_flow.mp4`
**Duration**: 60-90 seconds
**Format**: MP4, 1080p, 30fps

**Script**:

1. **[0:00-0:05]** App launch → Timeline empty state
2. **[0:05-0:15]** Tap Import → File picker → Select .pkpass file
3. **[0:15-0:25]** Pass detail screen displays with barcode
4. **[0:25-0:30]** Navigate back to timeline → Pass appears
5. **[0:30-0:35]** Import 2 more passes (fast forward)
6. **[0:35-0:40]** Timeline shows 3 passes sorted by date
7. **[0:40-0:45]** Long-press → Multi-selection → Select 2 passes
8. **[0:45-0:50]** Tap "Create Journey" → Enter name "Summer Trip"
9. **[0:50-0:55]** Navigate to Journeys tab → Journey appears
10. **[0:55-0:65]** Tap journey → View journey detail with passes
11. **[0:65-0:70]** Tap a pass → Navigate to pass detail
12. **[0:70-0:75]** Return to timeline
13. **[0:75-0:80]** Show smooth scrolling
14. **[0:80-0:90]** End card with feature summary

**Recording Tools**:
- Android Studio built-in screen recorder
- ADB screen record: `adb shell screenrecord /sdcard/demo.mp4`
- Android device built-in screen recorder

**Post-Processing**:
- Trim to 60-90 seconds
- Add text overlays for key features (optional)
- Add smooth transitions
- Compress to <50MB for GitHub

---

## Screenshot Capture Guide

### Device Configuration

**Recommended Device**:
- Pixel 4a or similar (standard size)
- Resolution: 1080 x 2340 (19.5:9 aspect ratio)
- Android 10+ (for consistent UI)

**Settings**:
- Remove notification icons from status bar (if possible)
- Set device time to consistent value (e.g., 10:00 AM)
- Fully charge device (100% battery icon)
- Disable animations for faster capture (optional)

### Capture Methods

#### Method 1: Android Studio Screenshot
1. Run app on emulator
2. Camera icon in emulator toolbar
3. Screenshot saved to Downloads/

#### Method 2: ADB Screenshot
```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

#### Method 3: Device Screenshot
- Power + Volume Down (most devices)
- Screenshot saved to Photos/Screenshots

### Image Specifications

- **Format**: PNG (lossless)
- **Resolution**: Native device resolution (1080 x 2340)
- **Size**: <500KB per image (compress if needed)
- **Naming**: Sequential with descriptive names
- **Alt Text**: Provide descriptive alt text for accessibility

### Post-Processing (Optional)

**Tools**:
- Remove status bar (for cleaner look)
- Add device frame (Pixel 4a frame)
- Add drop shadow
- Create side-by-side comparisons

**Online Tools**:
- [Screely](https://www.screely.com/) - Add frames and backgrounds
- [Mockuphone](https://mockuphone.com/) - Device mockups

**Don't**:
- Don't alter screenshot content
- Don't add fake data
- Don't use old screenshots (keep updated)

---

## Screenshot Checklist

Before submitting PR, ensure you have:

- [ ] **01_timeline_empty.png** - Empty timeline
- [ ] **02_import_file_picker.png** - File picker
- [ ] **03_pass_detail_boarding.png** - Boarding pass detail
- [ ] **04_pass_detail_event.png** - Event ticket detail
- [ ] **05_timeline_populated.png** - Timeline with passes
- [ ] **06_timeline_selection.png** - Multi-selection mode
- [ ] **07_journey_create_dialog.png** - Create journey dialog
- [ ] **08_journey_list_empty.png** - Empty journey list
- [ ] **09_journey_list_populated.png** - Journey list with items
- [ ] **10_journey_detail.png** - Journey detail screen
- [ ] **11_delete_confirmation.png** - Delete confirmation
- [ ] **12_error_invalid_file.png** - Error: invalid file
- [ ] **13_error_duplicate.png** - Error: duplicate pass
- [ ] **14_accessibility_talkback.png** - TalkBack mode (optional)
- [ ] **15_timeline_dark_mode.png** - Dark theme (optional)

### Video Checklist

- [ ] **demo_full_flow.mp4** - 60-90 second demo video
- [ ] Video shows all 3 user stories
- [ ] Video is smooth (60fps or 30fps)
- [ ] Video size <50MB
- [ ] Video uploaded to PR or hosted externally

---

## PR Description Template

Use these screenshots in your PR description:

```markdown
## Feature: Passbook Timeline and Journeys

### Screenshots

#### User Story 1: Import and View Pass
![Empty Timeline](docs/screenshots/01_timeline_empty.png)
![File Picker](docs/screenshots/02_import_file_picker.png)
![Pass Detail](docs/screenshots/03_pass_detail_boarding.png)

#### User Story 2: View Timeline
![Timeline Populated](docs/screenshots/05_timeline_populated.png)

#### User Story 3: Create Journey
![Multi-Selection](docs/screenshots/06_timeline_selection.png)
![Create Journey](docs/screenshots/07_journey_create_dialog.png)
![Journey List](docs/screenshots/09_journey_list_populated.png)
![Journey Detail](docs/screenshots/10_journey_detail.png)

### Demo Video

[Watch Full Demo](docs/screenshots/demo_full_flow.mp4)

### Feature Highlights

- ✅ Import and parse .pkpass files
- ✅ Display passes in chronological timeline
- ✅ Create custom journeys from selected passes
- ✅ Full CRUD operations for passes and journeys
- ✅ Material Design 3 UI
- ✅ Accessibility support (TalkBack)
- ✅ 70%+ test coverage
- ✅ Performance optimized (<2s timeline load)
```

---

## README.md Integration

Update `README.md` with a features section:

```markdown
## Features

### Import Passbook Files
Import `.pkpass` files from your device and view detailed information including barcodes, dates, and event details.

![Import Flow](docs/screenshots/03_pass_detail_boarding.png)

### Timeline View
View all your passes in a beautiful, chronological timeline sorted by date.

![Timeline](docs/screenshots/05_timeline_populated.png)

### Create Journeys
Group related passes together into named journeys for better organization.

![Journey Detail](docs/screenshots/10_journey_detail.png)
```

---

## GitHub Release Notes

When creating a release, include:

```markdown
## What's New in v1.0.0

🎉 First release of Pasbuk Enhanced!

### Features

- 📥 Import Apple Passbook (.pkpass) files
- 📅 View passes in chronological timeline
- 🗂️ Create custom journeys to organize passes
- 🎨 Material Design 3 UI with dark mode support
- ♿ Full accessibility support
- ⚡ Optimized performance (<2s load time)
- ✅ 70%+ test coverage

### Screenshots

[Include 4-5 key screenshots]

### Demo

[Link to demo video]

### Download

[Download APK](...)
```

---

## Notes

### Data Privacy

- Use **sample/test data** only - no real personal information
- No real barcodes (can be scanned and used fraudulently)
- No real booking references or ticket numbers
- Use fictional names, dates, and locations

### Screenshot Guidelines

- Keep consistent device/orientation across all screenshots
- Use light mode unless showing dark mode feature
- Ensure text is readable (high contrast)
- Show realistic data (not "Test Test 123")
- Keep navigation elements visible for context

### Video Guidelines

- Keep video short and focused (60-90 seconds)
- Show smooth interactions (no lag or stuttering)
- Demonstrate all 3 user stories
- Add text overlays to highlight features (optional)
- End with a summary or app logo

---

## Examples from Other Projects

**Good Examples**:
- [Jetpack Compose Samples](https://github.com/android/compose-samples) - Clean screenshots with device frames
- [Tivi](https://github.com/chrisbanes/tivi) - Multiple screenshots showing features
- [Plaid](https://github.com/nickbutcher/plaid) - Animated GIFs for interactions

**Tools Used by Top Projects**:
- Device frames: Pixel 4a, Pixel 5
- Mockup generators: Mockuphone, Screely
- GIF creation: Screen2Gif, LICEcap

---

## Submission Checklist

Before marking task complete:

- [ ] All required screenshots captured (15 total)
- [ ] Demo video recorded (60-90 seconds)
- [ ] Screenshots named correctly and organized
- [ ] All images <500KB each
- [ ] Video <50MB
- [ ] Screenshots use sample data (no real personal info)
- [ ] Alt text prepared for accessibility
- [ ] PR description template prepared with screenshots
- [ ] README.md updated with feature showcase

---

**Last Updated**: 2025-12-25
**Status**: Documentation Complete - Screenshot Capture Pending
**Next Step**: Run app on device and capture screenshots following this guide
