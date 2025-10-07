package com.example.androidgamekt.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.androidgamekt.R
import com.example.androidgamekt.data.ScoreWithPlayer

class RecordsAdapter(private val scores: List<ScoreWithPlayer>) : BaseAdapter() {
    override fun getCount(): Int = scores.size
    override fun getItem(position: Int): Any = scores[position]
    override fun getItemId(position: Int): Long = scores[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.item_record, parent, false)
        val score = scores[position]
        val playerName = view.findViewById<TextView>(R.id.tvPlayerName)
        val scoreValue = view.findViewById<TextView>(R.id.tvScore)

        playerName.text = score.playerName
        val difficultyName = listOf("Лёгкий", "Средний", "Сложный", "Дезинсектор").getOrElse(score.difficulty) { "?" }
        scoreValue.text = "Счёт: ${score.score}, Сложность: $difficultyName"

        return view
    }
}