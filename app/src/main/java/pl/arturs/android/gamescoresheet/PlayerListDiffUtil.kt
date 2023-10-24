package pl.arturs.android.gamescoresheet

import androidx.recyclerview.widget.DiffUtil
import pl.arturs.android.gamescoresheet.database.Player

class PlayerListDiffUtil(
    private val oldList: List<Player>,
    private val newList: List<Player>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].playerId == newList[newItemPosition].playerId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].playerId != newList[newItemPosition].playerId -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].numberOfWonGames != newList[newItemPosition].numberOfWonGames -> false
            oldList[oldItemPosition].numberOfGamesPlayed != newList[newItemPosition].numberOfGamesPlayed -> false
            oldList[oldItemPosition].isActive != newList[newItemPosition].isActive -> false
            oldList[oldItemPosition].isSelected != newList[newItemPosition].isSelected -> false
            else -> true
        }
    }
}