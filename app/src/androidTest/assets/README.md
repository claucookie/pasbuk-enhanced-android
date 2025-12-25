# Test .pkpass Files

This directory contains sample .pkpass files for testing the Passbook Timeline and Journeys feature.

## Obtaining Sample .pkpass Files

Since .pkpass files are digitally signed ZIP archives, they cannot be easily generated programmatically for testing. Here are recommended sources for obtaining valid test files:

### 1. Apple Sample Passes
Download official sample passes from Apple's PassKit documentation:
- [Apple PassKit Package Format](https://developer.apple.com/library/archive/documentation/UserExperience/Conceptual/PassKit_PG/)
- Apple provides sample boarding passes, event tickets, and store cards

### 2. Online Pass Generators
Use online tools to create test passes:
- [PassKit.com](https://passkit.com/) - Free pass generator
- [PassSource](https://www.passsource.com/) - Sample pass templates

### 3. Real-World Passes
Use actual .pkpass files from:
- Airline boarding passes (sent via email)
- Event tickets from Eventbrite, Ticketmaster
- Loyalty cards from retailers
- Movie tickets from cinema apps

## Expected Test Files

For comprehensive testing, include at least one of each pass type:

1. **boarding-pass.pkpass** - Airline boarding pass (tests location, date/time, barcode)
2. **event-ticket.pkpass** - Concert/event ticket (tests date, venue, barcode)
3. **store-card.pkpass** - Loyalty/membership card (tests persistent storage)
4. **coupon.pkpass** - Discount coupon (tests expiration date)
5. **generic-pass.pkpass** - Generic pass type

## .pkpass File Structure

For reference, a valid .pkpass file is a ZIP archive containing:

```
pass.pkpass (ZIP archive)
├── pass.json          # Main pass data (REQUIRED)
├── manifest.json      # File checksums (REQUIRED)
├── signature          # Digital signature (REQUIRED)
├── logo.png           # Logo image (optional)
├── icon.png           # Icon image (optional)
├── thumbnail.png      # Thumbnail image (optional)
├── background.png     # Background image (optional)
└── logo@2x.png        # Retina logo (optional)
```

## Using Test Files

1. Place .pkpass files in this directory (`app/src/androidTest/assets/`)
2. Access them in instrumented tests using:
   ```kotlin
   val inputStream = InstrumentationRegistry
       .getInstrumentation()
       .context
       .assets
       .open("boarding-pass.pkpass")
   ```

## Testing Coverage

Each test file should verify:
- ✓ Valid ZIP extraction
- ✓ JSON parsing of pass.json
- ✓ Image extraction and storage
- ✓ Barcode rendering
- ✓ Date/time parsing
- ✓ Duplicate detection via serial number

## Security Note

Do NOT commit actual personal passes containing:
- Real barcodes/QR codes (can be used fraudulently)
- Personal information (names, booking references)
- Valid ticket/boarding pass numbers

Always use test/sample passes for development.
