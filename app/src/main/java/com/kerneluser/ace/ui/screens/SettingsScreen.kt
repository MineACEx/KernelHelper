package com.kerneluser.ace.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
fun SettingsScreen(isDark: Boolean = false, onToggleDark: (Boolean) -> Unit = {}) {
    val c = LocalAceColors.current
    val ctx = LocalContext.current
    var isRooted by remember { mutableStateOf(false) }
    var rootType by remember { mutableStateOf("") }
    var rootVersion by remember { mutableStateOf("") }
    var showMounts by remember { mutableStateOf(false) }; var showServices by remember { mutableStateOf(false) }; var showNetwork by remember { mutableStateOf(false) }
    var mounts by remember { mutableStateOf<String?>(null) }; var services by remember { mutableStateOf<String?>(null) }; var network by remember { mutableStateOf<String?>(null) }
    var rebootDialog by remember { mutableStateOf<RebootType?>(null) }
    var showCacheCleared by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val s = RootUtils.checkRoot(); isRooted = s.isRooted; rootType = s.rootType; rootVersion = s.version
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置", color = c.textPrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)) },
        containerColor = c.bg
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader("应用信息")
            SurfaceCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ace Kernel", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        StatusBadge(if (isRooted) "已 Root" else "未 Root", if (isRooted) c.success else c.textMuted)
                    }
                    Text("v1.0.00", color = c.textSecondary, fontSize = 13.sp)
                    if (isRooted && rootType != "Unknown") Text("$rootType ${rootVersion}", color = c.textSecondary, fontSize = 13.sp)
                }
            }

            SectionHeader("外观")
            SurfaceCard {
                Column(Modifier.fillMaxWidth()) {
                    SwitchRow(icon = Icons.Outlined.DarkMode, label = "深色模式", desc = "切换深色界面主题", checked = isDark, onChecked = onToggleDark, c = c)
                }
            }

            SectionHeader("通知")
            SurfaceCard {
                Column(Modifier.fillMaxWidth()) {
                    SwitchRow(icon = Icons.Outlined.Notifications, label = "Root 请求通知", desc = "应用请求 Root 权限时通知", checked = notificationsEnabled, onChecked = { notificationsEnabled = it }, c = c)
                }
            }

            if (isRooted) {
                SectionHeader("内核信息")
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        InfoRow("CPU 核心", RootUtils.getCpuCores())
                        InfoRow("CPU 调度", RootUtils.getCpuGovernor())
                        InfoRow("频率范围", RootUtils.getCpuFreqRange())
                        InfoRow("温度", RootUtils.getThermalTemp())
                        InfoRow("运行时间", RootUtils.getUptime())
                    }
                }

                SectionHeader("系统操作")
                SurfaceCard {
                    Column(Modifier.fillMaxWidth()) {
                        RebootRow("重启系统", "正常重启设备") { rebootDialog = RebootType.System }
                        HorizontalDivider(thickness = 0.5.dp, color = c.divider)
                        RebootRow("重启到 Recovery", "进入恢复模式") { rebootDialog = RebootType.Recovery }
                        HorizontalDivider(thickness = 0.5.dp, color = c.divider)
                        RebootRow("重启到 Bootloader", "进入引导加载程序") { rebootDialog = RebootType.Bootloader }
                    }
                }
            }

            SectionHeader("数据管理")
            SurfaceCard {
                Column(Modifier.fillMaxWidth()) {
                    SimpleRow(Icons.Outlined.DeleteSweep, "清除缓存", "清除应用本地缓存数据") { ctx.cacheDir.deleteRecursively(); showCacheCleared = true }
                    HorizontalDivider(thickness = 0.5.dp, color = c.divider)
                    SimpleRow(Icons.Outlined.Storage, "打开应用信息", "查看系统应用详情页") {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:${ctx.packageName}")
                        ctx.startActivity(intent)
                    }
                }
            }

            SectionHeader("系统详情")
            SurfaceCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    ExpandRow("挂载点", showMounts, { showMounts = !showMounts; if (showMounts && mounts == null) mounts = DeviceInfo.getMountPoints() }, mounts)
                    HorizontalDivider(thickness = 0.5.dp, color = c.divider)
                    ExpandRow("运行服务", showServices, { showServices = !showServices; if (showServices && services == null) services = DeviceInfo.getRunningServices() }, services)
                    HorizontalDivider(thickness = 0.5.dp, color = c.divider)
                    ExpandRow("网络统计", showNetwork, { showNetwork = !showNetwork; if (showNetwork && network == null) network = DeviceInfo.getNetworkStats() }, network)
                }
            }

            SectionHeader("关于")
            SurfaceCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, null, tint = c.textMuted, modifier = Modifier.size(20.dp))
                    Text("Ace Kernel 是一款强大的 Android 内核管理工具，支持 Magisk / KernelSU / APatch，提供 Root 管理、内核模块管理、Superuser 权限控制、分区刷写与备份等功能。", color = c.textSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }

    if (rebootDialog != null) {
        val r = rebootDialog!!
        AlertDialog(
            onDismissRequest = { rebootDialog = null },
            title = { Text("确认重启") },
            text = { Text("确定要${when (r) { RebootType.System -> "重启系统"; RebootType.Recovery -> "重启到 Recovery"; RebootType.Bootloader -> "重启到 Bootloader" }}吗？") },
            confirmButton = { TextButton(onClick = { RootUtils.execSu(when (r) { RebootType.System -> "reboot"; RebootType.Recovery -> "reboot recovery"; RebootType.Bootloader -> "reboot bootloader" }); rebootDialog = null }, colors = ButtonDefaults.textButtonColors(contentColor = c.error)) { Text("确认") } },
            dismissButton = { TextButton(onClick = { rebootDialog = null }) { Text("取消") } },
            containerColor = c.surface
        )
    }

    if (showCacheCleared) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showCacheCleared = false
        }
        Snackbar(modifier = Modifier.padding(16.dp), action = { TextButton(onClick = { showCacheCleared = false }) { Text("好的", color = c.primary) } }) { Text("缓存已清除", color = c.textPrimary) }
    }
}

@Composable
fun RebootRow(label: String, desc: String, onClick: () -> Unit) {
    val c = LocalAceColors.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column { Text(label, color = c.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp); Text(desc, color = c.textSecondary, fontSize = 12.sp) }
        Icon(Icons.Outlined.Refresh, null, tint = c.textMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ExpandRow(title: String, expanded: Boolean, onToggle: () -> Unit, content: String?) {
    val c = LocalAceColors.current
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = c.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, tint = c.textMuted)
        }
        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            if (content != null) Box(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(8.dp)).background(c.bgSecondary).padding(12.dp)) {
                Text(content, color = c.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SwitchRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, desc: String, checked: Boolean, onChecked: (Boolean) -> Unit, c: com.kerneluser.ace.ui.theme.AceThemeColors) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.textMuted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) { Text(label, color = c.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp); Text(desc, color = c.textSecondary, fontSize = 12.sp) }
        Switch(checked = checked, onCheckedChange = onChecked, colors = SwitchDefaults.colors(checkedThumbColor = c.surface, checkedTrackColor = c.primary, uncheckedThumbColor = c.surface, uncheckedTrackColor = c.divider))
    }
}

@Composable
fun SimpleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, desc: String, onClick: () -> Unit) {
    val c = LocalAceColors.current
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.textMuted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) { Text(label, color = c.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp); Text(desc, color = c.textSecondary, fontSize = 12.sp) }
        Icon(Icons.Outlined.ChevronRight, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
    }
}

enum class RebootType { System, Recovery, Bootloader }