package com.example.data.repository

import com.example.data.local.LifeOsDao
import com.example.data.model.CareerItem
import com.example.data.model.FinanceTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.ShoppingItem
import com.example.data.model.StudyPlan
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import kotlinx.coroutines.flow.Flow

class LifeOsRepository(private val dao: LifeOsDao) {
    // Tasks
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    suspend fun insertTask(task: TaskItem) = dao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = dao.deleteTask(task)

    // Trips
    val allTrips: Flow<List<TripPlan>> = dao.getAllTrips()
    suspend fun insertTrip(trip: TripPlan) = dao.insertTrip(trip)
    suspend fun updateTrip(trip: TripPlan) = dao.updateTrip(trip)
    suspend fun deleteTrip(trip: TripPlan) = dao.deleteTrip(trip)

    // Study
    val allStudyPlans: Flow<List<StudyPlan>> = dao.getAllStudyPlans()
    suspend fun insertStudyPlan(plan: StudyPlan) = dao.insertStudyPlan(plan)
    suspend fun updateStudyPlan(plan: StudyPlan) = dao.updateStudyPlan(plan)
    suspend fun deleteStudyPlan(plan: StudyPlan) = dao.deleteStudyPlan(plan)

    // Finance
    val allTransactions: Flow<List<FinanceTransaction>> = dao.getAllTransactions()
    suspend fun insertTransaction(transaction: FinanceTransaction) = dao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) = dao.deleteTransaction(transaction)

    val allSavingsGoals: Flow<List<SavingsGoal>> = dao.getAllSavingsGoals()
    suspend fun insertSavingsGoal(goal: SavingsGoal) = dao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    // Career
    val allCareerItems: Flow<List<CareerItem>> = dao.getAllCareerItems()
    suspend fun insertCareerItem(item: CareerItem) = dao.insertCareerItem(item)
    suspend fun updateCareerItem(item: CareerItem) = dao.updateCareerItem(item)
    suspend fun deleteCareerItem(item: CareerItem) = dao.deleteCareerItem(item)

    // Shopping
    val allShoppingItems: Flow<List<ShoppingItem>> = dao.getAllShoppingItems()
    suspend fun insertShoppingItem(item: ShoppingItem) = dao.insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = dao.updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItem) = dao.deleteShoppingItem(item)

    // Privacy Wipe
    suspend fun wipeAllUserData() {
        dao.clearTasks()
        dao.clearTrips()
        dao.clearStudyPlans()
        dao.clearTransactions()
        dao.clearSavingsGoals()
        dao.clearCareerItems()
        dao.clearShoppingItems()
    }
}
