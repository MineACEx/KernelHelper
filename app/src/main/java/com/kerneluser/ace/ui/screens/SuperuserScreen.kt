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
import com.kerneluser.ace.utils.RootUtils
import com.kerneluser.ace.utils.SuperuserEntry
import com.kerneluser.ace.utils.SuperuserUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperuserScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var isRooted by remember { mutableStateOf(false) }
    val list = remember { mutableStateListOf<SuperuserEntry>() }
    var input by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val s = RootUtils.checkRoot(); isRooted = s.isRooted
        if (isRooted) { list.clear(); list.addAll(SuperuserUtils.getSuperuserList()) }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("权限管理", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Bg)) },
        containerColor = AppColors.Bg
    ) { padding ->
        if (isLoading) { LoadingView(Modifier.padding(padding)) }
        else if (!isRooted) { EmptyState(Icons.Default.Shield, "需要 Root 权限", modifier = Modifier.padding(padding)) }
        else {
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Grant section
                SurfaceCard {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("授权应用", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                            OutlinedTextField(input, { input = it }, Modifier.weight(1f), label = { Text("包名") }, placeholder = { Text("com.example.app") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.Primary, focusedLabelColor = AppColors.Primary))
                            Button(onClick = {
                                if (input.isNotBlank()) { SuperuserUtils.grantApp(input); list.clear(); list.addAll(SuperuserUtils.getSuperuserList()); input = "" }
                            }, enabled = input.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)) { Text("授权") }
                        }
                    }
                }

                Text("已授权 (${list.size})", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                if (list.isEmpty()) {
                    EmptyState(Icons.Outlined.VerifiedUser, "暂无授权", modifier = Modifier.height(200.dp))
                } else {
                    list.forEach { e ->
                        SurfaceCard {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(e.packageName, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    Text("${e.manager} · UID: ${e.uid}", color = AppColors.TextSecondary, fontSize = 12.sp)
                                }
                                FilledTonalButton(onClick = { SuperuserUtils.revokeApp(e.packageName); list.remove(e) }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = AppColors.Error.copy(alpha = 0.1f), contentColor = AppColors.Error)) { Text("撤销") }
                            }
                        }
                    }
                }
            }
        }
    }
}