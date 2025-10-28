package com.example.androidgamekt.di

import com.example.androidgamekt.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    viewModel { GameViewModel() }
}


