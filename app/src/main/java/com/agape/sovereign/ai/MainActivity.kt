package com.agape.sovereign.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.agape.sovereign.ai.ui.PrivacyAppScreen
import com.agape.sovereign.ai.ui.SecureViewModel
import com.agape.sovereign.ai.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SecureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PrivacyAppScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
