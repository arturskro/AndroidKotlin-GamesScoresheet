package pl.arturs.android.gamescoresheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pl.arturs.android.gamescoresheet.database.Game
import pl.arturs.android.gamescoresheet.database.Round
import pl.arturs.android.gamescoresheet.databinding.FragmentGameListBinding
import java.util.UUID

class GameListFragment : Fragment() {

    private val mainVm: MainViewModel by activityViewModels()
    private val appRepository = AppRepository.get()

    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var gameAdapter: GameAdapter

    private var _binding: FragmentGameListBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "Cannot access binding because it is null. Is the view visible?" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameListBinding.inflate(inflater, container, false)
        binding.gameRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btPlayersList.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_gameListFragment_to_playerListFragment)
        )

        gameRecyclerView = binding.gameRecyclerView
        gameAdapter = GameAdapter(::onGameClicked, ::deleteGameDialog)

        gameRecyclerView.layoutManager = LinearLayoutManager(context)
        gameRecyclerView.adapter = gameAdapter

        fetchActivePlayerList()
        fetchGameList()

        binding.addGameBt.setOnClickListener {
            val selectedPlayers = mainVm.activePlayers.value.filter { it.isSelected }

            if (selectedPlayers.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_select_players),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                addNewGame()
            }
        }

    }

    private fun onGameClicked(gameId : UUID) {
        mainVm.gameBoardId = gameId
        findNavController().navigate(R.id.action_gameListFragment_to_scoreBoardFragment)
    }

    private fun deleteGameDialog(game: Game) {
        val context = requireContext()
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(context.getString(R.string.dialog_remove_game, game.title))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.dialog_remove)) { dialog, _ ->
                deleteGame(game.gameId)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun deleteGame(gameId: UUID) {
        mainVm.deleteGame(gameId)
    }

    private fun fetchGameList() {
        viewLifecycleOwner.lifecycleScope.launch {
            appRepository.getGameListItem().collect {
                gameAdapter.updateGameList(it)
            }
        }
    }

    private fun fetchActivePlayerList() {
        viewLifecycleOwner.lifecycleScope.launch {
            mainVm.activePlayers.collect { players ->
                val selectedPlayers = players.filter { it.isSelected }

                if (selectedPlayers.isEmpty()) {
                    binding.PlayerListTv.text = getString(R.string.label_empty_player_list)
                } else {
                    val selectedPlayerNames = selectedPlayers.map { it.name }
                    val playerList = selectedPlayerNames.joinToString(", ")

                    binding.PlayerListTv.text =
                        getString(
                            R.string.dlabel_players_at_the_table,
                            selectedPlayers.count(),
                            playerList
                        )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addNewGame() {
        var gameName = binding.enterNameEt.text.toString().trim()
        binding.enterNameEt.text?.clear()
        val gameId = UUID.randomUUID()

        if (gameName.isEmpty()) {
            gameName = getString(R.string.label_default_game_name)
        }

        mainVm.addGame(gameName, gameId)
        mainVm.gameBoardId = gameId
        findNavController().navigate(R.id.action_gameListFragment_to_scoreBoardFragment)

    }
}