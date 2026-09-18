package com.mashvpn.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.mashvpn.android.ui.screens.HomeScreen
import com.mashvpn.android.ui.screens.LicenseScreen
import com.mashvpn.android.ui.screens.SplashScreen
import com.mashvpn.android.ui.theme.DarkBase
import com.mashvpn.android.ui.theme.MashVpnTheme
import com.mashvpn.android.ui.viewmodel.MainViewModel
import com.mashvpn.android.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MashVpnTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = DarkBase
                    ) {
                        val uiState by viewModel.uiState.collectAsState()

                        // Toast Error / Success Handlers
                        LaunchedEffect(uiState.errorMessage) {
                            uiState.errorMessage?.let { msg ->
                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                                viewModel.clearMessages()
                            }
                        }

                        LaunchedEffect(uiState.successMessage) {
                            uiState.successMessage?.let { msg ->
                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                                viewModel.clearMessages()
                            }
                        }

                        when (uiState.currentScreen) {
                            is ScreenState.Splash -> SplashScreen()
                            is ScreenState.LicenseEntry -> LicenseScreen(viewModel = viewModel)
                            is ScreenState.Home -> HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
