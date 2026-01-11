package labs.claucookie.pasbuk.data.service

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import labs.claucookie.pasbuk.BuildConfig
import labs.claucookie.pasbuk.domain.model.ActivitySuggestion
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.service.GeminiApiException
import labs.claucookie.pasbuk.domain.service.GeminiRateLimitException
import labs.claucookie.pasbuk.domain.service.GeminiSuggestionService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GeminiSuggestionService using Google's Gemini AI.
 */
@Singleton
class GeminiSuggestionServiceImpl @Inject constructor() : GeminiSuggestionService {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",  // Fast stable model for quick suggestions
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 4048  // Increased to accommodate JSON response with multiple suggestions
        }
    )

    override suspend fun generateSuggestions(journey: Journey): Result<List<ActivitySuggestion>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Gemini API called for journey: ${journey.name}")

                // Validate journey has at least 2 passes (to have gaps)
                if (journey.passes.size < 2) {
                    Log.d(TAG, "Journey has < 2 passes, returning empty list")
                    return@withContext Result.success(emptyList())
                }

                val prompt = buildPrompt(journey)
                Log.d(TAG, "Prompt built, sending to Gemini API...")
                Log.v(TAG, "Prompt: $prompt")

                val response = model.generateContent(prompt)
                val responseText = response.text ?: ""
                Log.d(TAG, "Gemini API response received: ${responseText.length} chars")
                Log.v(TAG, "Response: $responseText")

                val suggestions = parseResponse(responseText)
                Log.d(TAG, "Parsed ${suggestions.size} suggestions from response")
                suggestions.forEachIndexed { index, suggestion ->
                    Log.v(TAG, "Suggestion $index: positionIndex=${suggestion.positionIndex}, title='${suggestion.title}'")
                }

                Result.success(suggestions)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API error: ${e.message}", e)
                when {
                    e.message?.contains("429") == true -> {
                        Result.failure(
                            GeminiRateLimitException("Rate limit exceeded. Please try again later.")
                        )
                    }
                    e.message?.contains("API key") == true || e.message?.contains("401") == true -> {
                        Result.failure(GeminiApiException("Invalid API key", e))
                    }
                    e.message?.contains("RESOURCE_EXHAUSTED") == true -> {
                        Result.failure(GeminiRateLimitException("API quota exceeded"))
                    }
                    else -> {
                        Result.failure(
                            GeminiApiException("Failed to generate suggestions: ${e.message}", e)
                        )
                    }
                }
            }
        }
    }

    private fun buildPrompt(journey: Journey): String {
        val passDetails = journey.passes.mapIndexed { index, pass ->
            """
            Pass ${index + 1}:
            - Event: ${pass.description}
            - Organization: ${pass.organizationName}
            - Type: ${pass.passType}
            - Date/Time: ${formatDateTime(pass.relevantDate)}
            - Location: ${formatLocation(pass)}
            """.trimIndent()
        }.joinToString("\n\n")

        return """
        You are a travel planning assistant analyzing a user's journey named "${journey.name}".

        The journey contains ${journey.passes.size} events/passes listed chronologically below:

        $passDetails

        TASK: Identify the 2-3 most important GAPS between these events where the user might need to take action.
        For each gap, provide a DIRECT, ACTIONABLE suggestion (not generic advice).

        RULES:
        1. Focus on gaps where timing creates a logical need (e.g., meal times, long waits, overnight stays, transportation)
        2. Suggest SPECIFIC actions (e.g., "Book a restaurant near the venue for 6 PM" NOT "Plan for dinner")
        3. Prioritize practical logistics: food, lodging, transportation between events
        4. ONLY suggest for the most critical 2-3 gaps (not every gap)
        5. If events are very close in time or location, no suggestion needed for that gap
        6. Be concise - titles max 60 chars, descriptions max 200 chars

        OUTPUT FORMAT (strict JSON array, no markdown backticks):
        [
          {
            "positionIndex": <gap number, 1 = between event 1 and 2, 2 = between event 2 and 3, etc.>,
            "title": "<Action-oriented title, max 60 chars>",
            "description": "<Detailed suggestion with specifics, max 200 chars>",
            "reasoning": "<Brief explanation why this gap matters, max 150 chars>"
          }
        ]

        Return ONLY the JSON array, no other text or formatting.
        """.trimIndent()
    }

    private fun parseResponse(text: String): List<ActivitySuggestion> {
        if (text.isBlank()) return emptyList()

        // Extract JSON array from response (remove markdown backticks if present)
        val jsonText = text
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return try {
            parseJsonManually(jsonText)
        } catch (e: Exception) {
            // Log error but return empty list (graceful degradation)
            emptyList()
        }
    }

    private fun parseJsonManually(json: String): List<ActivitySuggestion> {
        val suggestions = mutableListOf<ActivitySuggestion>()

        // Find JSON objects within array
        val objectRegex = """\{[^}]+\}""".toRegex()
        val matches = objectRegex.findAll(json)

        for (match in matches) {
            val obj = match.value

            val positionIndex = extractIntField(obj, "positionIndex") ?: continue
            val title = extractStringField(obj, "title") ?: continue
            val description = extractStringField(obj, "description") ?: continue
            val reasoning = extractStringField(obj, "reasoning")

            suggestions.add(
                ActivitySuggestion(
                    id = UUID.randomUUID().toString(),
                    positionIndex = positionIndex,
                    title = title,
                    description = description,
                    reasoning = reasoning,
                    isDismissed = false,
                    generatedAt = Instant.now()
                )
            )
        }

        return suggestions
    }

    private fun extractIntField(json: String, field: String): Int? {
        val regex = """"$field"\s*:\s*(\d+)""".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractStringField(json: String, field: String): String? {
        val regex = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun formatDateTime(instant: Instant?): String {
        return instant?.let {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
                .withZone(ZoneId.systemDefault())
            formatter.format(it)
        } ?: "No date"
    }

    private fun formatLocation(pass: Pass): String {
        return pass.locations.firstOrNull()?.let { loc ->
            val coords = "${loc.latitude}, ${loc.longitude}"
            if (loc.relevantText != null) {
                "${loc.relevantText} ($coords)"
            } else {
                coords
            }
        } ?: "No location"
    }

    companion object {
        private const val TAG = "GeminiSuggestionService"
    }
}
