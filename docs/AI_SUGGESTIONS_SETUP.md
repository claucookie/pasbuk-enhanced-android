# AI Journey Suggestions Setup Guide

This guide explains how to set up and use the AI-powered journey suggestions feature.

## Prerequisites

- Android Studio (latest stable version)
- JDK 17 or higher
- Gemini API key from Google AI

## Getting Started

### 1. Obtain a Gemini API Key

1. Visit [Google AI Studio](https://ai.google.dev/)
2. Sign in with your Google account
3. Create a new API key
4. Copy the API key (keep it secure!)

### 2. Configure the API Key

Add your Gemini API key to your local configuration:

1. Open or create `local.properties` in the project root
2. Add the following line:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
3. Replace `your_actual_api_key_here` with your actual API key

> **Note:** The `local.properties` file is in `.gitignore` and will not be committed to version control.

### 3. Build the Project

```bash
./gradlew clean build
```

### 4. Run the App

```bash
./gradlew installDebug
```

Or use Android Studio's "Run" button.

## How It Works

### Journey Creation
When you create a new journey with 2 or more passes:
1. The journey is saved to the database
2. An async background task analyzes the journey
3. Gemini AI generates 2-3 activity suggestions for gaps between events
4. Suggestions are stored in the database with the journey

### Viewing Suggestions
1. Open a journey in the Journey Detail screen
2. Suggestions appear as amber cards with lightbulb icons
3. Suggestions are positioned between passes at logical gaps
4. Click a suggestion to expand and see details

### Interacting with Suggestions
- **Expand/Collapse**: Tap the suggestion card to toggle details
- **Dismiss**: Tap the X button to permanently hide a suggestion
- **Reasoning**: Expanded suggestions show why the AI recommended them

## Features

### AI Prioritization
The AI analyzes:
- Time gaps between events
- Event types (boarding pass, event ticket, etc.)
- Locations
- Timing patterns (meal times, overnight stays, etc.)

It selects the 2-3 most important gaps where you're likely to need action.

### Suggestion Types
Common suggestions include:
- Restaurant reservations before/after events
- Transportation between locations
- Lodging for overnight gaps
- Activity planning for long waits

### Visual Design
- **Amber accent color** (#FFC107) distinguishes AI suggestions
- **Lightbulb icon** indicates AI-generated content
- **Dashed connectors** vs solid lines for passes
- **"AI" badge** on each suggestion card

## Troubleshooting

### "No suggestions appearing"
- **Check API Key**: Verify `GEMINI_API_KEY` is set in `local.properties`
- **Check Journey Size**: Need at least 2 passes with time gaps
- **Wait for Generation**: Allow 2-3 seconds after journey creation
- **Check Logs**: Look for errors tagged with `GenerateJourneySuggestions`

### "Build fails with API key error"
- Ensure `local.properties` has `GEMINI_API_KEY=your_key`
- Rebuild: `./gradlew clean build`
- Check that BuildConfig feature is enabled in `build.gradle.kts`

### "Rate limit exceeded"
- Gemini API has usage limits
- Wait a few minutes and try again
- Consider upgrading your API quota

### "Suggestions are not contextual"
- The AI uses limited context (pass data only)
- Ensure passes have complete information (dates, locations, descriptions)
- More detailed pass data = better suggestions

## Privacy & Data

### What data is sent to Gemini?
When generating suggestions, the following journey data is sent:
- Pass descriptions (event names)
- Organization names
- Pass types (boarding pass, ticket, etc.)
- Dates and times
- Locations (if available)
- Journey name

### What is NOT sent?
- Personal user information
- Barcode data
- Payment information
- Full pass JSON
- Other journeys

### Data Storage
- Suggestions are stored locally in your device's Room database
- No suggestion data is sent to external servers except during generation
- Dismissed suggestions remain in the database (marked as dismissed)

## API Costs

Gemini API usage:
- **Model**: gemini-1.5-flash (fast, cost-effective)
- **Cost**: ~1000 requests per month free tier
- **Per Journey**: ~1 request per journey creation
- **Token Usage**: ~500-1000 tokens per request

Monitor your usage at [Google AI Studio](https://ai.google.dev/)

## Development

### Testing Locally
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Modifying Prompts
Edit the prompt in `GeminiSuggestionServiceImpl.kt`:
```kotlin
private fun buildPrompt(journey: Journey): String {
    // Modify prompt logic here
}
```

### Changing Suggestion UI
Edit `TimelineSuggestionItem.kt` for visual changes.

### Adjusting Generation Logic
Edit `GenerateJourneySuggestionsUseCase.kt` for when/how suggestions are created.

## Support

For issues or questions:
1. Check the [GitHub Issues](https://github.com/claucookie/pasbuk-enhanced-android/issues)
2. Review the PR: #153
3. Check Gemini API documentation: https://ai.google.dev/docs

## License

This feature is part of the Pasbuk Enhanced Android app.
