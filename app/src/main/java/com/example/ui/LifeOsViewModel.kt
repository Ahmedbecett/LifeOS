package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiGeneratedPlan
import com.example.data.api.GeminiApiClient
import com.example.data.local.LifeOsDatabase
import com.example.data.model.CareerItem
import com.example.data.model.FinanceTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.ShoppingItem
import com.example.data.model.StudyPlan
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import com.example.data.repository.LifeOsRepository
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LifeOsScreen {
    DASHBOARD,
    AI_ASSISTANT,
    TRAVEL,
    STUDY,
    CAREER,
    FINANCE,
    TASKS,
    SHOPPING,
    SETTINGS
}

class LifeOsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LifeOsDatabase.getInstance(application, viewModelScope)
    private val repository = LifeOsRepository(database.dao())

    // Navigation and Language
    private val _currentScreen = MutableStateFlow(LifeOsScreen.DASHBOARD)
    val currentScreen: StateFlow<LifeOsScreen> = _currentScreen.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // Data streams
    val allTasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrips: StateFlow<List<TripPlan>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyPlans: StateFlow<List<StudyPlan>> = repository.allStudyPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<FinanceTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCareerItems: StateFlow<List<CareerItem>> = repository.allCareerItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShoppingItems: StateFlow<List<ShoppingItem>> = repository.allShoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant State
    private val _aiPrompt = MutableStateFlow("")
    val aiPrompt: StateFlow<String> = _aiPrompt.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _generatedPlan = MutableStateFlow<AiGeneratedPlan?>(null)
    val generatedPlan: StateFlow<AiGeneratedPlan?> = _generatedPlan.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun navigateTo(screen: LifeOsScreen) {
        _currentScreen.value = screen
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun togglePremium() {
        _isPremium.value = !_isPremium.value
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun setAiPrompt(text: String) {
        _aiPrompt.value = text
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // AI Plan Generation & Import
    fun generatePlanFromAi(prompt: String? = null) {
        val query = prompt ?: _aiPrompt.value
        if (query.isBlank()) return

        _aiLoading.value = true
        _generatedPlan.value = null
        _statusMessage.value = null

        viewModelScope.launch {
            try {
                val plan = GeminiApiClient.generatePlan(query)
                _generatedPlan.value = plan
            } catch (e: Exception) {
                _statusMessage.value = "Failed to generate plan: ${e.message}"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun applyCurrentPlanToLifeOs() {
        val plan = _generatedPlan.value ?: return
        viewModelScope.launch {
            try {
                // Insert generated tasks
                if (plan.tasks.isNotEmpty()) {
                    for (task in plan.tasks) {
                        repository.insertTask(task)
                    }
                }
                // Insert trip if present
                plan.trip?.let {
                    repository.insertTrip(it)
                }
                // Insert study plan if present
                plan.studyPlan?.let {
                    repository.insertStudyPlan(it)
                }
                // Insert savings goal if present
                plan.savingsGoal?.let {
                    repository.insertSavingsGoal(it)
                }

                _statusMessage.value = "Plan successfully imported into LifeOS!"
            } catch (e: Exception) {
                _statusMessage.value = "Error saving plan: ${e.message}"
            }
        }
    }

    // Task Operations
    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun addTask(title: String, description: String, category: String, priority: String, dueDate: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskItem(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Trip Operations
    fun addTrip(
        destination: String,
        durationDays: Int,
        budget: Double,
        accommodation: String,
        itinerary: String,
        packing: String,
        phrases: String
    ) {
        viewModelScope.launch {
            repository.insertTrip(
                TripPlan(
                    destination = destination,
                    durationDays = durationDays,
                    budget = budget,
                    accommodation = accommodation,
                    dailyItineraryJson = itinerary,
                    packingChecklistJson = packing,
                    usefulPhrasesJson = phrases
                )
            )
        }
    }

    fun deleteTrip(trip: TripPlan) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    // Study Operations
    fun addStudyPlan(subject: String, targetExamDate: String, goalDescription: String, scheduleNotes: String) {
        viewModelScope.launch {
            repository.insertStudyPlan(
                StudyPlan(
                    subject = subject,
                    targetExamDate = targetExamDate,
                    goalDescription = goalDescription,
                    scheduleNotes = scheduleNotes,
                    progressPercent = 0
                )
            )
        }
    }

    fun updateStudyProgress(plan: StudyPlan, newProgress: Int) {
        viewModelScope.launch {
            repository.updateStudyPlan(plan.copy(progressPercent = newProgress.coerceIn(0, 100)))
        }
    }

    fun deleteStudyPlan(plan: StudyPlan) {
        viewModelScope.launch {
            repository.deleteStudyPlan(plan)
        }
    }

    // Finance Operations
    fun addTransaction(title: String, amount: Double, type: String, category: String, date: String, notes: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                FinanceTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    notes = notes
                )
            )
        }
    }

    fun deleteTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, currentAmount: Double, targetDate: String) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDate = targetDate
                )
            )
        }
    }

    fun updateSavingsProgress(goal: SavingsGoal, addedAmount: Double) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goal.copy(currentAmount = (goal.currentAmount + addedAmount).coerceAtLeast(0.0)))
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // Career Operations
    fun addCareerItem(title: String, type: String, companyOrField: String, status: String, deadline: String, notes: String) {
        viewModelScope.launch {
            repository.insertCareerItem(
                CareerItem(
                    title = title,
                    type = type,
                    companyOrField = companyOrField,
                    status = status,
                    deadline = deadline,
                    notes = notes
                )
            )
        }
    }

    fun updateCareerStatus(item: CareerItem, newStatus: String) {
        viewModelScope.launch {
            repository.updateCareerItem(item.copy(status = newStatus))
        }
    }

    fun deleteCareerItem(item: CareerItem) {
        viewModelScope.launch {
            repository.deleteCareerItem(item)
        }
    }

    // Shopping Operations
    fun addShoppingItem(name: String, category: String, estimatedPrice: Double, isWishlist: Boolean) {
        viewModelScope.launch {
            repository.insertShoppingItem(
                ShoppingItem(
                    name = name,
                    category = category,
                    estimatedPrice = estimatedPrice,
                    isWishlist = isWishlist
                )
            )
        }
    }

    fun toggleShoppingPurchased(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    // Privacy & Data reset
    fun wipeAllUserData() {
        viewModelScope.launch {
            repository.wipeAllUserData()
            _generatedPlan.value = null
            _statusMessage.value = "All local data has been permanently wiped."
        }
    }
}
