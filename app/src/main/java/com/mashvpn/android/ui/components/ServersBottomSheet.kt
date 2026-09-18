package com.mashvpn.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashvpn.android.ui.theme.*
import com.mashvpn.android.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersBottomSheet(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredNodes = uiState.nodes.filter {
        it.cleanTitle.contains(searchQuery, ignoreCase = true) ||
        it.countryName.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.setServersBottomSheetVisible(false) },
        containerColor = DarkCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorderLight) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لیست سرورهای فعال مش وی پی ان",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                IconButton(onClick = { viewModel.refreshPing() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandIndigoLight)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در کشور یا نام سرور...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandIndigo,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkBase,
                    unfocusedContainerColor = DarkBase,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Server List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(bottom = 20.dp)
            ) {
                items(filteredNodes) { node ->
                    val isSelected = node.id == uiState.selectedNode?.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) DarkCardHover else DarkBase)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) BrandEmerald else DarkBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectNode(node) }
                            .padding(14.dp)
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
                                Text(text = node.flagEmoji, fontSize = 24.sp)

                                Column {
                                    Text(
                                        text = node.cleanTitle,
                                        style = Typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${node.countryName} • پروتکل ${node.protocol.uppercase()}",
                                        style = Typography.bodyMedium,
                                        color = TextGray
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (node.pingMs > 0) {
                                    val pingColor = when {
                                        node.pingMs < 100 -> BrandEmerald
                                        node.pingMs < 200 -> AccentAmber
                                        else -> AccentRose
                                    }
                                    Text(
                                        text = "${node.pingMs}ms",
                                        style = Typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = pingColor
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
