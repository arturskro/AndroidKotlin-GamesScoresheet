package pl.arturs.android.gamescoresheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pl.arturs.android.gamescoresheet.database.Player
import pl.arturs.android.gamescoresheet.databinding.FragmentPlayerListBinding

class PlayerListFragment : Fragment() {

    private val mainVm: MainViewModel by activityViewModels()
    private lateinit var playerAdapter: PlayerAdapter

    private var _binding: FragmentPlayerListBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "Cannot access binding because it is null. Is the view visible?" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerListBinding.inflate(inflater, container, false)
        binding.playerRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Adapter
        playerAdapter = PlayerAdapter(::removePlayerDialog, ::selectPlayer)
        binding.playerRecyclerView.adapter = playerAdapter

        // Update Fragment with new data
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainVm.activePlayers.collect { players ->
                    // Update Adapter
                    playerAdapter.setData(players)
                    // Update Selected Players counter
                    val selectedPlayersCount = players.count { it.isSelected }
                    binding.selectedPlayersTv.text = getString(R.string.dlabel_selected_players, selectedPlayersCount)
                }
            }
        }

        binding.addPlayerBt.setOnClickListener {
            val playerName = binding.enterNameEt.text.toString().trim()
            if (playerName.isNotEmpty()) {
                addNewPlayer(playerName)
                binding.enterNameEt.text?.clear()
            } else {
                Toast.makeText(requireContext(), getString(R.string.toast_empty_player_name), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun removePlayerDialog(player: Player){
        val context = requireContext()
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(context.getString(R.string.dialog_remove_player, player.name))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.dialog_remove)) { dialog, _ ->
                mainVm.removePlayer(player)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun addNewPlayer(playerName: String) {
        val player = Player(name = playerName)
        mainVm.addPlayer(player)
    }

    private fun selectPlayer(player: Player) {
        mainVm.selectPlayer(player)
    }
}