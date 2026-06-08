package com.kerneluser.ace.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun SettingsScreen() {
    var isRooted by remember { mutableStateOf(false) }
    var rootType by remember { mutableStateOf("") }
    var rootVersion by remember { mutableStateOf("") }

    var showMounts by remember { mutableStateOf(false) }
    var showServices by remember { mutableStateOf(false) }
    var showNetwork by remember { mutableStateOf(false) }
    var mounts by remember { mutableStateOf<String?>(null) }
    var services by remember { mutableStateOf<String?>(null) }
    var network by remember { mutableStateOf<String?>(null) }
    var rebootDialog by remember { mutableStateOf<RebootType?>(null) }

    LaunchedEffect(Unit) {
        val s = RootUtils.checkRoot(); isRooted = s.isRooted; rootType = s.rootType; rootVersion = s.version
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Bg)) },
        containerColor = AppColors.Bg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // App info
            Text("应用信息", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            SurfaceCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ace Kernel", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        StatusBadge(if (isRooted) "已 Root" else "未 Root", if (isRooted) AppColors.Success else AppColors.TextMuted)
                    }
                    Text("v1.0.00", color = AppColors.TextSecondary, fontSize = 13.sp)
                    if (isRooted && rootType != "Unknown") {
                        Text("$rootType ${rootVersion}", color = AppColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // Reboot actions
            if (isRooted) {
                Text("系统操作", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                SurfaceCard {
                    Column(Modifier.fillMaxWidth()) {
                        RebootRow("重启系统", "正常重启设备") { rebootDialog = RebootType.System }
                        HorizontalDivider(0.5.dp, AppColors.Divider)
                        RebootRow("重启到 Recovery", "进入恢复模式") { rebootDialog = RebootType.Recovery }
                        HorizontalDivider(0.5.dp, AppColors.Divider)
                        RebootRow("重启到 Bootloader", "进入引导加载程序") { rebootDialog = RebootType.Bootloader }
                    }
                }
            }

            // Expandable system details
            Text("系统详情", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            SurfaceCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    ExpandRow("挂载点", showMounts, {
                        showMounts = !showMounts
                        if (showMounts && mounts == null) mounts = DeviceInfo.getMountPoints()
                    }, mounts)
                    HorizontalDivider(0.5.dp, AppColors.Divider)
                    ExpandRow("运行服务", showServices, {
                        showServices = !showServices
                        if (showServices && services == null) services = DeviceInfo.getRunningServices()
                    }, services)
                    HorizontalDivider(0.5.dp, AppColors.Divider)
                    ExpandRow("网络统计", showNetwork, {
                        showNetwork = !showNetwork
                        if (showNetwork && network == null) network = DeviceInfo.getNetworkStats()
                    }, network)
                }
            }

            // About
            Text("关于", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            SurfaceCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, null, tint = AppColors.TextMuted, modifier = Modifier.size(20.dp))
                    Text("Ace Kernel 是一款强大的 Android 内核管理工具，提供 Root 管理、内核模块管理、Superuser 权限控制、分区刷写与备份等功能。", color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (rebootDialog != null) {
        val r = rebootDialog!!
        AlertDialog(
            onDismissRequest = { rebootDialog = null },
            title = { Text("确认重启") },
            text = { Text("确定要${when (r) { RebootType.System -> "重启系统"; RebootType.Recovery -> "重启到 Recovery"; RebootType.Bootloader -> "重启到 Bootloader" }}吗？") },
            confirmButton = { TextButton(onClick = { RootUtils.execSu(when (r) { RebootType.System -> "reboot"; RebootType.Recovery -> "reboot recovery"; RebootType.Bootloader -> "reboot bootloader" }); rebootDialog = null }, colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error)) { Text("确认") } },
            dismissButton = { TextButton(onClick = { rebootDialog = null }) { Text("取消") } },
            containerColor = AppColors.Surface
        )
    }
}

@Composable
fun RebootRow(label: String, desc: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(label, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp); Text(desc, color = AppColors.TextSecondary, fontSize = 12.sp) }
        Icon(Icons.Outlined.Refresh, null, tint = AppColors.TextMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ExpandRow(title: String, expanded: Boolean, onToggle: () -> Unit, content: String?) {
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, tint = AppColors.TextMuted)
        }
        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            if (content != null) {
                Box(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.BgSecondary).padding(12.dp)) {
                    Text(content, color = AppColors.TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

enum class RebootType { System, Recovery, Bootloader }