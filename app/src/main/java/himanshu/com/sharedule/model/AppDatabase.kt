package himanshu.com.sharedule.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DailyTask::class], version = 2, exportSchema = false)
@TypeConverters(RecurrenceTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyTaskDao(): DailyTaskDao
} 