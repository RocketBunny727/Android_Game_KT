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

        sbGameSpeed.isEnabled = false
        sbMaxRoaches.isEnabled = false
        sbBonusInterval.isEnabled = false
        sbRoundDuration.isEnabled = false

        val difficultyNames = mapOf(
            0 to "Лёгкий",
            1 to "Средний",
            2 to "Сложный",
            3 to "Дезинсектор"
        )

        // Инициализация значений
        sbGameSpeed.progress = GameSettings.instance.difficulty
        tvGameSpeed.text = "Сложность: ${difficultyNames[GameSettings.instance.difficulty]}"
        sbMaxRoaches.progress = 0
        tvMaxRoaches.text = "Макс. жуков: (зависит от сложности)"
        sbBonusInterval.progress = 0
        tvBonusInterval.text = "Интервал бонусов: (зависит от сложности)"
        sbRoundDuration.progress = 0
        tvRoundDuration.text = "Длительность раунда: (зависит от сложности)"

        sbGameSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvGameSpeed.text = "Сложность: ${difficultyNames[progress]}"
                GameSettings.instance.difficulty = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return view
    }
}