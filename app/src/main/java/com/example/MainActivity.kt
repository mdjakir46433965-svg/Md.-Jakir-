package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.ChannelRepository
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SportsViewModel
import com.example.ui.viewmodel.SportsViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Database holding structures
    val database = AppDatabase.getDatabase(applicationContext)
    val dao = database.channelDao()
    val repository = ChannelRepository(dao)
    
    // Initialize ViewModel with custom factory instantiations
    val sportsViewModelFactory = SportsViewModelFactory(repository)
    val sportsViewModel = ViewModelProvider(this, sportsViewModelFactory)[SportsViewModel::class.java]

    setContent {
      MyApplicationTheme {
        HomeScreen(viewModel = sportsViewModel)
      }
    }
  }
}
