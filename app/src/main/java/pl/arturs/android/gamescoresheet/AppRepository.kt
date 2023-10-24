package pl.arturs.android.gamescoresheet

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import pl.arturs.android.gamescoresheet.database.AppDatabase
import pl.arturs.android.gamescoresheet.database.FinalScore
import pl.arturs.android.gamescoresheet.database.Game
import pl.arturs.android.gamescoresheet.database.Player
import pl.arturs.android.gamescoresheet.database.GameItem
import pl.arturs.android.gamescoresheet.database.RoundScore
import java.util.Date
import java.util.UUID

private const val DATABASE_NAME = "gss-database"

class AppRepository private constructor(
    context: Context) {

    private val appDatabase: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        DATABASE_NAME
    ).fallbackToDestructiveMigration().build()

    fun getGames() : Flow<List<Game>> {
        return appDatabase.appDao().getGames()
    }

    fun getGame(gameId: UUID) : Flow<Game> {
        return appDatabase.appDao().getGame(gameId)
    }

    fun getActivePlayers() : Flow<List<Player>> {
        return appDatabase.appDao().getActivePlayers()
    }

    fun getPlayers(gameId: UUID): Flow<List<Player>> {
        return appDatabase.appDao().getPlayers(gameId)
    }

    fun getRounds(gameId: UUID): Flow<List<RoundScore>> {
        return appDatabase.appDao().getRoundScores(gameId)
    }

    fun getGameListItem(): Flow<List<GameItem>> {
        return appDatabase.appDao().getGameListItem()
    }

    suspend fun updatePlayer(player: Player) {
        appDatabase.appDao().updatePlayer(player)
    }

    suspend fun updateGame(game: Game) {
        appDatabase.appDao().updateGame(game)
    }

    suspend fun insertRoundScore(gameId: UUID, roundName: String, scores: Map<UUID, Int>, roundCounter: Int, date: Date) {
        appDatabase.appDao().insertRoundScore(gameId, roundName, scores, roundCounter = roundCounter, date = date)
    }

    suspend fun insertGame(game: Game, players: List<Player>) {
        appDatabase.appDao().addGameWithPlayers(game, players)
    }

    suspend fun insertPlayer(player: Player) {
        appDatabase.appDao().addPlayer(player)
    }

    suspend fun insertFinalScore(finalScore: FinalScore) {
        appDatabase.appDao().insertFinalScore(finalScore)
    }

    suspend fun deleteGame(gameId: UUID) {
        appDatabase.appDao().deleteGame(gameId)
    }

    suspend fun deleteRoundWithPlayerScores(roundId: UUID) {
        appDatabase.appDao().deleteRoundWithPlayerScores(roundId)
    }

    suspend fun incrementWinCounter(playerId: UUID) {
        appDatabase.appDao().incrementWinCounter(playerId)
    }

    suspend fun incrementGamesPlayed(playerId: UUID) {
        appDatabase.appDao().incrementGamesPlayed(playerId)
    }

    companion object {
        private var INSTANCE: AppRepository? = null

        fun initialize (context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AppRepository(context)
            }
        }

        fun get(): AppRepository {
            return INSTANCE ?: throw IllegalStateException("Game Repository must be initialized")
        }
    }
}