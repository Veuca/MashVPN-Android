package com.mashvpn.android.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mashvpn.android.ui.theme.*
import com.mashvpn.android.ui.viewmodel.MainViewModel

@Composable
fun SettingsDialog(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = { viewModel.setSettingsDialogVisible(false) }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorderLight, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "مشخصات حساب کاربری مش وی پی ان",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Divider(color = DarkBorder)

                // Info Rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkBase)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نام خریدار:", style = Typography.bodyMedium, color = TextGray)
                        Text(uiState.customerName, style = Typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کد لایسنس:", style = Typography.bodyMedium, color = TextGray)
                        Text(uiState.licenseKey, style = Typography.bodyMedium, fontFamily = FontFamily.Monospace, color = BrandEmeraldLight)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تعداد دستگاه مجاز:", style = Typography.bodyMedium, color = TextGray)
                        Text("${uiState.maxDevices} دستگاه", style = Typography.bodyMedium, color = TextWhite)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("اعتبار باقیمانده:", style = Typography.bodyMedium, color = TextGray)
                        Text("${uiState.remainingDays} روز", style = Typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandEmerald)
                    }
                }

                // Telegram Buttons
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Veuca"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پشتیبانی تلگرام @Veuca", style = Typography.titleMedium)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/mashhvpn"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorderLight)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("کانال تلگرام @mashhvpn", style = Typography.titleMedium, color = AccentCyan)
                }

                // Logout Button
                OutlinedButton(
                    onClick = {
                        viewModel.setSettingsDialogVisible(false)
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentRose.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خروج از حساب / تغییر لایسنس", style = Typography.titleMedium, color = AccentRose)
                }
            }
        }
    }
}
