package com.mashvpn.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashvpn.android.ui.theme.*
import com.mashvpn.android.ui.viewmodel.MainViewModel

@Composable
fun LicenseScreen(viewModel: MainViewModel) {
    var licenseInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBase)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Shield Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(BrandIndigo, BrandEmerald)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "مش وی پی ان",
                style = Typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )

            Text(
                text = "برای استفاده، لایسنس اشتراک خود را وارد کنید",
                style = Typography.bodyMedium,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // License Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "کد لایسنس (MASH-XXXX-XXXX-XXXX):",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = licenseInput,
                        onValueChange = { licenseInput = it.uppercase() },
                        placeholder = {
                            Text(
                                text = "MASH-XXXX-XXXX-XXXX",
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = BrandIndigoLight)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let {
                                    licenseInput = it.trim().uppercase()
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = TextGray)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                viewModel.activateLicense(licenseInput)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandIndigo,
                            unfocusedBorderColor = DarkBorderLight,
                            focusedTextColor = BrandEmeraldLight,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = DarkBase,
                            unfocusedContainerColor = DarkBase
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.activateLicense(licenseInput)
                        },
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandEmerald,
                            disabledContainerColor = DarkBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "بررسی و فعال‌سازی اشتراک",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Support and Telegram links
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Veuca"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("پشتیبانی @Veuca", style = Typography.bodyMedium, color = BrandIndigoLight)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/mashhvpn"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorderLight))),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("کانال @mashhvpn", style = Typography.bodyMedium, color = AccentCyan)
                }
            }
        }
    }
}
