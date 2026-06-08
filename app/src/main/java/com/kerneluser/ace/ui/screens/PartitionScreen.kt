package com.kerneluser.ace.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.components.*
import com.kerneluser.ace.ui.theme.LocalAceColors
import com.kerneluser.ace.utils.PartitionUtils
import com.kerneluser.ace.utils.RootUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartitionScreen() {
    val c = LocalAceColors.current
    val ctx = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isRooted by remember { mutableStateOf(false) }
    var partitions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedPartition by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf("") }
    var showFlashDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex("_display_name")
                        if (idx >= 0) selectedImageName = cursor.getString(idx)
                    }
                }
            } catch (_: Exception) { selectedImageName = "unknown.img" }
        }
    }

    LaunchedEffect(Unit) {
        val s = RootUtils.checkRoot(); isRooted = s.isRooted
        if (isRooted) { partitions = parsePartitionNames(PartitionUtils.getPartitionList()) }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("分区管理", color = c.textPrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = c.bg)) },
        containerColor = c.bg
    ) { padding ->
        if (isLoading) { LoadingView(Modifier.padding(padding)) }
        else if (!isRooted) { EmptyState(Icons.Filled.Shield, "需要 Root 权限", modifier = Modifier.padding(padding)) }
        else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // ──── 刷写分区 ────
                SectionHeader("刷写分区")
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("目标分区", color = c.textSecondary, fontSize = 14.sp)
                            if (selectedPartition != null) StatusBadge(selectedPartition!!, c.primary)
                            else Text("未选择", color = c.textMuted, fontSize = 13.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { filePicker.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (selectedImageName.isEmpty()) "选择镜像文件" else selectedImageName, maxLines = 1, fontSize = 13.sp)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showFlashDialog = true },
                                enabled = selectedPartition != null && selectedImageUri != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = c.error, contentColor = c.surface)
                            ) { Text("刷写分区", fontWeight = FontWeight.Bold) }
                            OutlinedButton(
                                onClick = { showBackupDialog = true },
                                enabled = selectedPartition != null,
                                modifier = Modifier.weight(1f)
                            ) { Text("备份分区") }
                        }
                    }
                }

                // ──── 分区列表 ────
                SectionHeader("分区列表 (${partitions.size})")
                if (partitions.isEmpty()) {
                    Text("无法获取分区列表", color = c.textSecondary, fontSize = 14.sp)
                } else {
                    SurfaceCard {
                        Column(Modifier.fillMaxWidth().padding(4.dp)) {
                            partitions.forEachIndexed { index, name ->
                                val isSelected = selectedPartition == name
                                Surface(
                                    onClick = { selectedPartition = name },
                                    color = if (isSelected) c.primary.copy(alpha = 0.08f) else c.surface,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Icon(Icons.Outlined.DiscFull, null, tint = if (isSelected) c.primary else c.textMuted, modifier = Modifier.size(20.dp))
                                            Text(name, color = c.textPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        }
                                        if (isSelected) Icon(Icons.Outlined.CheckCircle, null, tint = c.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                if (index < partitions.size - 1) HorizontalDivider(thickness = 0.5.dp, color = c.divider)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }

    // ──── 刷写确认 ────
    if (showFlashDialog && selectedPartition != null && selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { showFlashDialog = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = c.error) },
            title = { Text("确认刷写") },
            text = { Text("即将刷写 ${selectedImageName} → /dev/block/by-name/$selectedPartition\n\n错误操作可能导致设备变砖！") },
            confirmButton = {
                TextButton(onClick = {
                    showFlashDialog = false
                    val tmpPath = "/data/local/tmp/ace_flash.img"
                    try {
                        ctx.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                            FileOutputStream(File(tmpPath)).use { output -> input.copyTo(output) }
                        }
                        resultMsg = PartitionUtils.flashPartition(selectedPartition!!, tmpPath)
                    } catch (e: Exception) { resultMsg = "读取文件失败: ${e.message}" }
                }, colors = ButtonDefaults.textButtonColors(contentColor = c.error)) { Text("确认刷写") }
            },
            dismissButton = { TextButton(onClick = { showFlashDialog = false }) { Text("取消") } },
            containerColor = c.surface
        )
    }

    // ──── 备份确认 ────
    if (showBackupDialog && selectedPartition != null) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            icon = { Icon(Icons.Outlined.SaveAlt, null, tint = c.primary) },
            title = { Text("备份分区") },
            text = {
                val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                Text("即将备份 $selectedPartition → /sdcard/AceKernel/${selectedPartition}_$ts.img\n\n需要约数十秒到数分钟，请耐心等待。")
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val outPath = "/sdcard/AceKernel/${selectedPartition}_$ts.img"
                    try {
                        var mkdirOut = PartitionUtils.execSh("mkdir -p /sdcard/AceKernel")
                        if (mkdirOut == null) mkdirOut = PartitionUtils.execSh("mkdir -p /storage/emulated/0/AceKernel")
                        resultMsg = PartitionUtils.backupPartition(selectedPartition!!, outPath)
                    } catch (e: Exception) { resultMsg = "备份失败: ${e.message}" }
                }, colors = ButtonDefaults.textButtonColors(contentColor = c.primary)) { Text("开始备份") }
            },
            dismissButton = { TextButton(onClick = { showBackupDialog = false }) { Text("取消") } },
            containerColor = c.surface
        )
    }

    // ──── 结果弹窗 ────
    if (resultMsg != null) {
        AlertDialog(
            onDismissRequest = { resultMsg = null },
            title = { Text("操作结果") },
            text = { Text(resultMsg ?: "") },
            confirmButton = { TextButton(onClick = { resultMsg = null }) { Text("确定") } },
            containerColor = c.surface
        )
    }
}

private fun parsePartitionNames(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    val result = mutableListOf<String>()
    for (line in raw.lines()) {
        val t = line.trim()
        if (t.isEmpty() || t.startsWith("total")) continue
        val parts = t.split("\\s+".toRegex())
        if (parts.size >= 9) {
            val nameField = parts[8]
            if (nameField != "->") result.add(nameField)
        }
    }
    return result.sorted()
}