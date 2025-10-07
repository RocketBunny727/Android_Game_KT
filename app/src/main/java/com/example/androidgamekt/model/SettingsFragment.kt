package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val sbGameSpeed = view.findViewById<SeekBar>(R.id.sbGameSpeed)
        val tvGameSpeed = view.findViewById<TextView>(R.id.tvGameSpeedValue)
        val sbMaxRoaches = view.findViewById<SeekBar>(R.id.sbMaxRoaches)
        val tvMaxRoaches = view.findViewById<TextView>(R.id.tvMaxRoachesValue)
        val sbBonusInterval = view.findViewById<SeekBar>(R.id.sbBonusInterval)
        val tvBonusInterval = view.findViewById<TextView>(R.id.tvBonusIntervalValue)
        val sbRoundDuration = view.findViewById<SeekBar>(R.id.sbRoundDuration)
        val tvRoundDuration = view.findViewById<TextView>(R.id.tvRoundDurationValue)

        val difficultyNames = mapOf(
            0 to "Лёгкий",
            1 to "Средний",
            2 to "Сложный",
            3 to "Дезинсектор"
        )

        // Initialize
        sbGameSpeed.max = 3
        sbGameSpeed.progress = GameSettings.instance.difficulty
        tvGameSpeed.text = "Сложность: ${difficultyNames[GameSettings.instance.difficulty]}"

        sbMaxRoaches.max = 20
        sbBonusInterval.max = 60
        sbRoundDuration.max = 180

        val maxRoaches = GameSettings.instance.overrideMaxBugs ?: 0
        val bonusInterval = GameSettings.instance.overrideBonusIntervalSec ?: 0
        val roundDuration = GameSettings.instance.overrideRoundDurationSec ?: 0

        sbMaxRoaches.progress = maxRoaches
        tvMaxRoaches.text = if (maxRoaches == 0) "Макс. жуков: по сложности" else "Макс. жуков: $maxRoaches"

        sbBonusInterval.progress = bonusInterval
        tvBonusInterval.text = if (bonusInterval == 0) "Интервал бонусов: по сложности" else "Интервал бонусов: ${bonusInterval}s"

        sbRoundDuration.progress = roundDuration
        tvRoundDuration.text = if (roundDuration == 0) "Длительность раунда: по сложности" else "Длительность раунда: ${roundDuration}s"

        fun bumpVersion() { GameSettings.instance.version++ }

        sbGameSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvGameSpeed.text = "Сложность: ${difficultyNames[progress]}"
                if (GameSettings.instance.difficulty != progress) {
                    GameSettings.instance.difficulty = progress
                    bumpVersion()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbMaxRoaches.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newValue = if (progress == 0) null else progress
                if (GameSettings.instance.overrideMaxBugs != newValue) {
                    GameSettings.instance.overrideMaxBugs = newValue
                    bumpVersion()
                }
                tvMaxRoaches.text = if (progress == 0) "Макс. жуков: по сложности" else "Макс. жуков: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbBonusInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newValue = if (progress == 0) null else progress
                if (GameSettings.instance.overrideBonusIntervalSec != newValue) {
                    GameSettings.instance.overrideBonusIntervalSec = newValue
                    bumpVersion()
                }
                tvBonusInterval.text = if (progress == 0) "Интервал бонусов: по сложности" else "Интервал бонусов: ${progress}s"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbRoundDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newValue = if (progress == 0) null else progress
                if (GameSettings.instance.overrideRoundDurationSec != newValue) {
                    GameSettings.instance.overrideRoundDurationSec = newValue
                    bumpVersion()
                }
                tvRoundDuration.text = if (progress == 0) "Длительность раунда: по сложности" else "Длительность раунда: ${progress}s"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return view
    }
}