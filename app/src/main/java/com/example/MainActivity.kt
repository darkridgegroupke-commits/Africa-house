package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HoaAppShell
import com.example.ui.state.HoaShellViewModel
import com.example.ui.theme.HouseOfAfricaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val shellViewModel: HoaShellViewModel = viewModel()
            val uiState by shellViewModel.uiState.collectAsStateWithLifecycle()

            HouseOfAfricaTheme(themePreference = uiState.themePreference) {
                HoaAppShell(
                    uiState = uiState,
                    viewModel = shellViewModel,
                )
            }
        }
    }
}
