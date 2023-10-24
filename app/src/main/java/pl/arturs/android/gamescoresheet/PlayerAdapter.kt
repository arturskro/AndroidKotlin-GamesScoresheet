package pl.arturs.android.gamescoresheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import pl.arturs.android.gamescoresheet.database.Player
import pl.arturs.android.gamescoresheet.databinding.ListItemPlayerBinding

class PlayerAdapter(
    private val removePlayer: (player: Player) -> Unit,
    private val selectPlayer: (player: Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.MyViewHolder>() {

    private var oldPlayerList = emptyList<Player>()

    inner class MyViewHolder(binding: ListItemPlayerBinding) : ViewHolder(binding.root) {
        val name = binding.playerNameTv
        val numberOfGamesPlayed = binding.playerGamesPlayedTv
        val numberOfGamesWon = binding.playerGamesWonTv
        val removePlayerButton = binding.removePlayerBt
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            binding = ListItemPlayerBinding.inflate(LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val player = oldPlayerList[position]

        holder.name.text = player.name
        holder.numberOfGamesWon.text = player.numberOfWonGames.toString()
        holder.numberOfGamesPlayed.text = player.numberOfGamesPlayed.toString()

        if (player.isSelected) {
            holder.itemView.findViewById<AppCompatImageView>(R.id.player_selected).visibility = View.VISIBLE
        } else {
            holder.itemView.findViewById<AppCompatImageView>(R.id.player_selected).visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            selectPlayer(player)
        }

        holder.removePlayerButton.setOnClickListener {
            removePlayer(player)
        }
    }

    override fun getItemCount() = oldPlayerList.size

    fun setData(newPlayerList : List<Player>) {
        val diffUtil = PlayerListDiffUtil(oldPlayerList, newPlayerList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        oldPlayerList = newPlayerList
        diffResults.dispatchUpdatesTo(this)
    }
}