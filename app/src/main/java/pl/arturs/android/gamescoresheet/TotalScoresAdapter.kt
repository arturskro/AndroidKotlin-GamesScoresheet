package pl.arturs.android.gamescoresheet

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import pl.arturs.android.gamescoresheet.database.Player
import pl.arturs.android.gamescoresheet.database.TotalScoreItem
import pl.arturs.android.gamescoresheet.databinding.ItemPlayerScoreBinding
import java.util.Collections
import kotlin.math.abs

class TotalScoresAdapter() : RecyclerView.Adapter<TotalScoresAdapter.PlayerScoreHolder>() {

    var playerScores: List<TotalScoreItem> = emptyList()
    private var highestScore: Int = 0
    private var lowestScore: Int = 0

    inner class PlayerScoreHolder(private val binding: ItemPlayerScoreBinding) : RecyclerView.ViewHolder(binding.root) {
        private val playerScoreInfo = binding.PlayerScoreInfo
        private val playerScoreProgressBar = binding.ScoreProgressBar
        private val scoreEt = binding.playerScoreEditText
        private val playerNameTv = binding.PlayerName
        private var cupIv = binding.cupIv

        lateinit var player: Player

        init {
            scoreEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val score = s?.toString()?.toIntOrNull() ?: 0
                    val playerScore = playerScores.find { it.player.playerId == player.playerId }
                    playerScore?.editTextScore = score
                }
            })
        }

        fun bind(playerScore: TotalScoreItem) {
            playerNameTv.text = playerScore.player.name
            player = playerScore.player

            if (playerScore.editTextScore != 0)
                scoreEt.setText(playerScore.editTextScore.toString())
            else
                scoreEt.text?.clear()

            val formattedAverageScore = String.format("%.1f", playerScore.averageScore)
            val playerScoreInfoText = itemView.context.getString(R.string.dlabel_player_score_info, playerScore.totalScore, formattedAverageScore)
            playerScoreInfo.text = playerScoreInfoText

            playerScoreProgressBar.max = highestScore + abs(lowestScore)

            val progress: Int = if (playerScore.totalScore >= 0) playerScore.totalScore + abs(lowestScore)
            else playerScore.totalScore + abs(lowestScore)

            animateProgressBar(playerScoreProgressBar, playerScore.progress, progress)
            playerScore.progress = progress

            cupIv.setColorFilter(ContextCompat.getColor(itemView.context, playerScore.cup))
        }
    }

    inner class TouchCallback : ItemTouchHelper.Callback() {
        private var draggedViewHolder: RecyclerView.ViewHolder? = null

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val draggedItemIndex = viewHolder.bindingAdapterPosition
            val targetIndex = target.bindingAdapterPosition

            Collections.swap(playerScores, draggedItemIndex, targetIndex)
            notifyItemMoved(draggedItemIndex, targetIndex)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                draggedViewHolder = viewHolder
                viewHolder?.itemView?.setBackgroundColor(
                    ContextCompat.getColor(viewHolder.itemView.context, R.color.fragment_background)
                )
            } else {
                draggedViewHolder?.itemView?.setBackgroundColor(Color.WHITE)
                draggedViewHolder = null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerScoreHolder {
        val binding = ItemPlayerScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerScoreHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerScoreHolder, position: Int) {
        val playerScore = playerScores[position]
        holder.bind(playerScore)
    }

    override fun getItemCount(): Int {
        return playerScores.size
    }

    private fun animateProgressBar(progressBar: ProgressBar, currentProgress: Int, targetProgress: Int) {
        val animation = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, targetProgress)
        animation.duration = 1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newPlayerScores: List<TotalScoreItem>) {
        playerScores = if (playerScores.isNotEmpty()) updateList(playerScores, newPlayerScores) else newPlayerScores

        highestScore = playerScores.maxByOrNull { it.totalScore }?.totalScore ?: 0
        lowestScore = playerScores.minByOrNull { it.totalScore }?.totalScore ?: 0

        setCups()
        notifyDataSetChanged()

    }

    private fun updateList(oldList: List<TotalScoreItem>, newList: List<TotalScoreItem>): List<TotalScoreItem> {
        val updatedList = oldList.toMutableList()

        for (newItem in newList) {
            val item = updatedList.find { it.player.playerId == newItem.player.playerId }
            if (item != null) {
                item.totalScore = newItem.totalScore
                item.averageScore = newItem.averageScore
            }
        }
        return updatedList
    }

    private fun setCups() {
        val tempList = playerScores.toMutableList()
        tempList.sortByDescending { it.totalScore }

        var currentMedal = 0
        var currentScore = tempList.firstOrNull()?.totalScore

        for (item in tempList) {
            if (item.totalScore != currentScore) {
                currentMedal += 1
                currentScore = item.totalScore
            }

            val playerScoreModel = playerScores.find { it.player.playerId == item.player.playerId }
            playerScoreModel?.cup = when (currentMedal) {
                0 -> R.color.cup_gold
                1 -> R.color.cup_silver
                2 -> R.color.cup_bronze
                else -> R.color.white
            }
        }
    }
}