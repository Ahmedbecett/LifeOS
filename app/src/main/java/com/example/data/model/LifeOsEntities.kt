package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "GENERAL", // GENERAL, STUDY, WORK, TRAVEL, SHOPPING, FINANCE
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val reminderTime: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trips")
data class TripPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destination: String,
    val startDate: String = "",
    val endDate: String = "",
    val durationDays: Int = 7,
    val budget: Double = 0.0,
    val spentAmount: Double = 0.0,
    val currency: String = "USD",
    val accommodation: String = "",
    val notes: String = "",
    val dailyItineraryJson: String = "",
    val packingChecklistJson: String = "",
    val usefulPhrasesJson: String = "",
    val documentsJson: String = "Passport & Visa Copy\nFlight Confirmation\nHotel Reservation Voucher\nTravel Medical Insurance",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val targetExamDate: String = "",
    val goalDescription: String = "",
    val scheduleNotes: String = "",
    val progressPercent: Int = 0,
    val dailyGoal: String = "2 hours focused deep study",
    val spacedRepetitionTopic: String = "Review interval: Day 1, Day 3, Day 7, Day 14",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // INCOME or EXPENSE
    val category: String, // FOOD, TRANSPORT, HOUSING, SHOPPING, TRAVEL, OTHER
    val date: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String = "",
    val iconName: String = "FLAG",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "career_items")
data class CareerItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String = "JOB_APPLICATION", // JOB_APPLICATION, GOAL, INTERVIEW_PREP, CV_NOTE
    val companyOrField: String = "",
    val status: String = "APPLIED", // SAVED, APPLIED, INTERVIEWING, OFFER, REJECTED
    val applicationDate: String = "Recent",
    val deadline: String = "",
    val notes: String = "",
    val interviewPrepQuestions: String = "STAR Method: Situation, Task, Action, Result.\nQ: Describe a difficult engineering obstacle and your solution.\nQ: How do you align priorities when multiple deadlines collide?",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "GROCERY", // GROCERY, TECH, HOME, CLOTHING, WISHLIST
    val quantity: Int = 1,
    val estimatedPrice: Double = 0.0,
    val isPurchased: Boolean = false,
    val isWishlist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
