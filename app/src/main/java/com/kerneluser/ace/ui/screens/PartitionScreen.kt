package com.kerneluser.ace.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.components.*
import com.kerneluser.ace.ui.theme.AppColors
import com.kerneluser.ace.utils.PartitionUtils
import com.kerneluser.ace.utils.RootUtils
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartitionScreen() {
    val ctx = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isRooted by remember { mutableStateOf(false) }
    var partitions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedPartition by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf("") }
    var showFlashDialog by remember { mutableStateOf(false) }
    var resultMsg by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // get display name
            try {
                ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex("_display_name")
                        if (idx >= 0) selectedImageName = cursor.getString(idx)
                    }
                }
            } catch (e: Exception) { selectedImageName = "unknown.img" }
        }
    }

    LaunchedEffect(Unit) {
        val s = RootUtils.checkRoot(); isRooted = s.isRooted
        if (isRooted) { partitions = parsePartitionNames(PartitionUtils.getPartitionList()) }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("分区管理", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Bg)) },
        containerColor = AppColors.Bg
    ) { padding ->
        if (isLoading) { LoadingView(Modifier.padding(padding)) }
        else if (!isRooted) { EmptyState(Icons.Default.Shield, "需要 Root 权限", modifier = Modifier.padding(padding)) }
        else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Flash section
                Text("刷写分区", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Currently selected
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("目标分区", color = AppColors.TextSecondary, fontSize = 14.sp)
                            if (selectedPartition != null) {
                                StatusBadge(selectedPartition!!, AppColors.Primary)
                            } else {
                                Text("未选择", color = AppColors.TextMuted, fontSize = 13.sp)
                            }
                        }

                        // Image picker
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { filePicker.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Outlined.FolderOpen, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (selectedImageName.isEmpty()) "选择镜像文件" else selectedImageName, maxLines = 1, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = { showFlashDialog = true },
                            enabled = selectedPartition != null && selectedImageUri != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error, contentColor = AppColors.Surface)
                        ) { Text("刷写分区", fontWeight = FontWeight.Bold) }
                    }
                }

                // Partition list
                Text("分区列表 (${partitions.size})", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                if (partitions.isEmpty()) {
                    Text("无法获取分区列表", color = AppColors.TextSecondary, fontSize = 14.sp)
                } else {
                    SurfaceCard {
                        Column(Modifier.fillMaxWidth().padding(4.dp)) {
                            partitions.forEach { name ->
                                val isSelected = selectedPartition == name
                                Surface(
                                    onClick = { selectedPartition = name },
                                    color = if (isSelected) AppColors.Primary.copy(alpha = 0.08f) else AppColors.Surface,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Icon(Icons.Outlined.DiscFull, null, tint = if (isSelected) AppColors.Primary else AppColors.TextMuted, modifier = Modifier.size(20.dp))
                                            Text(name, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        }
                                        if (isSelected) Icon(Icons.Outlined.CheckCircle, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                HorizontalDivider(0.5.dp, AppColors.Divider)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }

    // Flash confirm dialog
    if (showFlashDialog && selectedPartition != null && selectedImageUri != null) {
        AlertDialog(
            onDismissRequest = { showFlashDialog = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = AppColors.Error) },
            title = { Text("确认刷写") },
            text = { Text("即将刷写 ${selectedImageName} → /dev/block/by-name/$selectedPartition\n\n错误操作可能导致设备变砖！") },
            confirmButton = {
                TextButton(onClick = {
                    showFlashDialog = false
                    // copy SAF uri to a temp path that su can access
                    val tmpPath = "/data/local/tmp/ace_flash.img"
                    try {
                        ctx.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                            FileOutputStream(File(tmpPath)).use { output -> input.copyTo(output) }
                        }
                        resultMsg = PartitionUtils.flashPartition(selectedPartition!!, tmpPath)
                    } catch (e: Exception) { resultMsg = "读取文件失败: ${e.message}" }
                }, colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error)) { Text("确认刷写") }
            },
            dismissButton = { TextButton(onClick = { showFlashDialog = false }) { Text("取消") } },
            containerColor = AppColors.Surface
        )
    }

    // Result dialog
    if (resultMsg != null) {
        AlertDialog(
            onDismissRequest = { resultMsg = null },
            title = { Text("操作结果") },
            text = { Text(resultMsg ?: "") },
            confirmButton = { TextButton(onClick = { resultMsg = null }) { Text("确定") } },
            containerColor = AppColors.Surface
        )
    }
}

// Parse partition names from "ls -la /dev/block/by-name/" output
private fun parsePartitionNames(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    val result = mutableListOf<String>()
    // ls -la output lines: "lrwxrwxrwx 1 root root 16 ... boot -> /dev/block/mmcblk0p42"
    // We want the first column after the permissions/time — the symlink name
    for (line in raw.lines()) {
        val t = line.trim()
        if (t.isEmpty() || t.startsWith("total")) continue
        // split by whitespace, last token is the name (or name -> target)
        val parts = t.split("\\s+".toRegex())
        if (parts.size >= 9) {
            val nameField = parts[8] // the symlink name
            if (nameField != "->") {
                result.add(nameField)
            }
        }
    }
    return result.sorted()
}