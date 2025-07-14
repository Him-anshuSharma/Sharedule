package himanshu.com.sharedule.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import himanshu.com.sharedule.database.entity.DailyTask
import himanshu.com.sharedule.database.dao.DailyTaskDao
import himanshu.com.sharedule.database.entity.RecurrenceTypeConverter

@Database(entities = [DailyTask::class], version = 3, exportSchema = false)
@TypeConverters(RecurrenceTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyTaskDao(): DailyTaskDao
}