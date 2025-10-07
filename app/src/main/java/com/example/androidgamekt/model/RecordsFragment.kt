package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R
import com.example.androidgamekt.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)
        val listView = view.findViewById<ListView>(R.id.listViewRecords)
        val repository = GameRepository(requireContext())

        CoroutineScope(Dispatchers.Main).launch {
            val scores = withContext(Dispatchers.IO) { repository.getScoresWithPlayerNames() }
            val adapter = RecordsAdapter(scores)
            listView.adapter = adapter
        }

        return view
    }
}