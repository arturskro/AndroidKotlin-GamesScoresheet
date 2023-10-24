package pl.arturs.android.gamescoresheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.arturs.android.gamescoresheet.database.FinalScore
import pl.arturs.android.gamescoresheet.database.Game
import pl.arturs.android.gamescoresheet.database.Round
import pl.arturs.android.gamescoresheet.database.RoundScore
import pl.arturs.android.gamescoresheet.databinding.FragmentScoreBoardBinding
import java.util.Date
import java.util.UUID

class ScoreBoardFragment : Fragment() {

    private val appRepository = AppRepository.get()
    private val mainVm: MainViewModel by activityViewModels()

    private var _binding: FragmentScoreBoardBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "Cannot access binding because it is null. Is the view visible?" }

    private var rounds: List<RoundScore> = emptyList()
    private var dateOfRound: Date? = null
    private lateinit var currentBoardGame: Game
    private lateinit var gameBoardId : UUID

    // RecyclerViews
    private lateinit var totalScoresRecyclerView: RecyclerView
    private lateinit var roundScoresRecyclerView: RecyclerView

    // Adapters
    private lateinit var totalScoresAdapter: TotalScoresAdapter
    private lateinit var roundScoresAdapter: RoundScoresAdapter

    // RoundName EditText
    private lateinit var roundNameEditText: EditText

    // Loading Bars
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScoreBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingProgressBar = binding.loadingProgressBar
        roundNameEditText = binding.etRoundName

        gameBoardId = mainVm.gameBoardId
        loadGame(gameBoardId)

        binding.btBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btSave.setOnClickListener {
            val roundName = roundNameEditText.text.toString()
            val scores: MutableMap<UUID, Int> = mutableMapOf()
            val lastCounter = rounds.firstOrNull()?.let { it.round.roundCounter + 1 } ?: 1
            var date = Date()

            for (playerScore in totalScoresAdapter.playerScores) {
                scores[playerScore.player.playerId] = playerScore.editTextScore
                playerScore.editTextScore = 0
            }

            if(dateOfRound != null) {
                date = dateOfRound as Date
                dateOfRound = null
            }

            if (roundName.isBlank()) {
                roundNameEditText.hint?.toString()?.let {
                    if (it.isNotBlank()) {
                        mainVm.insertRoundScore(it, scores, lastCounter, date)
                    }
                }
            } else {
                mainVm.insertRoundScore(roundName, scores, lastCounter, date)
            }

            // Clearing RoundName EditText
            roundNameEditText.text?.clear()
        }

        binding.btEndGame.setOnClickListener {
            endGameDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadGame(gameId: UUID) {
        lifecycleScope.launch {
            val game = appRepository.getGame(gameId).first()
            currentBoardGame = game
            binding.mainTitle.text = getString(R.string.header_game, game.title)

            if (!currentBoardGame.isFinished) {
                binding.btEndGame.visibility = View.VISIBLE
                binding.btSave.visibility = View.VISIBLE
                roundNameEditText.visibility = View.VISIBLE
            } else {
                binding.labelEnterRoundScore.text = getString(R.string.label_game_finished)
            }

            setupRecyclerViews()
            loadTotalScoreItems(gameBoardId)
            loadScores(gameBoardId)
        }
    }

    private fun loadScores(gameId: UUID) {
        lifecycleScope.launch {
            appRepository.getRounds(gameId).collect { items ->
                val oldRounds = rounds.toList()
                rounds = items

                // Set editText hint round name
                roundNameEditText.hint = getString(R.string.dlabel_round_name_suggested, rounds.firstOrNull()?.let { it.round.roundCounter + 1 } ?: 1)

                // Update recyclerview
                roundScoresAdapter.updateRoundScoreList(rounds)
                if (oldRounds.size < rounds.size) {
                   roundScoresRecyclerView.scrollToPosition(0)
                }
            }
        }
    }

    private fun loadTotalScoreItems(gameId: UUID) {
        lifecycleScope.launch {
            mainVm.getTotalScoreItem(gameId).collect {items ->
                totalScoresAdapter.updateData(items)
                loadingProgressBar.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerViews() {
        totalScoresRecyclerView = binding.playerScoreRecyclerView
        roundScoresRecyclerView = binding.scoreRecyclerView

        totalScoresAdapter = TotalScoresAdapter()
        roundScoresAdapter = RoundScoresAdapter(::deleteRoundItemDialog, ::editRoundItem, currentBoardGame.isFinished)

        totalScoresRecyclerView.layoutManager = LinearLayoutManager(context)
        roundScoresRecyclerView.layoutManager = LinearLayoutManager(context)

        totalScoresRecyclerView.adapter = totalScoresAdapter
        roundScoresRecyclerView.adapter = roundScoresAdapter

        val helper = ItemTouchHelper(totalScoresAdapter.TouchCallback())
        helper.attachToRecyclerView(totalScoresRecyclerView)
    }

    private fun deleteRoundItemDialog(round: Round) {
        val context = requireContext()
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(context.getString(R.string.dialog_remove_round, round.roundName))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.dialog_remove)) { dialog, _ ->
                mainVm.deleteRound(round.roundId)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun endGameDialog() {
        val context = requireContext()
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(context.getString(R.string.dialog_end_game))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.dialog_end)) { dialog, _ ->
                endGame()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun endGame() {
        val game = currentBoardGame.copy(isFinished = true)
        val winnerId = totalScoresAdapter.playerScores.maxByOrNull { it.totalScore }?.player?.playerId

        for (ps in totalScoresAdapter.playerScores) {
            val finalScore = FinalScore(gameBoardId, ps.player.name, ps.totalScore)
            mainVm.insertFinalScore(finalScore)
            mainVm.incrementGamesPlayed(ps.player.playerId)
        }

        if (winnerId != null) { mainVm.incrementWinCounter(winnerId) }
        mainVm.updateGame(game)

        findNavController().popBackStack()
    }

    private fun editRoundItem(roundScore: RoundScore) {
        dateOfRound = roundScore.round.date
        roundNameEditText.setText(roundScore.round.roundName)

        for (playerScore in roundScore.playerScores) {
            val totalScoreItem = totalScoresAdapter.playerScores.find { it.player.playerId == playerScore.playerId }
            totalScoreItem?.editTextScore = playerScore.score
        }

        mainVm.deleteRound(roundScore.round.roundId)
    }

}