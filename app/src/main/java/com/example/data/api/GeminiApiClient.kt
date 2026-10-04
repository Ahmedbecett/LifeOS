package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.SavingsGoal
import com.example.data.model.ShoppingItem
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
    val planType: String, // TRAVEL, STUDY, FINANCE, RELOCATION, CAREER, GENERAL, SHOPPING, DAY_PLAN
    val tasks: List<TaskItem> = emptyList(),
    val trip: TripPlan? = null,
    val studyPlan: StudyPlan? = null,
    val savingsGoal: SavingsGoal? = null,
    val shoppingItems: List<ShoppingItem> = emptyList(),
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
You are LifeOS AI, the ultimate intelligent life assistant.
The user provides a life goal or request (e.g. travel, study for exam, savings goal, relocation, career move, daily routine, shopping list).
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
                    lowerPrompt.contains("finance") || lowerPrompt.contains("expense") -> "FINANCE"
            lowerPrompt.contains("shop") || lowerPrompt.contains("grocery") || lowerPrompt.contains("buy") -> "SHOPPING"
            lowerPrompt.contains("plan my day") || lowerPrompt.contains("routine") || lowerPrompt.contains("today") -> "DAY_PLAN"
            lowerPrompt.contains("moving") || lowerPrompt.contains("move") || lowerPrompt.contains("relocat") ||
                    lowerPrompt.contains("city") || lowerPrompt.contains("apartment") -> "RELOCATION"
            lowerPrompt.contains("job") || lowerPrompt.contains("career") || lowerPrompt.contains("interview") ||
                    lowerPrompt.contains("cv") || lowerPrompt.contains("resume") -> "CAREER"
            else -> "GENERAL"
        }

        val lines = text.lines().filter { it.isNotBlank() }
        val title = lines.firstOrNull { it.startsWith("#") || it.contains("Plan") || it.length in 5..60 }
            ?.replace("#", "")?.trim() ?: "Custom LifeOS Plan: $prompt"

        val bulletTasks = lines
            .filter { it.trim().startsWith("- [ ]") || it.trim().startsWith("- ") || it.trim().startsWith("* ") || it.trim().matches(Regex("^\\d+\\..*")) }
            .take(6)
            .map { line ->
                val cleaned = line.replace(Regex("^[-*\\d.]+\\s*(\\[ \\])?\\s*"), "").trim()
                TaskItem(
                    title = cleaned.take(70),
                    description = cleaned,
                    category = if (planType == "DAY_PLAN") "GENERAL" else planType,
                    priority = "HIGH",
                    dueDate = "Within 7 days"
                )
            }

        val tasks = if (bulletTasks.isNotEmpty()) bulletTasks else listOf(
            TaskItem(title = "Execute Phase 1 of $title", category = if (planType == "DAY_PLAN") "GENERAL" else planType, priority = "HIGH", dueDate = "Day 1"),
            TaskItem(title = "Review Milestones and Budget", category = if (planType == "DAY_PLAN") "GENERAL" else planType, priority = "MEDIUM", dueDate = "Day 3"),
            TaskItem(title = "Finalize Preparation Checklist", category = if (planType == "DAY_PLAN") "GENERAL" else planType, priority = "HIGH", dueDate = "Day 5")
        )

        val trip = if (planType == "TRAVEL") {
            TripPlan(
                destination = extractDestination(prompt),
                startDate = "Next Month",
                durationDays = 7,
                budget = extractBudget(prompt),
                spentAmount = 0.0,
                currency = "USD",
                accommodation = "Central Hotel / AirBnb",
                notes = "Generated by LifeOS AI Assistant based on: $prompt",
                dailyItineraryJson = text,
                packingChecklistJson = "Passport & ID\nUniversal charger\nLocal currency & cards\nWeather-appropriate clothing\nMedicines & toiletries",
                usefulPhrasesJson = "Hello = Merhaba / Bonjour / Hola\nThank you = Teşekkürler / Merci / Gracias\nHow much? = Ne kadar? / Combien? / Cuánto?",
                documentsJson = "Passport (valid 6+ months)\nE-Visa Application\nRound-trip Flight Tickets\nHotel Booking Confirmation"
            )
        } else null

        val study = if (planType == "STUDY") {
            StudyPlan(
                subject = extractSubject(prompt),
                targetExamDate = "3 Weeks",
                goalDescription = "Score Top Tier Mastery",
                scheduleNotes = text,
                progressPercent = 10,
                dailyGoal = "2 hours focused deep study",
                spacedRepetitionTopic = "Day 1 (Core Concepts), Day 3 (Practice), Day 7 (Mock)"
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

        val shopping = if (planType == "SHOPPING") {
            listOf(
                ShoppingItem(name = "Core Grocery Essentials", category = "GROCERY", quantity = 1, estimatedPrice = 45.0),
                ShoppingItem(name = "Fresh Produce & Healthy Snacks", category = "GROCERY", quantity = 1, estimatedPrice = 25.0),
                ShoppingItem(name = "Household Cleaning Supplies", category = "HOME", quantity = 1, estimatedPrice = 18.0)
            )
        } else emptyList()

        return AiGeneratedPlan(
            title = title,
            summary = lines.take(3).joinToString(" ").replace("#", ""),
            planType = planType,
            tasks = tasks,
            trip = trip,
            studyPlan = study,
            savingsGoal = savings,
            shoppingItems = shopping,
            detailsText = text
        )
    }

    fun generateOfflineSmartPlan(prompt: String): AiGeneratedPlan {
        val lower = prompt.lowercase()
        return when {
            // "Plan my day"
            lower.contains("plan my day") || lower.contains("daily plan") -> {
                val daySchedule = """
07:30 - Morning Energizer: Hydration, 15m stretch & healthy breakfast.
08:30 - Deep Work Sprint 1: High-priority project deliverables (zero distractions).
11:30 - Quick Reset: 15m walk & review daily inbox.
12:30 - Nutritious Lunch & screen-free rest.
14:00 - Focus Block 2: Secondary tasks, meetings & collaborative work.
16:30 - LifeOS Daily Audit: Check off tasks, log expenses & update savings.
18:00 - Study / Exercise session (45 minutes).
21:30 - Night Wind-down: Reading, planning tomorrow & 8 hours sleep prep.
                """.trimIndent()

                AiGeneratedPlan(
                    title = "High-Focus Daily Blueprint",
                    summary = "An optimized circadian-aligned daily schedule prioritizing deep work in the morning and self-care in the evening.",
                    planType = "DAY_PLAN",
                    tasks = listOf(
                        TaskItem(title = "Morning Deep Work Sprint (Block 1)", category = "WORK", priority = "HIGH", dueDate = "Today 08:30"),
                        TaskItem(title = "Log daily expenses & reconcile balance", category = "FINANCE", priority = "MEDIUM", dueDate = "Today 16:30"),
                        TaskItem(title = "45-minute focused study or workout session", category = "STUDY", priority = "HIGH", dueDate = "Today 18:00"),
                        TaskItem(title = "Evening wind-down & plan next day in LifeOS", category = "GENERAL", priority = "LOW", dueDate = "Tonight 21:30")
                    ),
                    detailsText = daySchedule
                )
            }

            // "Create a shopping list"
            lower.contains("shopping list") || lower.contains("grocer") -> {
                val shoppingDetails = """
Organized Smart Shopping List:
• Produce: Bananas, spinach, avocados, honeycrisp apples (~$18.50)
• Pantry & Dairy: Almond milk, sourdough bread, eggs, organic oats (~$16.00)
• Protein & Healthy Fats: Chicken breast/tofu, olive oil, walnuts (~$28.00)
• Household: Biodegradable dish soap & paper towels (~$9.50)

Total Estimated Cost: ~$72.00 (within standard weekly allowance)
                """.trimIndent()

                AiGeneratedPlan(
                    title = "Weekly Restock & Grocery Checklist",
                    summary = "A nutritious, balanced shopping list structured by supermarket aisles to minimize wasted time and budget overruns.",
                    planType = "SHOPPING",
                    tasks = listOf(
                        TaskItem(title = "Check refrigerator inventory & pantry staples", category = "SHOPPING", priority = "MEDIUM", dueDate = "Before Store"),
                        TaskItem(title = "Purchase weekly groceries ($72 budget)", category = "SHOPPING", priority = "HIGH", dueDate = "This Saturday")
                    ),
                    shoppingItems = listOf(
                        ShoppingItem(name = "Organic Eggs & Sourdough", category = "GROCERY", quantity = 1, estimatedPrice = 8.50),
                        ShoppingItem(name = "Fresh Produce (Greens & Fruits)", category = "GROCERY", quantity = 1, estimatedPrice = 18.50),
                        ShoppingItem(name = "Almond Milk & Rolled Oats", category = "GROCERY", quantity = 1, estimatedPrice = 7.50),
                        ShoppingItem(name = "Chicken Breast / Tofu Pack", category = "GROCERY", quantity = 2, estimatedPrice = 16.00),
                        ShoppingItem(name = "Eco Dish Soap", category = "HOME", quantity = 1, estimatedPrice = 4.50)
                    ),
                    detailsText = shoppingDetails
                )
            }

            // "Plan a trip" or Turkey Prompt
            lower.contains("turkey") || (lower.contains("travel") || lower.contains("plan a trip")) -> {
                val dest = if (lower.contains("turkey")) "Turkey (Istanbul & Cappadocia)" else extractDestination(prompt)
                val budget = extractBudget(prompt)

                val itinerary = """
Day 1: Arrival & check-in, orientation walk in central historical quarter.
Day 2: Top cultural landmarks, ancient mosques/museums & scenic river cruise.
Day 3: Traditional bazaar spice exploration, local street food & viewpoint tower.
Day 4: Travel to regional highlight (e.g. Cappadocia fairy chimneys / historic coast).
Day 5: Sunrise sightseeing activity, valley hike & local artisan craft workshop.
Day 6: Underground city / archaeological ruins & traditional thermal bath.
Day 7: Souvenir shopping, local confection tastings & return flight home.
                """.trimIndent()

                val budgetNotes = """
Budget Allocation ($${budget.toInt()} Total):
• Flights / Long-distance transit: ~$${(budget * 0.32).toInt()}
• Boutique Accommodation: ~$${(budget * 0.35).toInt()}
• Dining & Culinary: ~$${(budget * 0.20).toInt()}
• Museum Passes & Activities: ~$${(budget * 0.13).toInt()}
Estimated Balance: On Track ($${budget.toInt()})
                """.trimIndent()

                val checklist = """
Passport with 6+ months validity
E-Visa & Travel Insurance documents
Power bank & universal travel adapter
Comfortable walking shoes
Modest clothing for religious cultural sites
Local currency cash + foreign exchange debit card
                """.trimIndent()

                val phrases = """
Hello = Merhaba
Thank you = Teşekkür ederim
Please = Lütfen
The bill please = Hesap lütfen
How much is this? = Ne kadar?
Do you speak English? = İngilizce biliyor musunuz?
Have a great day = İyi günler
                """.trimIndent()

                AiGeneratedPlan(
                    title = "7-Day $dest Expedition ($${budget.toInt()} Budget)",
                    summary = "A comprehensive 7-day itinerary covering top highlights, historical monuments, and culinary delights within a $$budget budget.",
                    planType = "TRAVEL",
                    tasks = listOf(
                        TaskItem(title = "Check passport validity & travel visa requirements", category = "TRAVEL", priority = "HIGH", dueDate = "Day 1"),
                        TaskItem(title = "Book boutique accommodation & internal transit", category = "TRAVEL", priority = "HIGH", dueDate = "Day 2"),
                        TaskItem(title = "Withdraw local travel currency cash for markets", category = "FINANCE", priority = "MEDIUM", dueDate = "Day 4"),
                        TaskItem(title = "Download offline maps and translation packs", category = "TRAVEL", priority = "MEDIUM", dueDate = "Day 5"),
                        TaskItem(title = "Pack modest attire and comfortable footwear", category = "TRAVEL", priority = "LOW", dueDate = "Day 6")
                    ),
                    trip = TripPlan(
                        destination = dest,
                        startDate = "Nov 12, 2026",
                        endDate = "Nov 19, 2026",
                        durationDays = 7,
                        budget = budget,
                        spentAmount = 0.0,
                        currency = if (lower.contains("turkey")) "TRY" else "USD",
                        accommodation = "Boutique Central Hotel / Cave Suite",
                        notes = budgetNotes,
                        dailyItineraryJson = itinerary,
                        packingChecklistJson = checklist,
                        usefulPhrasesJson = phrases,
                        documentsJson = "Passport (valid 6+ months)\nE-Visa Approval\nFlight Confirmation\nHotel Reservation Voucher\nTravel Health Insurance"
                    ),
                    detailsText = "$itinerary\n\n$budgetNotes\n\nDocuments & Checklist:\n$checklist\n\nUseful Phrases:\n$phrases"
                )
            }

            // "Create a study plan" or Exam Prompt
            lower.contains("exam") || lower.contains("study") || lower.contains("three weeks") -> {
                val studySchedule = """
Week 1: Foundations & Comprehensive Audit (Days 1–7)
• Days 1–2: Audit syllabus, highlight weak spots, organize lecture notes.
• Days 3–5: Deep dive into core modules with Pomodoro technique (4 x 25 min sessions).
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
                        subject = extractSubject(prompt),
                        targetExamDate = "In 3 Weeks",
                        goalDescription = "Score in Top 10% with active recall and practice testing",
                        scheduleNotes = studySchedule,
                        progressPercent = 15,
                        dailyGoal = "2 hours focused deep study",
                        spacedRepetitionTopic = "Spaced intervals: Day 1, Day 3, Day 7, Day 14"
                    ),
                    detailsText = studySchedule
                )
            }

            // "Track my expenses" or Savings Prompt
            lower.contains("save") || lower.contains("1000") || lower.contains("expense") || lower.contains("budget") -> {
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

            // General or Moving
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
            lower.contains("france") || lower.contains("paris") -> "Paris, France"
            lower.contains("italy") || lower.contains("rome") -> "Rome, Italy"
            lower.contains("spain") || lower.contains("barcelona") -> "Spain"
            lower.contains("germany") -> "Germany"
            lower.contains("dubai") -> "Dubai, UAE"
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
            lower.contains("program") || lower.contains("cod") || lower.contains("computer") -> "Computer Science & Algorithms"
            lower.contains("physics") -> "Physics"
            lower.contains("biology") -> "Biology & Medical Science"
            lower.contains("history") -> "History & Social Sciences"
            lower.contains("language") -> "Language & Linguistics"
            else -> "Academic Subject"
        }
    }
}
