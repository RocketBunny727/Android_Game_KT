package com.example.androidgamekt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.androidgamekt.data.GameRepository
import com.example.androidgamekt.util.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        val repository = GameRepository(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val players = repository.getAllPlayers()
                Log.d("MainActivity", "Игроков в БД: ${players.size}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка БД: ${e.message}")
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Меню"
                1 -> tab.text = "Игра"
                2 -> tab.text = "Настройки"
                3 -> tab.text = "Рекорды"
                4 -> tab.text = "Авторы"
                5 -> tab.text = "Правила"
                6 -> tab.text = "Регистрация"
            }
        }.attach()
    }
}