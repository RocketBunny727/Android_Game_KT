package com.example.androidgamekt.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.androidgamekt.model.AuthorsFragment
import com.example.androidgamekt.model.RulesFragment
import com.example.androidgamekt.model.SettingsFragment
import com.example.androidgamekt.model.SignUpFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SignUpFragment()
            1 -> RulesFragment()
            2 -> AuthorsFragment()
            3 -> SettingsFragment()
            else -> SignUpFragment()
        }
    }
}