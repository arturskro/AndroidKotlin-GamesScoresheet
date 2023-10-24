package pl.arturs.android.gamescoresheet.database

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import pl.arturs.android.gamescoresheet.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

@Entity(tableName = "games")
data class Game(
    @PrimaryKey val gameId: UUID,
    val title: String,
    val date: Date,
    val isFinished: Boolean = false
) {
    val formattedDate: String
        get() {
            val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            return localDateTime.format(formatter)
        }
}

@Entity(tableName = "players")
data class Player(
    @PrimaryKey val playerId: UUID = UUID.randomUUID(),
    val name: String,
    val numberOfGamesPlayed: Int = 0,
    val numberOfWonGames: Int = 0,
    val isActive: Boolean = true,
    val isSelected: Boolean = false
)

@Entity(tableName = "games_players", primaryKeys = ["gameId", "playerId"], indices = [Index("playerId"), Index("gameId")])
data class GamePlayerCrossRef(
    val gameId: UUID,
    val playerId: UUID
)

@Entity(tableName = "winners")
data class FinalScore(
    val gameId: UUID,
    val playerName: String,
    val finalScore: Int,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Entity(tableName = "rounds")
data class Round(
    @PrimaryKey val roundId: UUID,
    val gameId: UUID,
    val roundName: String,
    val date: Date,
    val roundCounter: Int = 1
)

@Entity(tableName = "rounds_players", primaryKeys = ["roundId", "playerId"])
data class Score(
    val roundId: UUID,
    val playerId: UUID,
    val score: Int
)

@DatabaseView("SELECT rounds_players.roundId, players.name AS playerName, players.playerId, rounds_players.score FROM rounds_players JOIN players ON rounds_players.playerId = players.playerId")
data class PlayerScore(
    val roundId: UUID,
    val playerId: UUID,
    val playerName: String,
    val score: Int
)

data class RoundScore(
    @Embedded val round: Round,
    @Relation(
        parentColumn = "roundId",
        entityColumn = "roundId",
        entity = PlayerScore::class
    )
    val playerScores: List<PlayerScore>
)

data class TotalScoreItem(
    // From DB
    val player: Player,

    // Calculated fields
    var totalScore: Int,
    var averageScore: Double,

    // Other fields
    var editTextScore: Int = 0,
    var cup: Int = R.color.white,
    var progress: Int = 0
)

data class GameItem(
    @Embedded val game: Game,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "gameId",
        entity = FinalScore::class
    )
    val playerScoreList: List<FinalScore>,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "playerId",
        associateBy = Junction(GamePlayerCrossRef::class),
        entity = Player::class
    )
    val playerList: List<Player>
)