package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R

class MenuFragment : Fragment() {
    companion object {
        var selectedPlayerId = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val btnStartGame = view.findViewById<Button>(R.id.btnStartGame)

        btnStartGame.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.viewPager, GameFragment())
                .commit()
        }

        return view
    }
}