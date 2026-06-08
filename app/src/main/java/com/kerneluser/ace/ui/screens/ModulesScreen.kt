package com.kerneluser.ace.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.components.*
import com.kerneluser.ace.ui.theme.AppColors
import com.kerneluser.ace.utils.Module
import com.kerneluser.ace.utils.ModuleUtils
import com.kerneluser.ace.utils.RootUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var isRooted by remember { mutableStateOf(false) }
    val modules = remember { mutableStateListOf<Module>() }
    var dialog by remember { mutableStateOf<DialogAction?>(null) }

    LaunchedEffect(Unit) {
        val s = RootUtils.checkRoot()
        isRooted = s.isRooted
        if (isRooted) { modules.clear(); modules.addAll(ModuleUtils.getModules()) }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("模块管理", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Bg)) },
        containerColor = AppColors.Bg
    ) { padding ->
        if (isLoading) { LoadingView(Modifier.padding(padding)) }
        else if (!isRooted) { EmptyState(Icons.Default.Shield, "需要 Root 权限", modifier = Modifier.padding(padding)) }
        else if (modules.isEmpty()) { EmptyState(Icons.Outlined.Extension, "暂无模块", "在 /data/adb/modules/ 下未找到 Magisk 模块", Modifier.padding(padding)) }
        else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${modules.size} 个模块", color = AppColors.TextSecondary, fontSize = 13.sp)
                modules.forEachIndexed { i, m ->
                    SurfaceCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text("v${m.version} · ${m.author}", color = AppColors.TextSecondary, fontSize = 12.sp)
                                }
                                StatusBadge(if (m.enabled) "已启用" else "已禁用", if (m.enabled) AppColors.Success else AppColors.TextMuted)
                            }
                            if (m.description.isNotBlank()) {
                                Text(m.description, color = AppColors.TextSecondary, fontSize = 13.sp, maxLines = 2)
                            }
                            HorizontalDivider(0.5.dp, AppColors.Divider)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(if (m.enabled) "已启用" else "已禁用", color = AppColors.TextSecondary, fontSize = 13.sp)
                                    Switch(
                                        checked = m.enabled,
                                        onCheckedChange = { en ->
                                            if (en) ModuleUtils.enableModule(m.id) else ModuleUtils.disableModule(m.id)
                                            modules[i] = m.copy(enabled = en)
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Surface, checkedTrackColor = AppColors.Primary, uncheckedThumbColor = AppColors.Surface, uncheckedTrackColor = AppColors.Divider)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilledTonalButton(onClick = { dialog = DialogAction.Remove(i, m) }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = AppColors.Warning.copy(alpha = 0.12f), contentColor = AppColors.Warning), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text("卸载", fontSize = 12.sp) }
                                    FilledTonalButton(onClick = { dialog = DialogAction.ForceDel(i, m) }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = AppColors.Error.copy(alpha = 0.1f), contentColor = AppColors.Error), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text("强制删除", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialog != null) {
        val d = dialog!!
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(if (d is DialogAction.Remove) "卸载模块" else "强制删除模块") },
            text = { Text(if (d is DialogAction.Remove) "确定要卸载 \"${d.module.name}\" 吗？它将在下次重启后移除。" else "确定要立即删除 \"${d.module.name}\" 吗？此操作不可撤销！") },
            confirmButton = { TextButton(onClick = { if (d is DialogAction.Remove) { ModuleUtils.removeModule(d.module.id); modules.removeAt(d.index) } else { ModuleUtils.forceDelete(d.module.id); modules.removeAt(d.index) }; dialog = null }, colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error)) { Text("确认") } },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("取消") } },
            containerColor = AppColors.Surface
        )
    }
}

sealed class DialogAction {
    abstract val index: Int
    abstract val module: Module
    data class Remove(override val index: Int, override val module: Module) : DialogAction()
    data class ForceDel(override val index: Int, override val module: Module) : DialogAction()
}