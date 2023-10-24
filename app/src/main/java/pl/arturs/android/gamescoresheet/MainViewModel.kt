package pl.arturs.android.gamescoresheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import pl.arturs.android.gamescoresheet.database.FinalScore
import pl.arturs.android.gamescoresheet.database.Game
import pl.arturs.android.gamescoresheet.database.Player
import pl.arturs.android.gamescoresheet.database.TotalScoreItem
import java.util.Date
import java.util.UUID

class MainViewModel : ViewModel() {

    private val appRepository = AppRepository.get()

    lateinit var gameBoardId: UUID

    private val _games: MutableStateFlow<List<Game>> = MutableStateFlow(emptyList())
    val games: StateFlow<List<Game>>
        get() = _games.asStateFlow()

    private val _activePlayers: MutableStateFlow<List<Player>> = MutableStateFlow(emptyList())
    val activePlayers: StateFlow<List<Player>>
        get() = _activePlayers.asStateFlow()

    init {
        viewModelScope.launch {
            appRepository.getGames().collect() {
                _games.value = it
            }
        }

        viewModelScope.launch {
            appRepository.getActivePlayers().collect() {
                _activePlayers.value = it
            }
        }
    }

    fun removePlayer(player: Player) {
        val updatedPlayer = player.copy(isActive = false)
        viewModelScope.launch {
            appRepository.updatePlayer(updatedPlayer)
        }
    }

    fun selectPlayer(player: Player) {
        val updatedPlayer = player.copy(isSelected = !player.isSelected)
        viewModelScope.launch {
            appRepository.updatePlayer(updatedPlayer)
        }
    }

    fun insertFinalScore(finalScore: FinalScore) {
        viewModelScope.launch {
            appRepository.insertFinalScore(finalScore)
        }
    }

    fun insertRoundScore(roundName: String, scores: Map<UUID, Int>, roundCounter: Int, date: Date) {
        viewModelScope.launch {
            appRepository.insertRoundScore(gameBoardId, roundName, scores, roundCounter = roundCounter, date = date)
        }
    }

    fun updateGame(game: Game) {
        viewModelScope.launch {
            appRepository.updateGame(game)
        }
    }

    fun addGame(name: String, gameId: UUID) {
        val players = activePlayers.value.filter { it.isSelected }
        val game = Game(
            gameId = gameId,
            title = name,
            date = Date()
        )
        viewModelScope.launch {
            appRepository.insertGame(game, players)
        }
    }

    fun addPlayer(player: Player) {
        viewModelScope.launch {
            appRepository.insertPlayer(player)
        }
    }

    fun getTotalScoreItem(gameId: UUID): Flow<List<TotalScoreItem>> {
        return combine(
            appRepository.getPlayers(gameId),
            appRepository.getRounds(gameId)
        ) { players, rounds ->
            players.map { player ->
                val scores = rounds.map { roundScore ->
                    roundScore.playerScores.find { it.playerId == player.playerId }?.score ?: 0
                }
                val totalScore = scores.sum()
                val averageScore = if (scores.isNotEmpty()) { scores.average() } else 0.0
                TotalScoreItem(player, totalScore, averageScore)
            }
        }
    }

    fun deleteRound(roundId: UUID) {
        viewModelScope.launch {
            appRepository.deleteRoundWithPlayerScores(roundId)
        }
    }

    fun deleteGame(gameId: UUID) {
        viewModelScope.launch {
            appRepository.deleteGame(gameId)
        }
    }

    fun incrementGamesPlayed(playerId: UUID) {
        viewModelScope.launch {
            appRepository.incrementGamesPlayed(playerId)
        }
    }

    fun incrementWinCounter(playerId: UUID) {
        viewModelScope.launch {
            appRepository.incrementWinCounter(playerId)
        }
    }
}