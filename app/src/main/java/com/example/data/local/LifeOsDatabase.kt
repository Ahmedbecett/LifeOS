package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CareerItem
import com.example.data.model.FinanceTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.ShoppingItem
import com.example.data.model.StudyPlan
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskItem::class,
        TripPlan::class,
        StudyPlan::class,
        FinanceTransaction::class,
        SavingsGoal::class,
        CareerItem::class,
        ShoppingItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LifeOsDatabase : RoomDatabase() {
    abstract fun dao(): LifeOsDao

    companion object {
        @Volatile
        private var INSTANCE: LifeOsDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): LifeOsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeOsDatabase::class.java,
                    "lifeos_database.db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.dao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: LifeOsDao) {
                // Initial Tasks
                dao.insertTasks(
                    listOf(
                        TaskItem(
                            title = "Review AI Study Plan for upcoming exam",
                            description = "Go through chapters 1 to 4 and complete flashcards",
                            category = "STUDY",
                            isCompleted = false,
                            dueDate = "Today 18:00",
                            priority = "HIGH"
                        ),
                        TaskItem(
                            title = "Confirm hotel reservation in Istanbul",
                            description = "Sultanahmet boutique hotel confirmation code check",
                            category = "TRAVEL",
                            isCompleted = false,
                            dueDate = "Tomorrow",
                            priority = "MEDIUM"
                        ),
                        TaskItem(
                            title = "Update LinkedIn & GitHub Portfolio",
                            description = "Add recent Jetpack Compose architecture project",
                            category = "WORK",
                            isCompleted = true,
                            dueDate = "Done",
                            priority = "LOW"
                        ),
                        TaskItem(
                            title = "Transfer $150 to Savings Goal",
                            description = "Weekly deposit toward $1,000 emergency fund",
                            category = "FINANCE",
                            isCompleted = false,
                            dueDate = "This Friday",
                            priority = "HIGH"
                        )
                    )
                )

                // Initial Trip Plan (matching the user's Turkey prompt example!)
                val turkeyItinerary = """
Day 1: Arrival in Istanbul, check-in Sultanahmet, Grand Bazaar stroll & Turkish coffee
Day 2: Hagia Sophia, Blue Mosque & Bosphorus Sunset Cruise ($25)
Day 3: Topkapi Palace, Galata Tower & Istiklal Street street-food tour
Day 4: Flight/Bus to Cappadocia, Göreme open-air museum & cave hotel
Day 5: Hot Air Balloon sunrise viewing, Love Valley hike & pottery workshop
Day 6: Derinkuyu Underground City & Turkish bath (Hamam) relaxation
Day 7: Souvenir shopping (Turkish delight, spices) & flight home
                """.trimIndent()

                val turkeyPacking = """
Passport & Visa copy
Power bank & universal adapter
Comfortable walking shoes
Modest clothing for mosques
Light jacket for Cappadocia sunrise
Credit card & emergency cash
Sunscreen & travel toiletries
                """.trimIndent()

                val turkeyPhrases = """
Merhaba = Hello
Teşekkür ederim = Thank you
Lütfen = Please
Hesap lütfen = The bill please
Ne kadar? = How much is this?
İyi günler = Have a good day
İngilizce biliyor musunuz? = Do you speak English?
                """.trimIndent()

                dao.insertTrip(
                    TripPlan(
                        destination = "Turkey (Istanbul & Cappadocia)",
                        startDate = "Next Month (7 Days)",
                        durationDays = 7,
                        budget = 800.0,
                        accommodation = "Cave Suite in Göreme & Boutique Hotel in Sultanahmet",
                        notes = "Estimated flight: $260, Accommodation: $280, Food & Transport: $160, Activities: $100. Total within $800 budget.",
                        dailyItineraryJson = turkeyItinerary,
                        packingChecklistJson = turkeyPacking,
                        usefulPhrasesJson = turkeyPhrases
                    )
                )

                // Initial Study Plan
                dao.insertStudyPlan(
                    StudyPlan(
                        subject = "Computer Science & Algorithms",
                        targetExamDate = "In 3 weeks",
                        goalDescription = "Master Graphs, Dynamic Programming & System Architecture",
                        scheduleNotes = "Week 1: Graph BFS/DFS & Dijkstra\nWeek 2: Dynamic Programming patterns\nWeek 3: Mock exams and timed coding sprints",
                        progressPercent = 45
                    )
                )

                // Initial Finance
                dao.insertTransactions(
                    listOf(
                        FinanceTransaction(
                            title = "Monthly Salary",
                            amount = 2800.0,
                            type = "INCOME",
                            category = "OTHER",
                            date = "Oct 01",
                            notes = "Direct deposit"
                        ),
                        FinanceTransaction(
                            title = "Grocery Store Restock",
                            amount = 64.50,
                            type = "EXPENSE",
                            category = "FOOD",
                            date = "Today",
                            notes = "Fresh produce & coffee"
                        ),
                        FinanceTransaction(
                            title = "Metro Card Pass",
                            amount = 25.00,
                            type = "EXPENSE",
                            category = "TRANSPORT",
                            date = "Today",
                            notes = "Weekly commute transit"
                        )
                    )
                )

                dao.insertSavingsGoal(
                    SavingsGoal(
                        title = "Turkey Vacation Fund",
                        targetAmount = 800.0,
                        currentAmount = 550.0,
                        targetDate = "Next Month",
                        iconName = "FLIGHT"
                    )
                )
                dao.insertSavingsGoal(
                    SavingsGoal(
                        title = "Emergency Safety Cushion",
                        targetAmount = 1000.0,
                        currentAmount = 650.0,
                        targetDate = "6 Months",
                        iconName = "SAVINGS"
                    )
                )

                // Initial Career
                dao.insertCareerItem(
                    CareerItem(
                        title = "Senior Mobile Engineer",
                        type = "JOB_APPLICATION",
                        companyOrField = "Global FinTech Labs",
                        status = "INTERVIEWING",
                        deadline = "Technical Round next Tuesday",
                        notes = "Review coroutines, MVI/MVVM, and clean architecture"
                    )
                )
                dao.insertCareerItem(
                    CareerItem(
                        title = "System Design & Cloud Certification",
                        type = "GOAL",
                        companyOrField = "Professional Skill",
                        status = "IN_PROGRESS",
                        deadline = "Target: End of Q4",
                        notes = "Complete 1 practice exam per week"
                    )
                )

                // Initial Shopping
                dao.insertShoppingItems(
                    listOf(
                        ShoppingItem(
                            name = "Organic Espresso Beans",
                            category = "GROCERY",
                            estimatedPrice = 14.99,
                            isPurchased = true
                        ),
                        ShoppingItem(
                            name = "Universal Travel Power Plug",
                            category = "TECH",
                            estimatedPrice = 18.50,
                            isPurchased = false
                        ),
                        ShoppingItem(
                            name = "Noise-Cancelling Travel Headphones",
                            category = "WISHLIST",
                            estimatedPrice = 199.00,
                            isPurchased = false,
                            isWishlist = true
                        )
                    )
                )
            }
        }
    }
}
