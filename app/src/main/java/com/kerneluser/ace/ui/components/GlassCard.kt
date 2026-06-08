package com.kerneluser.ace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kerneluser.ace.ui.theme.LocalAceColors

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = LocalAceColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(0.5.dp, c.divider, RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val c = LocalAceColors.current
    val color = c.blurBg
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(0.5.dp, c.divider, RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
fun SectionHeader(title: String) {
    val c = LocalAceColors.current
    Text(title, color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
}