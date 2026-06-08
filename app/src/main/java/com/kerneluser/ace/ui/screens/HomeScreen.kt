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
import com.kerneluser.ace.ui.theme.LocalAceColors
import com.kerneluser.ace.utils.DeviceInfo
import com.kerneluser.ace.utils.RootUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val c = LocalAceColors.current
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
                        Text("Ace Kernel", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("v1.0.00", color = c.textMuted, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)
            )
        },
        containerColor = c.bg
    ) { padding ->
        if (isLoading) {
            LoadingView(Modifier.padding(padding))
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SurfaceCard {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(
                            if (rootStatus.isRooted) Icons.Outlined.VerifiedUser else Icons.Outlined.Cancel,
                            null,
                            tint = if (rootStatus.isRooted) c.success else c.error,
                            modifier = Modifier.size(44.dp)
                        )
                        Column {
                            Text(
                                if (rootStatus.isRooted) "Root 权限已获取" else "Root 权限未获取",
                                color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp
                            )
                            if (rootStatus.isRooted && rootStatus.rootType != "Unknown") {
                                Text("${rootStatus.rootType} ${rootStatus.version}", color = c.textSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                SectionHeader("设备信息")
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        InfoRow("机型", "${deviceInfo["brand"] ?: ""} ${deviceInfo["model"] ?: ""}")
                        InfoRow("Android 版本", deviceInfo["androidVersion"] ?: "-")
                        InfoRow("内核版本", deviceInfo["kernelVersion"] ?: "-")
                        val se = deviceInfo["selinuxStatus"] ?: "-"
                        InfoRow("SELinux", se, iconColor = if (se.equals("Enforcing", true)) c.success else c.warning)
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
                    Box(Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(8.dp))) {
                        Text(deviceInfo["fingerprint"] ?: "-", color = c.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}