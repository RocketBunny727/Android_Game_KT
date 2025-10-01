package com.example.androidgamekt.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.androidgamekt.model.*

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 7

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MenuFragment()
            1 -> GameFragment()
            2 -> SettingsFragment()
            3 -> RecordsFragment()
            4 -> AuthorsFragment()
            5 -> RulesFragment()
            6 -> SignUpFragment()
            else -> MenuFragment()
        }
    }
}