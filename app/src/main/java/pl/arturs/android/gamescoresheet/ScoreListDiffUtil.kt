package pl.arturs.android.gamescoresheet

import androidx.recyclerview.widget.DiffUtil
import pl.arturs.android.gamescoresheet.database.RoundScore

class ScoreListDiffUtil(
    private val oldList: List<RoundScore>,
    private val newList: List<RoundScore>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].round.roundId == newList[newItemPosition].round.roundId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
    }
}