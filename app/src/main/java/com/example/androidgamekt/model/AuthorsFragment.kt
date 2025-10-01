package com.example.androidgamekt.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.androidgamekt.R

class AuthorsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_authors, container, false)
        val listView = view.findViewById<ListView>(R.id.listViewAuthors)

        val authors = listOf(
            Author("Гузенков Семён", R.drawable.author1),
            Author("Номоконов Данил", R.drawable.author2)
        ).map { author ->
            author.copy(photoResId = if (resources.getIdentifier("author${author.photoResId - R.drawable.author1 + 1}", "drawable", requireContext().packageName) == 0)
                R.drawable.ic_launcher_background else author.photoResId)
        }

        val adapter = AuthorsAdapter(authors)
        listView.adapter = adapter
        return view
    }

    private inner class AuthorsAdapter(private val authors: List<Author>) : BaseAdapter() {
        override fun getCount(): Int = authors.size
        override fun getItem(position: Int): Any = authors[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_author, parent, false)
            val author = authors[position]
            view.findViewById<TextView>(R.id.tvAuthorName).text = author.name
            view.findViewById<ImageView>(R.id.ivAuthorPhoto).setImageResource(author.photoResId)
            return view
        }
    }
}