package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.VpnDatabase
import com.example.data.VpnRepository
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Database & Repository
        val database = VpnDatabase.getDatabase(applicationContext)
        val repository = VpnRepository(database.vpnProfileDao())
        
        // Initialize View Model using Factory pattern
        val viewModel = ViewModelProvider(
            this,
            VpnViewModel.Factory(repository)
        )[VpnViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
