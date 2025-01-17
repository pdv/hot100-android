package io.rstlne.hot100

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import java.io.Serializable

@Entity(tableName = "hot100")
data class ChartEntry(
    @PrimaryKey
    val id: Int?,
    @ColumnInfo(name = "chart_week")
    val week: String?,
    @ColumnInfo(name = "current_week")
    val position: Int?,
    val title: String?,
    val performer: String?,
    @ColumnInfo(name = "last_week")
    val previousPosition: Int?,
    @ColumnInfo(name = "peak_pos")
    val peakPosition: Int?,
    @ColumnInfo(name = "wks_on_chart")
    val weeksOnChart: Int?
)

data class Track(
    val title: String,
    val performer: String,
    val peak: Int
): Serializable

data class Entry(
    val week: String,
    val position: Int,
): Serializable

@Dao
interface Hot100Dao {
    @Query("""
select title, performer, min(peak_pos) as peak
from hot100
where performer like '%' || :query || '%' or title like '%' || :query || '%'
group by 1, 2
order by 3
limit 100
""")
    suspend fun search(query: String): List<Track>

    @Query("""
select chart_week as week, current_week as position
from hot100
where performer = :performer and title = :title
    """)
    suspend fun entries(performer: String, title: String): List<Entry>

    @Query("""
select current_week as peak, performer, title
from hot100
where chart_week = :week
    """)
    suspend fun chart(week: String): List<Track>
}

@Database(entities = [ChartEntry::class], version = 1)
abstract class Hot100Database : RoomDatabase() {
    abstract fun hot100Dao(): Hot100Dao
}
