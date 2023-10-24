package pl.arturs.android.gamescoresheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pl.arturs.android.gamescoresheet.database.Round
import pl.arturs.android.gamescoresheet.database.RoundScore
import pl.arturs.android.gamescoresheet.databinding.ItemRoundScoreBinding
import pl.arturs.android.gamescoresheet.databinding.ItemRoundScoreRowBinding
import kotlin.math.abs

class RoundScoresAdapter(
    private val deleteRoundDialog: (round: Round) -> Unit,
    private val editRoundItem: (roundScore: RoundScore) -> Unit,
    private val isGameFinished: Boolean
) : RecyclerView.Adapter<RoundScoresAdapter.ScoreHolder>() {

    private var rounds: List<RoundScore> = emptyList()
    private var highestScore: Int = 0
    private var lowestScore: Int = 0

    inner class ScoreHolder(private val binding: ItemRoundScoreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var roundName = binding.roundNameTv
        private val deleteRound = binding.deleteRoundBt
        private val editRound = binding.editRoundBt
        var scoreTable = binding.RoundScoreTable
        lateinit var roundScoreItem: RoundScore

        init {
            if(!isGameFinished) {
                deleteRound.visibility = View.VISIBLE
                editRound.visibility = View.VISIBLE
            }
            deleteRound.setOnClickListener {
                deleteRoundDialog(roundScoreItem.round)
            }

            editRound.setOnClickListener {
                editRoundItem(roundScoreItem)
            }
        }

        fun bind(round: RoundScore) {
            roundName.text = round.round.roundName
            roundScoreItem = round

            // Delete existing views
            scoreTable.removeAllViews()

            // Sort playerScores
            val sortedPlayerScores = round.playerScores.sortedByDescending { it.score }

            // Add new views to scoreTable
            for (playerScore in sortedPlayerScores) {
                val scoreRowBinding = ItemRoundScoreRowBinding.inflate(
                    LayoutInflater.from(itemView.context), scoreTable, true
                )

                scoreRowBinding.NameTv.text = playerScore.playerName

                val formattedScore = itemView.context.resources.getQuantityString(
                    R.plurals.dlabel_player_score, playerScore.score, playerScore.score
                )
                scoreRowBinding.ScoreTv.text = formattedScore

                // Set progress bar
                val progressBar = scoreRowBinding.ScoreProgressBar

                progressBar.max = highestScore + abs(lowestScore)
                if (playerScore.score >= 0) progressBar.progress = playerScore.score + abs(lowestScore)
                else progressBar.progress = playerScore.score + abs(lowestScore)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHolder {
        val binding = ItemRoundScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScoreHolder(binding)
    }

    override fun getItemCount(): Int {
        return rounds.size
    }

    override fun onBindViewHolder(holder: ScoreHolder, position: Int) {
        val round = rounds[position]
        calculateHighestAndLowestScores()
        holder.bind(round)
    }

    fun updateRoundScoreList(roundList: List<RoundScore>) {
        val diffResult = DiffUtil.calculateDiff(ScoreListDiffUtil(rounds, roundList))
        rounds = roundList
        diffResult.dispatchUpdatesTo(this)
    }

    private fun calculateHighestAndLowestScores() {
        if (rounds.isNotEmpty()) {
            val firstPlayerScore = rounds[0].playerScores.firstOrNull()

            if (firstPlayerScore != null) {
                highestScore = firstPlayerScore.score
                lowestScore = firstPlayerScore.score
            }

            for (round in rounds) {
                for (playerScore in round.playerScores) {
                    if (playerScore.score > highestScore) highestScore = playerScore.score
                    if (playerScore.score < lowestScore) lowestScore = playerScore.score
                }
            }
        }
    }
}