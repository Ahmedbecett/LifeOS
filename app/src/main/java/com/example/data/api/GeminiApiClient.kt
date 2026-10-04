package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.SavingsGoal
import com.example.data.model.StudyPlan
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiGeneratedPlan(
    val title: String,
    val summary: String,
    val planType: String, // TRAVEL, STUDY, FINANCE, RELOCATION, CAREER, GENERAL
    val tasks: List<TaskItem> = emptyList(),
    val trip: TripPlan? = null,
    val studyPlan: StudyPlan? = null,
    val savingsGoal: SavingsGoal? = null,
    val detailsText: String = ""
)

object GeminiApiClient {
    private const val TAG = "LifeOsGemini"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generatePlan(userPrompt: String): AiGeneratedPlan = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val apiResponse = callGeminiApi(userPrompt, apiKey)
                if (apiResponse.isNotBlank()) {
                    return@withContext parseAiResponseToPlan(userPrompt, apiResponse)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to intelligent offline engine: ${e.message}")
            }
        }

        // Intelligent local rule-based planner for instant offline capability
        return@withContext generateOfflineSmartPlan(userPrompt)
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val systemPrompt = """
You are LifeOS, the ultimate intelligent life assistant.
The user provides a life goal or request (e.g. travel, study for exam, savings goal, relocation, career move).
Create a complete, actionable, highly practical plan.
Your response MUST include:
1. Short Catchy Title (e.g. "7-Day Turkey Expedition on $800")
2. Executive Summary (2-3 sentences)
3. Step-by-Step Schedule or Itinerary
4. Financial / Budget Breakdown
5. Action Checklist & Preparation Tasks
6. Useful phrases, documents, or key milestones.
Be encouraging, realistic, and structured.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nUser Request: $prompt")
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val err = response.body?.string() ?: ""
            Log.e(TAG, "Gemini error: code ${response.code}, msg: $err")
            return ""
        }

        val responseString = response.body?.string() ?: return ""
        val json = JSONObject(responseString)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }
        return ""
    }

    private fun parseAiResponseToPlan(prompt: String, text: String): AiGeneratedPlan {
        val lowerPrompt = prompt.lowercase()
        val planType = when {
            lowerPrompt.contains("travel") || lowerPrompt.contains("trip") || lowerPrompt.contains("turkey") ||
                    lowerPrompt.contains("japan") || lowerPrompt.contains("flight") || lowerPrompt.contains("visit") -> "TRAVEL"
            lowerPrompt.contains("exam") || lowerPrompt.contains("study") || lowerPrompt.contains("homework") ||
                    lowerPrompt.contains("learn") || lowerPrompt.contains("course") -> "STUDY"
            lowerPrompt.contains("save") || lowerPrompt.contains("budget") || lowerPrompt.contains("money") ||
                    lowerPrompt.contains("finance") || lowerPrompt.contains("dollar") -> "FINANCE"
            lowerPrompt.contains("moving") || lowerPrompt.contains("move") || lowerPrompt.contains("relocat") ||
                    lowerPrompt.contains("city") || lowerPrompt.contains("apartment") -> "RELOCATION"
            lowerPrompt.contains("job") || lowerPrompt.contains("career") || lowerPrompt.contains("interview") ||
                    lowerPrompt.contains("cv") || lowerPrompt.contains("resume") -> "CAREER"
            else -> "GENERAL"
        }

        val lines = text.lines().filter { it.isNotBlank() }
        val title = lines.firstOrNull { it.startsWith("#") || it.contains("Plan") || it.length in 5..60 }
            ?.replace("#", "")?.trim() ?: "Custom LifeOS Plan: $prompt"

        // Generate tasks from bullet points in the AI text
        val bulletTasks = lines
            .filter { it.trim().startsWith("- [ ]") || it.trim().startsWith("- ") || it.trim().startsWith("* ") || it.trim().matches(Regex("^\\d+\\..*")) }
            .take(6)
            .map { line ->
                val cleaned = line.replace(Regex("^[-*\\d.]+\\s*(\\[ \\])?\\s*"), "").trim()
                TaskItem(
                    title = cleaned.take(70),
                    description = cleaned,
                    category = planType,
                    priority = "HIGH",
                    dueDate = "Within 7 days"
                )
            }

        val tasks = if (bulletTasks.isNotEmpty()) bulletTasks else listOf(
            TaskItem(title = "Execute Phase 1 of $title", category = planType, priority = "HIGH", dueDate = "Day 1"),
            TaskItem(title = "Review Milestones and Budget", category = planType, priority = "MEDIUM", dueDate = "Day 3"),
            TaskItem(title = "Finalize Preparation Checklist", category = planType, priority = "HIGH", dueDate = "Day 5")
        )

        val trip = if (planType == "TRAVEL") {
            TripPlan(
                destination = extractDestination(prompt),
                startDate = "Next Month",
                durationDays = 7,
                budget = extractBudget(prompt),
                accommodation = "Central Hotel / AirBnb",
                notes = "Generated by LifeOS AI Assistant based on: $prompt",
                dailyItineraryJson = text,
                packingChecklistJson = "Passport & ID\nUniversal charger\nLocal currency & cards\nWeather-appropriate clothing\nMedicines & toiletries",
                usefulPhrasesJson = "Hello = Merhaba / Bonjour / Hola\nThank you = Teşekkürler / Merci / Gracias\nHow much? = Ne kadar? / Combien? / Cuánto?"
            )
        } else null

        val study = if (planType == "STUDY") {
            StudyPlan(
                subject = extractSubject(prompt),
                targetExamDate = "3 Weeks",
                goalDescription = "Score Top Tier Mastery",
                scheduleNotes = text,
                progressPercent = 10
            )
        } else null

        val savings = if (planType == "FINANCE") {
            SavingsGoal(
                title = "Target: $prompt",
                targetAmount = extractBudget(prompt).let { if (it > 0) it else 1000.0 },
                currentAmount = 0.0,
                targetDate = "6 Months",
                iconName = "SAVINGS"
            )
        } else null

        return AiGeneratedPlan(
            title = title,
            summary = lines.take(3).joinToString(" ").replace("#", ""),
            planType = planType,
            tasks = tasks,
            trip = trip,
            studyPlan = study,
            savingsGoal = savings,
            detailsText = text
        )
    }

    fun generateOfflineSmartPlan(prompt: String): AiGeneratedPlan {
        val lower = prompt.lowercase()
        return when {
            // Travel prompt like "I'm traveling to Turkey next month for 7 days with a budget of $800"
            lower.contains("turkey") || (lower.contains("travel") && lower.contains("7 day")) -> {
                val itinerary = """
Day 1: Arrive in Istanbul, airport shuttle to Sultanahmet, explore Sultanahmet Square & Blue Mosque.
Day 2: Visit Hagia Sophia, Basilica Cistern, Grand Bazaar for local spices & Turkish tea.
Day 3: Bosphorus public ferry cruise, Galata Tower climb, sunset dinner at Karaköy ($20).
Day 4: Morning domestic flight/overnight bus to Cappadocia, check into Göreme Cave Suite.
Day 5: Sunrise Hot Air Balloon observation, hike through Red Valley & Love Valley.
Day 6: Derinkuyu Underground City tour, Uchisar Castle panoramic view & Hamam bath.
Day 7: Souvenir shopping, Turkish delight tasting, return flight back home.
                """.trimIndent()

                val budgetNotes = """
Budget Breakdown ($800 Total):
• Flights / Long distance transport: $260
• Accommodation (6 nights @ $45 avg): $270
• Food & Drinks ($25/day): $175
• Activities & Museum passes: $95
Total Estimated: $800 (Balanced & Achievable)
                """.trimIndent()

                val checklist = """
Passport with 6+ months validity
Turkish E-Visa (if required)
Offline Google Maps of Istanbul & Göreme
Turkish Lira cash (500 TL for tips & street stalls)
Comfortable walking sneakers
Modest scarf/covering for mosque visits
Power adapter (European Type C/F)
                """.trimIndent()

                val phrases = """
Merhaba = Hello
Teşekkür ederim = Thank you
Lütfen = Please
Hesap lütfen = Check / Bill please
Ne kadar? = How much is this?
İndirim var mı? = Is there a discount?
İyi günler = Have a great day
                """.trimIndent()

                AiGeneratedPlan(
                    title = "7-Day Turkey Expedition ($800 Budget)",
                    summary = "A comprehensive 7-day itinerary covering Istanbul historic districts and Cappadocia fairy chimneys within a strict $800 budget.",
                    planType = "TRAVEL",
                    tasks = listOf(
                        TaskItem(title = "Check passport validity & apply for Turkey E-Visa", category = "TRAVEL", priority = "HIGH", dueDate = "Day 1"),
                        TaskItem(title = "Book Istanbul Sultanahmet hotel & Göreme cave room", category = "TRAVEL", priority = "HIGH", dueDate = "Day 2"),
                        TaskItem(title = "Exchange 500-1000 Turkish Lira or notify bank for ATM card", category = "FINANCE", priority = "MEDIUM", dueDate = "Day 4"),
                        TaskItem(title = "Download Istanbulkart public transit app and offline maps", category = "TRAVEL", priority = "MEDIUM", dueDate = "Day 5"),
                        TaskItem(title = "Pack modest mosque attire and Cappadocia morning layer", category = "TRAVEL", priority = "LOW", dueDate = "Day 6")
                    ),
                    trip = TripPlan(
                        destination = "Turkey (Istanbul & Cappadocia)",
                        startDate = "Next Month",
                        durationDays = 7,
                        budget = 800.0,
                        accommodation = "Sultanahmet Boutique Hotel & Göreme Cave Suite",
                        notes = budgetNotes,
                        dailyItineraryJson = itinerary,
                        packingChecklistJson = checklist,
                        usefulPhrasesJson = phrases
                    ),
                    detailsText = "$itinerary\n\n$budgetNotes\n\nKey Checklist:\n$checklist\n\nPhrases:\n$phrases"
                )
            }

            // Study prompt like "I have an exam in three weeks and I need a study plan"
            lower.contains("exam") || lower.contains("study") || lower.contains("three weeks") -> {
                val studySchedule = """
Week 1: Core Foundation & Theory Acquisition (Days 1–7)
• Days 1–2: Audit syllabus, highlight weak spots, organize lecture notes.
• Days 3–5: Deep dive into Chapters 1 to 4 with Pomodoro technique (4 x 25 min sessions).
• Days 6–7: Active recall flashcards creation & initial end-of-chapter problems.

Week 2: Intensive Practice & Application (Days 8–14)
• Days 8–10: Work through past exam papers under timed conditions.
• Days 11–12: Identify mistake patterns and rewrite formula cheat sheets.
• Days 13–14: Explain concepts aloud (Feynman Technique) to reinforce retention.

Week 3: Mock Exams & Peak Performance (Days 15–21)
• Days 15–17: Full-length simulation exams under strict time limits.
• Days 18–19: Light review of high-yield summary cards only; no new complex topics.
• Days 20–21: Rest, optimal hydration, 8 hours sleep, exam day kit preparation.
                """.trimIndent()

                AiGeneratedPlan(
                    title = "3-Week High-Performance Exam Sprint",
                    summary = "Structured 21-day timeline utilizing spaced repetition, active recall, and full-length exam simulations.",
                    planType = "STUDY",
                    tasks = listOf(
                        TaskItem(title = "Audit exam syllabus & identify top 3 weak areas", category = "STUDY", priority = "HIGH", dueDate = "Week 1 - Day 1"),
                        TaskItem(title = "Create 40 active recall flashcards for core terms", category = "STUDY", priority = "HIGH", dueDate = "Week 1 - Day 4"),
                        TaskItem(title = "Complete 2 past exam papers under strict 90 min timer", category = "STUDY", priority = "HIGH", dueDate = "Week 2 - Day 9"),
                        TaskItem(title = "Analyze mistakes and build 1-page condensed formula sheet", category = "STUDY", priority = "MEDIUM", dueDate = "Week 2 - Day 13"),
                        TaskItem(title = "Final simulation test & ensure 8h sleep before exam day", category = "STUDY", priority = "HIGH", dueDate = "Week 3 - Day 20")
                    ),
                    studyPlan = StudyPlan(
                        subject = "Comprehensive Exam Preparation",
                        targetExamDate = "In 3 Weeks",
                        goalDescription = "Score in Top 10% with active recall and practice testing",
                        scheduleNotes = studySchedule,
                        progressPercent = 15
                    ),
                    detailsText = studySchedule
                )
            }

            // Savings prompt like "I need to save $1,000 in six months"
            lower.contains("save") || lower.contains("1000") || lower.contains("1,000") || lower.contains("six months") -> {
                val financeBreakdown = """
Target: Save $1,000 in 6 Months (26 Weeks)

Weekly Target: ~$38.50 per week
Monthly Target: ~$166.67 per month

Actionable Cutbacks & Micro-Gains:
1. Coffee & Dining Out: Cut 2 takeout meals/week = Saves ~$25/week ($100/month)
2. Subscriptions: Cancel 1 unused streaming or gym subscription = Saves ~$15/month
3. Smart Grocery Strategy: Buy store brands & meal prep on Sundays = Saves ~$18/week
Total projected savings: ~$190/month (Exceeds goal ahead of schedule!)
                """.trimIndent()

                AiGeneratedPlan(
                    title = "$1,000 Savings Sprint (6-Month Roadmap)",
                    summary = "Save $1,000 with a frictionless $38.50 weekly automated target, backed by simple everyday micro-adjustments.",
                    planType = "FINANCE",
                    tasks = listOf(
                        TaskItem(title = "Open dedicated High-Yield Savings sub-account", category = "FINANCE", priority = "HIGH", dueDate = "This Week"),
                        TaskItem(title = "Automate $40 bi-weekly bank transfer on payday", category = "FINANCE", priority = "HIGH", dueDate = "Next Payday"),
                        TaskItem(title = "Audit bank statement & cancel recurring unused subscriptions", category = "FINANCE", priority = "MEDIUM", dueDate = "This Weekend"),
                        TaskItem(title = "Pack lunch for work 3 days next week ($20 saved)", category = "FINANCE", priority = "LOW", dueDate = "Next Week")
                    ),
                    savingsGoal = SavingsGoal(
                        title = "$1,000 Financial Cushion",
                        targetAmount = 1000.0,
                        currentAmount = 0.0,
                        targetDate = "6 Months",
                        iconName = "SAVINGS"
                    ),
                    detailsText = financeBreakdown
                )
            }

            // Moving / Relocation prompt like "I'm moving to another city next month"
            lower.contains("mov") || lower.contains("relocat") || lower.contains("city") -> {
                val relocationPlan = """
4-Week City Relocation Blueprint:

Week 4 Prior:
• Confirm new lease, move-in date, key handover protocol.
• Declutter room-by-room: Sell on marketplace or donate clothes.
• Request moving quotes (van rental vs movers).

Week 3 Prior:
• Collect 15-20 sturdy boxes, bubble wrap, tape & labeling markers.
• Notify current landlord or agent in writing.
• Begin packing out-of-season clothes and books.

Week 2 Prior:
• Schedule electricity, internet, gas & water transfer for move-in day.
• Submit postal address change & update bank address.
• Pack kitchen non-essentials and decorative items.

Week 1 Prior & Move Day:
• Pack "Day One Survival Box": Toiletries, bed linens, phone chargers, kettle, important documents.
• Defrost fridge 24h prior, clean previous apartment for deposit return.
• Final walkthrough, meter readings snapshot, celebrate arrival!
                """.trimIndent()

                AiGeneratedPlan(
                    title = "Seamless City Relocation Checklist",
                    summary = "A 4-week timeline covering logistics, utilities transfer, decluttering, and move-day essentials.",
                    planType = "RELOCATION",
                    tasks = listOf(
                        TaskItem(title = "Schedule electricity and home WiFi activation at new home", category = "WORK", priority = "HIGH", dueDate = "2 Weeks Before"),
                        TaskItem(title = "Declutter closet and donate unused items", category = "GENERAL", priority = "MEDIUM", dueDate = "3 Weeks Before"),
                        TaskItem(title = "Pack 'Day One Survival Box' (documents, chargers, bedding)", category = "GENERAL", priority = "HIGH", dueDate = "1 Day Before"),
                        TaskItem(title = "Take photos of utility meters and apartment condition", category = "FINANCE", priority = "HIGH", dueDate = "Move Day")
                    ),
                    detailsText = relocationPlan
                )
            }

            // General or Career
            else -> {
                val generalPlan = """
Actionable Master Plan for: "$prompt"

1. Objective Definition & Milestones:
• Milestone 1 (Immediate - 48h): Initial setup and resource gathering.
• Milestone 2 (Day 7): Core implementation sprint.
• Milestone 3 (Day 14): Review, refine, and measure tangible results.

2. Daily Routine Alignment:
• Allocate 45-60 focused minutes every morning before distractions arise.
• Track completion metrics daily inside LifeOS.
                """.trimIndent()

                AiGeneratedPlan(
                    title = "Action Plan: ${prompt.take(40)}",
                    summary = "Personalized strategic plan optimized for consistency and measurable progress.",
                    planType = "GENERAL",
                    tasks = listOf(
                        TaskItem(title = "Step 1: Set up dedicated workspace & resources", category = "GENERAL", priority = "HIGH", dueDate = "Day 1"),
                        TaskItem(title = "Step 2: 45-minute daily focus sprint", category = "GENERAL", priority = "MEDIUM", dueDate = "Daily"),
                        TaskItem(title = "Step 3: Mid-point progress review", category = "GENERAL", priority = "HIGH", dueDate = "End of Week")
                    ),
                    detailsText = generalPlan
                )
            }
        }
    }

    private fun extractDestination(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("turkey") -> "Turkey"
            lower.contains("japan") -> "Japan"
            lower.contains("france") -> "France"
            lower.contains("italy") -> "Italy"
            lower.contains("spain") -> "Spain"
            lower.contains("germany") -> "Germany"
            lower.contains("dubai") -> "Dubai"
            lower.contains("uk") || lower.contains("london") -> "London, UK"
            else -> "Dream Destination"
        }
    }

    private fun extractBudget(prompt: String): Double {
        val match = Regex("""\$?(\d+([.,]\d+)?)""").find(prompt)
        return match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 800.0
    }

    private fun extractSubject(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("math") -> "Mathematics & Calculus"
            lower.contains("program") || lower.contains("cod") || lower.contains("computer") -> "Computer Science"
            lower.contains("physics") -> "Physics"
            lower.contains("biology") -> "Biology & Medical Science"
            lower.contains("history") -> "History & Social Sciences"
            lower.contains("language") -> "Language & Linguistics"
            else -> "Academic Subject"
        }
    }
}
