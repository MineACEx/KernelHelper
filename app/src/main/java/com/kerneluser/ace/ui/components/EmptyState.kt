package com.kerneluser.ace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.theme.LocalAceColors

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalAceColors.current
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = c.textMuted, modifier = Modifier.size(56.dp))
            Text(title, color = c.textPrimary, fontSize = 16.sp)
            if (description != null) {
                Text(description, color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    }
}