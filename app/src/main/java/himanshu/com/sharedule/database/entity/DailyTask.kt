package himanshu.com.sharedule.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.TypeConverter
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.annotation.Keep

@Entity(tableName = "daily_tasks")
@IgnoreExtraProperties
@TypeConverters(RecurrenceTypeConverter::class)
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    var localId: Long = 0L, // Room local id
    @DocumentId
    var firebaseId: String? = null, // Firestore id
    var title: String = "", // Minimal required
    var description: String? = null, // Optional
    var date: Long = 0L, // UTC millis for the day (for non-repeating)
    var isDone: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var recurrence: Recurrence? = null, // Null = not repeating
    var expectedHours: Float = 1.0f // Expected hours to complete
)

// Recurrence options for repeating tasks
@Keep
data class Recurrence(
    val type: String = "", // e.g., "DAILY", "WEEKLY", "CUSTOM"
    val daysOfWeek: List<Int>? = null, // For weekly/custom (0=Sun, 6=Sat)
    val interval: Int? = null // For custom (e.g., every 2 days)
)

class RecurrenceTypeConverter {
    @TypeConverter
    fun fromRecurrence(recurrence: Recurrence?): String? {
        return recurrence?.let { Gson().toJson(it) }
    }
    @TypeConverter
    fun toRecurrence(data: String?): Recurrence? {
        if (data == null) return null
        val type = object : TypeToken<Recurrence>() {}.type
        return Gson().fromJson(data, type)
    }
} 