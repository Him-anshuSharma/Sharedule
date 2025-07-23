package himanshu.com.sharedule.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import himanshu.com.sharedule.database.entity.DailyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTask(task: DailyTask): Long

    @Update
    suspend fun updateTask(task: DailyTask)

    @Delete
    suspend fun deleteTask(task: DailyTask)

    @Query("SELECT * FROM daily_tasks WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks")
    fun getAllTasks(): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isDone = 1 AND date = :date")
    fun getCompletedTasksForDate(date: Long): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isDone = 0 AND date = :date")
    fun getPendingTasksForDate(date: Long): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE date = :date")
    suspend fun getTasksByDate(date: Long): List<DailyTask>

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAllTasks()
}