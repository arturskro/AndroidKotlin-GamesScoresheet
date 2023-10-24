package pl.arturs.android.gamescoresheet.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Game::class, Player::class, GamePlayerCrossRef::class, Round::class, Score::class, FinalScore::class], views = [PlayerScore::class], version = 10,  exportSchema = false)
@TypeConverters(pl.arturs.android.gamescoresheet.database.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}