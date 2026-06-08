package com.kerneluser.ace.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.components.*
import com.kerneluser.ace.ui.theme.AppColors
import com.kerneluser.ace.utils.DeviceInfo
import com.kerneluser.ace.utils.RootUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var rootStatus by remember { mutableStateOf(RootUtils.RootStatus(false, "", "")) }
    var deviceInfo by remember { mutableStateOf(emptyMap<String, String>()) }

    LaunchedEffect(Unit) {
        rootStatus = RootUtils.checkRoot()
        deviceInfo = DeviceInfo.get()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ace Kernel", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("v1.0.00", color = AppColors.TextMuted, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Bg)
            )
        },
        containerColor = AppColors.Bg
    ) { padding ->
        if (isLoading) {
            LoadingView(Modifier.padding(padding))
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Root status card
                SurfaceCard {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(
                            if (rootStatus.isRooted) Icons.Outlined.VerifiedUser else Icons.Outlined.Cancel,
                            null,
                            tint = if (rootStatus.isRooted) AppColors.Success else AppColors.Error,
                            modifier = Modifier.size(44.dp)
                        )
                        Column {
                            Text(
                                if (rootStatus.isRooted) "Root 权限已获取" else "Root 权限未获取",
                                color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp
                            )
                            if (rootStatus.isRooted && rootStatus.rootType != "Unknown") {
                                Text(
                                    "${rootStatus.rootType} ${rootStatus.version}",
                                    color = AppColors.TextSecondary, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Device info section
                SectionHeader("设备信息")
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        InfoRow("机型", "${deviceInfo["brand"] ?: ""} ${deviceInfo["model"] ?: ""}")
                        InfoRow("Android 版本", deviceInfo["androidVersion"] ?: "-")
                        InfoRow("内核版本", deviceInfo["kernelVersion"] ?: "-")
                        val se = deviceInfo["selinuxStatus"] ?: "-"
                        InfoRow("SELinux", se, iconColor = if (se.equals("Enforcing", true)) AppColors.Success else AppColors.Warning)
                        InfoRow("安全补丁", deviceInfo["securityPatch"] ?: "-")
                        InfoRow("构建号", deviceInfo["display"] ?: "-")
                    }
                }

                SectionHeader("系统详情")
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        InfoRow("制造商", deviceInfo["manufacturer"] ?: "-")
                        InfoRow("设备代号", deviceInfo["device"] ?: "-")
                        InfoRow("硬件", deviceInfo["hardware"] ?: "-")
                        InfoRow("主板", deviceInfo["board"] ?: "-")
                        InfoRow("Bootloader", deviceInfo["bootloader"] ?: "-")
                        InfoRow("构建类型", deviceInfo["buildType"] ?: "-")
                        InfoRow("构建时间", deviceInfo["buildTime"] ?: "-")
                    }
                }

                SectionHeader("指纹")
                SurfaceCard {
                    Box(Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(8.dp)).then(Modifier.fillMaxWidth())) {
                        Text(
                            deviceInfo["fingerprint"] ?: "-",
                            color = AppColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
}