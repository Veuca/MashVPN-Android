package com.mashvpn.android.ui.screens

import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashvpn.android.ui.components.ServersBottomSheet
import com.mashvpn.android.ui.components.SettingsDialog
import com.mashvpn.android.ui.theme.*
import com.mashvpn.android.ui.viewmodel.MainViewModel
import com.mashvpn.android.vpn.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // VPN Permission Request Launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.toggleVpnConnection(context)
        }
    }

    val isConnected = uiState.vpnState is VpnState.Connected
    val isConnecting = uiState.vpnState is VpnState.Connecting

    Scaffold(
        containerColor = DarkBase,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subscription Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .border(1.dp, if (isConnected) BrandEmeraldDark else DarkBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isConnected) BrandEmerald else BrandIndigo,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "${uiState.remainingDays} روز اعتبار",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                // Settings & Account Button
                IconButton(
                    onClick = { viewModel.setSettingsDialogVisible(true) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextWhite)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Center: Connection Status & Live Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val statusTitle = when (uiState.vpnState) {
                    is VpnState.Connected -> "اتصال ایمن برقرار شد"
                    is VpnState.Connecting -> "در حال اتصال به سرور..."
                    is VpnState.Error -> "خطا در اتصال"
                    else -> "اتصال قطع است"
                }

                val statusSubtitle = when (uiState.vpnState) {
                    is VpnState.Connected -> "ترافیک دستگاه شما رمزنگاری شده است"
                    is VpnState.Connecting -> "لطفاً چند ثانیه شکیبا باشید"
                    else -> "برای اتصال روی دکمه زیر ضربه بزنید"
                }

                Text(
                    text = statusTitle,
                    style = Typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isConnected -> BrandEmeraldLight
                        isConnecting -> AccentAmber
                        else -> TextWhite
                    }
                )

                Text(
                    text = statusSubtitle,
                    style = Typography.bodyMedium,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (isConnected) {
                    val minutes = (uiState.connectionDurationSeconds / 60)
                    val seconds = (uiState.connectionDurationSeconds % 60)
                    val formattedTime = String.format("%02d:%02d", minutes, seconds)

                    Text(
                        text = formattedTime,
                        style = Typography.headlineLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = BrandEmerald,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Central Glowing Power Button
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isConnected || isConnecting) 1.12f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                // Background Pulsing Ring
                if (isConnected || isConnecting) {
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .scale(pulseScale)
                            .background(
                                color = if (isConnected) BrandEmerald.copy(alpha = 0.15f) else AccentAmber.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    )
                }

                val buttonBg by animateColorAsState(
                    targetValue = when {
                        isConnected -> BrandEmerald
                        isConnecting -> AccentAmber
                        else -> DarkCard
                    },
                    label = "btnColor"
                )

                val buttonBorder by animateColorAsState(
                    targetValue = when {
                        isConnected -> BrandEmeraldLight
                        isConnecting -> AccentAmber
                        else -> DarkBorderLight
                    },
                    label = "border"
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .border(3.dp, buttonBorder, CircleShape)
                        .clickable {
                            val vpnIntent = VpnService.prepare(context)
                            if (vpnIntent != null) {
                                vpnPermissionLauncher.launch(vpnIntent)
                            } else {
                                viewModel.toggleVpnConnection(context)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Connect",
                            tint = Color.White,
                            modifier = Modifier.size(68.dp)
                        )
                    }
                }
            }

            // Bottom: Server Selector Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .clickable { viewModel.setServersBottomSheetVisible(true) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.selectedNode?.flagEmoji ?: "🌐",
                            fontSize = 28.sp
                        )
                        Column {
                            Text(
                                text = uiState.selectedNode?.cleanTitle ?: "انتخاب سرور",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${uiState.selectedNode?.countryName ?: "هلند"} • ${uiState.selectedNode?.protocol?.uppercase() ?: "UDP"}",
                                style = Typography.bodyMedium,
                                color = TextGray
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.selectedNode?.pingMs != null && uiState.selectedNode?.pingMs!! > 0) {
                            val ping = uiState.selectedNode!!.pingMs
                            val pingColor = when {
                                ping < 100 -> BrandEmerald
                                ping < 200 -> AccentAmber
                                else -> AccentRose
                            }
                            Text(
                                text = "${ping}ms",
                                style = Typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = pingColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = TextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Servers Selection BottomSheet
        if (uiState.isServersBottomSheetVisible) {
            ServersBottomSheet(viewModel = viewModel)
        }

        // Settings / Account Dialog
        if (uiState.isSettingsDialogVisible) {
            SettingsDialog(viewModel = viewModel)
        }
    }
}
