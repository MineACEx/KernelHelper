package com.kerneluser.ace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.screens.*
import com.kerneluser.ace.ui.theme.AppColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AceKernelApp()
        }
    }
}

enum class Tab(val label: String, val icon: ImageVector, val activeIcon: ImageVector) {
    Home("主页", Icons.Outlined.PhoneAndroid, Icons.Filled.PhoneAndroid),
    Modules("模块", Icons.Outlined.Widgets, Icons.Filled.Widgets),
    Superuser("授权", Icons.Outlined.Shield, Icons.Filled.Shield),
    Partition("分区", Icons.Outlined.Memory, Icons.Filled.Memory),
    Settings("设置", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@Composable
fun AceKernelApp() {
    var selectedTab by remember { mutableStateOf(Tab.Home) }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = AppColors.GlassBg,
                shadowElevation = 12.dp,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, AppColors.GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) Modifier.background(AppColors.Primary.copy(alpha = 0.08f))
                                    else Modifier
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(onClick = { selectedTab = tab }) {
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) AppColors.TabActive else AppColors.TabInactive,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                tab.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) AppColors.TabActive else AppColors.TabInactive
                            )
                            if (isSelected) {
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier
                                        .width(16.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(AppColors.TabActive)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { tab ->
                when (tab) {
                    Tab.Home -> HomeScreen()
                    Tab.Modules -> ModulesScreen()
                    Tab.Superuser -> SuperuserScreen()
                    Tab.Partition -> PartitionScreen()
                    Tab.Settings -> SettingsScreen()
                }
            }
        }
    }
}