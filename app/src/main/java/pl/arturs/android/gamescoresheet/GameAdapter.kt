package pl.arturs.android.gamescoresheet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import pl.arturs.android.gamescoresheet.database.Game
import pl.arturs.android.gamescoresheet.database.GameItem
import pl.arturs.android.gamescoresheet.databinding.ListItemGameBinding
import pl.arturs.android.gamescoresheet.databinding.ListItemGameRowBinding
import java.util.UUID

class GameAdapter(
    private val onGameClicked: (gameId: UUID) -> Unit,
    private val onDeleteClicked: (game: Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.MyViewHolder>() {

    private var games: List<GameItem> = emptyList()

    inner class MyViewHolder(private val binding: ListItemGameBinding) : ViewHolder(binding.root) {
        private val date = binding.gameDate
        private val title = binding.gameTitle
        private val status = binding.status
        private val participantsContainer = binding.participantsContainer
        private val deleteButton = binding.deleteGameBt

        fun bind(gameItem: GameItem) {
            date.text = gameItem.game.formattedDate
            title.text = gameItem.game.title

            val sortedPlayerScores = gameItem.playerScoreList.sortedByDescending { it.finalScore }

            if (!gameItem.game.isFinished) {
                status.text = itemView.context.getString(R.string.label_not_finished)

                participantsContainer.removeAllViews()

                gameItem.playerList.forEachIndexed { index, player ->
                    val rowBinding = ListItemGameRowBinding.inflate(LayoutInflater.from(itemView.context), participantsContainer, true)
                    rowBinding.playerInfoRow.text = itemView.context.getString(R.string.dlabel_winner_game_not_finished, index + 1, player.name)
                }
            } else {
                status.text = itemView.context.getString(R.string.label_finished)

                participantsContainer.removeAllViews()

                sortedPlayerScores.forEachIndexed { index, player ->
                    val rowBinding = ListItemGameRowBinding.inflate(LayoutInflater.from(itemView.context), participantsContainer, true)
                    val scoreText = itemView.context.resources.getQuantityString(R.plurals.dlabel_player_score, player.finalScore, player.finalScore)
                    rowBinding.playerInfoRow.text = itemView.context.getString(R.string.dlabel_winner_game_finished, index + 1, player.playerName, scoreText)

                    when(index) {
                        0 -> rowBinding.cupIv.setColorFilter(ContextCompat.getColor(itemView.context, R.color.cup_gold))
                        1 -> rowBinding.cupIv.setColorFilter(ContextCompat.getColor(itemView.context, R.color.cup_silver))
                        2 -> rowBinding.cupIv.setColorFilter(ContextCompat.getColor(itemView.context, R.color.cup_bronze))
                    }
                }
            }

            itemView.setOnClickListener {
                onGameClicked(gameItem.game.gameId)
            }

            deleteButton.setOnClickListener {
                onDeleteClicked(gameItem.game)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val game = games[position]
        holder.bind(game)
    }

    override fun getItemCount() = games.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateGameList(gameList: List<GameItem>) {
        games = gameList
        notifyDataSetChanged()
    }
}
