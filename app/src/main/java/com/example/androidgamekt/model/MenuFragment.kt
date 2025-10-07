package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R
import androidx.viewpager2.widget.ViewPager2
import android.widget.Spinner
import android.widget.ArrayAdapter
import com.example.androidgamekt.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val spPlayers = view.findViewById<Spinner>(R.id.spPlayers)

        val repository = GameRepository(requireContext())

        CoroutineScope(Dispatchers.Main).launch {
            val players = withContext(Dispatchers.IO) { repository.getAllPlayers() }
            val items = players.map { "${it.id}: ${it.fullName}" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spPlayers.adapter = adapter
            if (players.isNotEmpty()) {
                selectedPlayerId = players.first().id
            }
            spPlayers.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedPlayerId = players[position].id
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            })
        }

        btnRegister.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 6 // SignUpFragment
        }

        btnStartGame.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 1
        }

        return view
    }
}