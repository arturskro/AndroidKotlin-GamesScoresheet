package pl.arturs.android.gamescoresheet.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface AppDao {
    // Get all games
    @Query("SELECT * FROM games ORDER BY date DESC")
    fun getGames(): Flow<List<Game>>

    // Get game by game id
    @Query("SELECT * FROM games WHERE gameId = :gameId")
    fun getGame(gameId: UUID): Flow<Game>

    // Get all players
    @Query("SELECT * FROM players")
    fun getPlayers(): Flow<List<Player>>

    // Get players by game id
    @Query("SELECT * FROM players WHERE playerId IN (SELECT playerId FROM games_players WHERE gameId = :gameId)")
    fun getPlayers(gameId: UUID): Flow<List<Player>>

    // Get all active players
    @Query("SELECT * FROM players WHERE isActive=1")
    fun getActivePlayers(): Flow<List<Player>>

    // Get selected players
    @Query("SELECT * FROM players WHERE isSelected=1")
    fun getSelectedPlayers(): Flow<List<Player>>

    @Transaction
    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY date DESC")
    fun getRoundScores(gameId: UUID): Flow<List<RoundScore>>

    @Transaction
    @Query("SELECT * FROM games ORDER BY date DESC")
    fun getGameListItem(): Flow<List<GameItem>>

    @Transaction
    suspend fun addGameWithPlayers(game: Game, players: List<Player>) {
        addGame(game)
        for (player in players) {
            val gamePlayerCrossRef = GamePlayerCrossRef(game.gameId, player.playerId)
            insertGamePlayerCrossRef(gamePlayerCrossRef)
        }
    }

    @Transaction
    suspend fun insertRoundScore(gameId: UUID, roundName: String, scores: Map<UUID, Int>, roundCounter: Int, date: Date) {
        val roundId = UUID.randomUUID()

        val round = Round(roundId, gameId, roundName, roundCounter = roundCounter, date = date)
        insertRound(round)

        for ((playerId, score) in scores) {
            val playerScore = Score(roundId, playerId, score)
            insertPlayerScore(playerScore)
        }
    }

    @Transaction
    suspend fun deleteRoundWithPlayerScores(roundID: UUID) {
        deleteRound(roundID)
        deleteScores(roundID)
    }

    // Delete game section

    @Transaction
    suspend fun deleteGame(gameId: UUID) {
        deleteGameById(gameId)
        deleteGamePlayerCrossRefByGameId(gameId)
        deleteFinalScoreByGameId(gameId)
        deleteRoundsAndScoresForGame(gameId)
    }

    @Query("DELETE FROM games WHERE gameId = :gameId")
    suspend fun deleteGameById(gameId: UUID)

    @Query("DELETE FROM games_players WHERE gameId = :gameId")
    suspend fun deleteGamePlayerCrossRefByGameId(gameId: UUID)

    @Query("DELETE FROM winners WHERE gameId = :gameId")
    suspend fun deleteFinalScoreByGameId(gameId: UUID)

    @Query("DELETE FROM rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: UUID)

    @Query("DELETE FROM rounds_players WHERE roundId IN (SELECT roundId FROM rounds WHERE gameId = :gameId)")
    suspend fun deleteScoresForGame(gameId: UUID)

    @Transaction
    suspend fun deleteRoundsAndScoresForGame(gameId: UUID) {
        deleteScoresForGame(gameId)
        deleteRoundsForGame(gameId)
    }

    // End of delete game section

    @Query("DELETE FROM rounds_players WHERE roundId = :roundId")
    suspend fun deleteScores(roundId: UUID)

    @Query("DELETE FROM rounds WHERE roundId = :roundId")
    suspend fun deleteRound(roundId: UUID)

    @Query("UPDATE players SET numberOfWonGames = numberOfWonGames + 1 WHERE playerId = :playerId")
    suspend fun incrementWinCounter(playerId: UUID)

    @Query("UPDATE players SET numberOfGamesPlayed = numberOfGamesPlayed + 1 WHERE playerId = :playerId")
    suspend fun incrementGamesPlayed(playerId: UUID)


    @Insert
    suspend fun insertRound(round: Round)

    @Insert
    suspend fun insertPlayerScore(playerScore: Score)

    @Insert
    suspend fun insertFinalScore(finalScore: FinalScore)

    @Update
    suspend fun updatePlayer(player: Player)

    @Update
    suspend fun updateGame(game: Game)

    @Insert
    suspend fun addGame(game: Game)

    @Insert
    suspend fun addPlayer(player: Player)

    @Insert
    suspend fun insertGamePlayerCrossRef(gamePlayerCrossRef: GamePlayerCrossRef)
}