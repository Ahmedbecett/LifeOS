package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CareerItem
import com.example.data.model.FinanceTransaction
import com.example.data.model.SavingsGoal
import com.example.data.model.ShoppingItem
import com.example.data.model.StudyPlan
import com.example.data.model.TaskItem
import com.example.data.model.TripPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeOsDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    // Trips
    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun getAllTrips(): Flow<List<TripPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripPlan): Long

    @Update
    suspend fun updateTrip(trip: TripPlan)

    @Delete
    suspend fun deleteTrip(trip: TripPlan)

    // Study
    @Query("SELECT * FROM study_plans ORDER BY createdAt DESC")
    fun getAllStudyPlans(): Flow<List<StudyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(plan: StudyPlan): Long

    @Update
    suspend fun updateStudyPlan(plan: StudyPlan)

    @Delete
    suspend fun deleteStudyPlan(plan: StudyPlan)

    // Finance
    @Query("SELECT * FROM finance_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<FinanceTransaction>)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransaction)

    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)

    // Career
    @Query("SELECT * FROM career_items ORDER BY createdAt DESC")
    fun getAllCareerItems(): Flow<List<CareerItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerItem(item: CareerItem): Long

    @Update
    suspend fun updateCareerItem(item: CareerItem)

    @Delete
    suspend fun deleteCareerItem(item: CareerItem)

    // Shopping
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, createdAt DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    // Clear everything for user privacy
    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM trips")
    suspend fun clearTrips()

    @Query("DELETE FROM study_plans")
    suspend fun clearStudyPlans()

    @Query("DELETE FROM finance_transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavingsGoals()

    @Query("DELETE FROM career_items")
    suspend fun clearCareerItems()

    @Query("DELETE FROM shopping_items")
    suspend fun clearShoppingItems()
}
