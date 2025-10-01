package com.example.androidgamekt.model

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R
import com.example.androidgamekt.data.GameRepository
import com.example.androidgamekt.data.ScoreEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class Bug(val id: Int, var x: Float, var y: Float, var speedX: Float, var speedY: Float, val type: BugType)

enum class BugType {
    NORMAL, POISON, BONUS
}

class GameFragment : Fragment() {
    private val bugs = mutableListOf<Bug>()
    private var score = 0
    private var timeElapsed = 0L
    private var lastBonusTime = 0L
    private var lastPoisonTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gameLayout: ViewGroup
    private lateinit var tvScore: TextView
    private lateinit var tvTime: TextView
    private lateinit var repository: GameRepository
    private val difficultySettings = mapOf(
        0 to DifficultyConfig(1f, 5, 10, 60, 0.1f), // Лёгкий
        1 to DifficultyConfig(1.5f, 7, 8, 60, 0.2f), // Средний
        2 to DifficultyConfig(2f, 10, 6, 60, 0.3f), // Сложный
        3 to DifficultyConfig(3f, 15, 5, 60, 0.5f)  // Дезинсектор
    )

    data class DifficultyConfig(
        val speedMultiplier: Float,
        val maxBugs: Int,
        val bonusInterval: Int,
        val roundDuration: Int,
        val poisonBugChance: Float
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        repository = GameRepository(requireContext())
        gameLayout = view.findViewById(R.id.gameLayout)
        tvScore = view.findViewById(R.id.tvScore)
        tvTime = view.findViewById(R.id.tvTime)

        gameLayout.setOnClickListener {
            score = (score - 5).coerceAtLeast(0)
            tvScore.text = "Очки: $score"
        }

        startGameLoop()
        return view
    }

    private fun startGameLoop() {
        val config = difficultySettings[GameSettings.instance.difficulty]
            ?: difficultySettings[0]!!

        handler.post(object : Runnable {
            override fun run() {
                if (timeElapsed < config.roundDuration * 1000L) {
                    if (bugs.size < config.maxBugs) {
                        val isBonus = (timeElapsed - lastBonusTime) >= config.bonusInterval * 1000L
                        val isPoison = Random.nextFloat() < config.poisonBugChance && (timeElapsed - lastPoisonTime) >= 1000L
                        if (isBonus) lastBonusTime = timeElapsed
                        if (isPoison) lastPoisonTime = timeElapsed
                        addBug(isBonus, isPoison)
                    }
                    updateBugs()
                    timeElapsed += 16L
                    tvTime.text = "Время: ${(config.roundDuration - timeElapsed / 1000).toInt()} сек"
                    handler.postDelayed(this, 16L)
                } else {
                    if (MenuFragment.selectedPlayerId != 0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                repository.insertScore(
                                    ScoreEntity(
                                        playerId = MenuFragment.selectedPlayerId,
                                        score = score,
                                        difficulty = GameSettings.instance.difficulty,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                    handler.removeCallbacks(this)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.viewPager, MenuFragment())
                        .commit()
                }
            }
        })
    }

    private fun addBug(isBonus: Boolean, isPoison: Boolean) {
        val config = difficultySettings[GameSettings.instance.difficulty]
            ?: difficultySettings[0]!!
        val type = when {
            isBonus -> BugType.BONUS
            isPoison -> BugType.POISON
            else -> BugType.NORMAL
        }
        val drawableRes = when (type) {
            BugType.NORMAL -> R.drawable.bugs
            BugType.POISON -> R.drawable.bugs2
            BugType.BONUS -> R.drawable.coint
        }.let { resId ->
            if (resources.getIdentifier(resources.getResourceEntryName(resId), "drawable", requireContext().packageName) == 0)
                R.drawable.ic_launcher_background else resId
        }
        val bugView = ImageView(requireContext()).apply {
            setImageResource(drawableRes)
            layoutParams = ViewGroup.LayoutParams(100, 100)
            setOnClickListener {
                bugs.removeAll { it.id == tag }
                score = when (type) {
                    BugType.NORMAL -> score + 10
                    BugType.BONUS -> score + 50
                    BugType.POISON -> (score - 20).coerceAtLeast(0)
                }
                tvScore.text = "Очки: $score"
                gameLayout.removeView(this)
            }
        }
        val id = bugs.size + 1
        bugView.tag = id
        val x = Random.nextFloat() * (gameLayout.width - 50)
        val y = Random.nextFloat() * (gameLayout.height - 50)
        val speedX = (Random.nextFloat() * 4 - 2) * config.speedMultiplier
        val speedY = (Random.nextFloat() * 4 - 2) * config.speedMultiplier
        bugs.add(Bug(id, x, y, speedX, speedY, type))
        bugView.x = x
        bugView.y = y
        gameLayout.addView(bugView)
    }

    private fun updateBugs() {
        bugs.forEachIndexed { index, bug ->
            var newX = bug.x + bug.speedX
            var newY = bug.y + bug.speedY
            var newSpeedX = bug.speedX
            var newSpeedY = bug.speedY

            if (newX < 0 || newX > gameLayout.width - 50) newSpeedX = -bug.speedX
            if (newY < 0 || newY > gameLayout.height - 50) newSpeedY = -bug.speedY
            newX = newX.coerceIn(0f, (gameLayout.width - 50).toFloat())
            newY = newY.coerceIn(0f, (gameLayout.height - 50).toFloat())

            bugs[index] = bug.copy(x = newX, y = newY, speedX = newSpeedX, speedY = newSpeedY)
            val bugView = gameLayout.findViewWithTag<ImageView>(bug.id)
            bugView?.x = newX
            bugView?.y = newY
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}