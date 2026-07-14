package com.agape.sovereign.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.agape.sovereign.ai.ai.InAppMcpServer
import com.agape.sovereign.ai.ui.PrivacyAppScreen
import com.agape.sovereign.ai.ui.SecureViewModel
import com.agape.sovereign.ai.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SecureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start the in-app hosted MCP server to support offline-only PWA connectivity on port 3001
        InAppMcpServer.start(3001)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    PrivacyAppScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the in-app server to release the port
        InAppMcpServer.stop()
    }
}
