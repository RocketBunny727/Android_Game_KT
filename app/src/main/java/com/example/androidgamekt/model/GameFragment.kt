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
import android.view.ViewTreeObserver
import androidx.viewpager2.widget.ViewPager2
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.media.MediaPlayer
import android.widget.Button
import com.example.androidgamekt.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel


data class Bug(val id: Int, var x: Float, var y: Float, var speedX: Float, var speedY: Float, val type: BugType)

enum class BugType {
    NORMAL, POISON, COIN, BONUS_TILT
}

class GameFragment : Fragment() {
    private val bugs = mutableListOf<Bug>()
    private val gameViewModel: GameViewModel by activityViewModel()
    // moved to ViewModel: timeElapsed & bonus state
    private var nextBugId = 1
    private var observedSettingsVersion = GameSettings.instance.version
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gameLayout: ViewGroup
    private lateinit var tvScore: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnRestart: Button
    private lateinit var repository: GameRepository

    private var tiltEnabled = false
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var tiltX: Float = 0f
    private var tiltY: Float = 0f
    private var mediaPlayer: MediaPlayer? = null
    private var goldRate: Double = 0.0
    private var lastGoldSpawnTime = 0L

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
        btnRestart = view.findViewById(R.id.btnRestart)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        gameLayout.setOnClickListener {
            gameViewModel.addScore(-5)
        }

        btnRestart.setOnClickListener {
            btnRestart.visibility = View.GONE
            gameViewModel.fullReset()
            resetGameScene()
            startGameLoop()
        }

        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (gameLayout.width > 0 && gameLayout.height > 0) {
                    gameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    resetGameScene()
                    startGameLoop()
                }
            }
        }
        gameLayout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        // Observe score changes to update UI
        gameViewModel.score.observe(viewLifecycleOwner) { value ->
            tvScore.text = "Очки: $value"
        }
        // Keep local tiltEnabled mirror for sensor wiring
        gameViewModel.tiltEnabled.observe(viewLifecycleOwner) { enabled ->
            tiltEnabled = enabled
        }

        return view
    }

    private fun resetGameScene() {
        handler.removeCallbacksAndMessages(null)
        bugs.clear()
        gameLayout.removeAllViews()
        nextBugId = 1
        tvScore.text = "Очки: ${gameViewModel.score.value ?: 0}"
        observedSettingsVersion = GameSettings.instance.version
        stopSound()
        // load last known gold rate from shared prefs
        val stored = com.example.androidgamekt.util.GoldRateStore.load(requireContext()).first
        if (stored > 0) goldRate = stored
    }

    private fun resolveConfig(): DifficultyConfig {
        val base = difficultySettings[GameSettings.instance.difficulty] ?: difficultySettings[0]!!
        val maxBugs = GameSettings.instance.overrideMaxBugs ?: base.maxBugs
        val bonusInterval = GameSettings.instance.overrideBonusIntervalSec ?: base.bonusInterval
        val roundDuration = GameSettings.instance.overrideRoundDurationSec ?: base.roundDuration
        return base.copy(maxBugs = maxBugs, bonusInterval = bonusInterval, roundDuration = roundDuration)
    }

    private fun startGameLoop() {
        val config = resolveConfig()

        handler.post(object : Runnable {
            override fun run() {
                if (observedSettingsVersion != GameSettings.instance.version) {
                    resetGameScene()
                    startGameLoop()
                    return
                }

                val timeElapsed = gameViewModel.timeElapsedMs.value ?: 0L
                if (timeElapsed < config.roundDuration * 1000L) {
                    btnRestart.visibility = View.GONE
                    val spawnIntervalMs = (400L / config.speedMultiplier).toLong().coerceAtLeast(120L)
                    if (bugs.size < config.maxBugs && (timeElapsed - gameViewModel.lastSpawnTimeMs) >= spawnIntervalMs) {
                        val isCoinTime = (timeElapsed - gameViewModel.lastCoinTimeMs) >= config.bonusInterval * 1000L
                        val isPoison = Random.nextFloat() < config.poisonBugChance && (timeElapsed - gameViewModel.lastPoisonTimeMs) >= 1000L
                        if (isCoinTime) gameViewModel.markCoin(timeElapsed)
                        if (isPoison) gameViewModel.markPoison(timeElapsed)
                        val typeToSpawn = when {
                            isCoinTime -> BugType.COIN
                            isPoison -> BugType.POISON
                            else -> BugType.NORMAL
                        }
                        addBug(typeToSpawn)
                        gameViewModel.markSpawn(timeElapsed)
                    }

                    val hasTiltBonus = bugs.any { it.type == BugType.BONUS_TILT }
                    if (!hasTiltBonus && (timeElapsed - gameViewModel.lastFixedBonusTimeMs) >= 15000L) {
                        addBug(BugType.BONUS_TILT)
                        gameViewModel.markFixedBonus(timeElapsed)
                    }

                    // Golden bug every 20 seconds
                    if ((timeElapsed - gameViewModel.lastGoldSpawnTimeMs) >= 20000L) {
                        addBug(BugType.COIN) // reuse COIN type for golden bug image/logic
                        gameViewModel.markGold(timeElapsed)
                    }

                    updateBugs()
                    gameViewModel.tick(16L)
                    val newElapsed = (gameViewModel.timeElapsedMs.value ?: 0L)
                    tvTime.text = "Время: ${(config.roundDuration - newElapsed / 1000).toInt()} сек"
                    handler.postDelayed(this, 16L)
                } else {
                    if (MenuFragment.selectedPlayerId != 0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                repository.insertScore(
                                    ScoreEntity(
                                        playerId = MenuFragment.selectedPlayerId,
                                        score = gameViewModel.score.value ?: 0,
                                        difficulty = GameSettings.instance.difficulty,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                    handler.removeCallbacks(this)
                    btnRestart.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun addBug(type: BugType) {
        val config = resolveConfig()
        val drawableRes = when (type) {
            BugType.NORMAL -> R.drawable.bugs
            BugType.POISON -> R.drawable.bugs2
            BugType.COIN -> resources.getIdentifier("coin", "drawable", requireContext().packageName).let { if (it == 0) R.drawable.ic_launcher_background else it }
            BugType.BONUS_TILT -> resources.getIdentifier("bonus", "drawable", requireContext().packageName).let { if (it == 0) R.drawable.ic_launcher_background else it }
        }.let { resId ->
            if (resources.getIdentifier(resources.getResourceEntryName(resId), "drawable", requireContext().packageName) == 0)
                R.drawable.ic_launcher_background else resId
        }

        val minDistance = 110f
        val position = findNonOverlappingPosition(minDistance, 30) ?: run {
            Pair(
                Random.nextFloat() * (gameLayout.width - 50),
                Random.nextFloat() * (gameLayout.height - 50)
            )
        }

        val bugView = ImageView(requireContext()).apply {
            setImageResource(drawableRes)
            layoutParams = ViewGroup.LayoutParams(100, 100)
            setOnClickListener {
                val tagId = tag as? Int
                if (tagId != null) {
                    bugs.removeAll { it.id == tagId }
                }
                when (type) {
                    BugType.NORMAL -> {
                        gameViewModel.addScore(10)
                    }
                    BugType.COIN -> {
                        val bonus = if (goldRate > 0) (goldRate / 10.0).toInt().coerceAtLeast(50) else 50
                        gameViewModel.addScore(bonus)
                    }
                    BugType.BONUS_TILT -> {
                        enableTiltControl()
                        playScream()
                    }
                    BugType.POISON -> {
                        gameViewModel.addScore(-20)
                    }
                }
                gameLayout.removeView(this)
            }
        }
        val id = nextBugId++
        bugView.tag = id
        val x = position.first
        val y = position.second
        val speedX = (Random.nextFloat() * 4 - 2) * config.speedMultiplier
        val speedY = (Random.nextFloat() * 4 - 2) * config.speedMultiplier
        bugs.add(Bug(id, x, y, speedX, speedY, type))
        bugView.x = x
        bugView.y = y
        gameLayout.addView(bugView)
    }

    private fun enableTiltControl() {
        tiltEnabled = true
        gameViewModel.setTiltEnabled(true)
        accelerometer?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun disableTiltControl() {
        tiltEnabled = false
        gameViewModel.setTiltEnabled(false)
        sensorManager?.unregisterListener(sensorListener)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!tiltEnabled) return
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                tiltX = -event.values[0]
                tiltY = event.values[1]
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun playScream() {
        stopSound()
        val resId = resources.getIdentifier("mellstroysvist", "raw", requireContext().packageName)
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(requireContext(), resId)
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
            mediaPlayer?.start()
        }
    }

    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun findNonOverlappingPosition(minDistance: Float, maxAttempts: Int): Pair<Float, Float>? {
        repeat(maxAttempts) {
            val x = Random.nextFloat() * (gameLayout.width - 50)
            val y = Random.nextFloat() * (gameLayout.height - 50)
            val isFarEnough = bugs.all { existing ->
                val dx = (existing.x + 50f / 2) - (x + 50f / 2)
                val dy = (existing.y + 50f / 2) - (y + 50f / 2)
                val distSq = dx * dx + dy * dy
                distSq >= minDistance * minDistance
            }
            if (isFarEnough) return Pair(x, y)
        }
        return null
    }

    private fun updateBugs() {
        val tiltAccel = if (tiltEnabled) 0.15f else 0f
        bugs.forEachIndexed { index, bug ->
            var newSpeedX = bug.speedX + tiltAccel * tiltX
            var newSpeedY = bug.speedY + tiltAccel * tiltY

            var newX = bug.x + newSpeedX
            var newY = bug.y + newSpeedY

            if (newX < 0 || newX > gameLayout.width - 50) newSpeedX = -newSpeedX
            if (newY < 0 || newY > gameLayout.height - 50) newSpeedY = -newSpeedY
            newX = newX.coerceIn(0f, (gameLayout.width - 50).toFloat())
            newY = newY.coerceIn(0f, (gameLayout.height - 50).toFloat())

            bugs[index] = bug.copy(x = newX, y = newY, speedX = newSpeedX, speedY = newSpeedY)
            val bugView = gameLayout.findViewWithTag<ImageView>(bug.id)
            bugView?.x = newX
            bugView?.y = newY
        }
    }

    override fun onResume() {
        super.onResume()
        if (tiltEnabled) {
            accelerometer?.let {
                sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorListener)
        stopSound()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        sensorManager?.unregisterListener(sensorListener)
        stopSound()
    }
}