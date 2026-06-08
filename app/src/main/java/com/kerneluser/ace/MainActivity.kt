package com.kerneluser.ace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.screens.*
import com.kerneluser.ace.ui.theme.DarkTheme
import com.kerneluser.ace.ui.theme.LightTheme
import com.kerneluser.ace.ui.theme.LocalAceColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDark by remember { mutableStateOf(false) }
            CompositionLocalProvider(LocalAceColors provides if (isDark) DarkTheme else LightTheme) {
                AceKernelApp(isDark = isDark, onToggleDark = { isDark = it })
            }
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
fun AceKernelApp(isDark: Boolean = false, onToggleDark: (Boolean) -> Unit = {}) {
    val c = LocalAceColors.current
    var selectedTab by remember { mutableStateOf(Tab.Home) }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        // 主内容区
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 78.dp)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val dir = if (targetState.ordinal > initialState.ordinal)
                        AnimatedContentTransitionScope.SlideDirection.Left
                    else AnimatedContentTransitionScope.SlideDirection.Right
                    (slideIntoContainer(dir, tween(320)) + fadeIn(tween(260)))
                        .togetherWith(slideOutOfContainer(dir.opposite(), tween(320)) + fadeOut(tween(260)))
                },
                label = "page"
            ) { tab ->
                when (tab) {
                    Tab.Home -> HomeScreen()
                    Tab.Modules -> ModulesScreen()
                    Tab.Superuser -> SuperuserScreen()
                    Tab.Partition -> PartitionScreen()
                    Tab.Settings -> SettingsScreen(isDark = isDark, onToggleDark = onToggleDark)
                }
            }
        }

        // 悬浮 Tab 栏 — 真高斯模糊
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .blur(2.dp)
                    .background(c.blurBg)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tab.entries.forEach { tab ->
                        val isSel = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (isSel) Modifier.background(c.primary.copy(alpha = 0.12f))
                                    else Modifier
                                )
                                .then(
                                    if (!isSel) Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { selectedTab = tab }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSel) tab.activeIcon else tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSel) c.tabActive else c.tabInactive,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) c.tabActive else c.tabInactive,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 辅助：反向滑出方向
private fun AnimatedContentTransitionScope.SlideDirection.opposite(): AnimatedContentTransitionScope.SlideDirection =
    when (this) {
        AnimatedContentTransitionScope.SlideDirection.Left -> AnimatedContentTransitionScope.SlideDirection.Right
        AnimatedContentTransitionScope.SlideDirection.Right -> AnimatedContentTransitionScope.SlideDirection.Left
        else -> this
    }