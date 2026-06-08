package com.kerneluser.ace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kerneluser.ace.ui.theme.AppColors

@Composable
fun BlurCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.BlurBg)
            .border(0.5.dp, AppColors.Divider, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    color: Color = AppColors.Surface,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(1.dp, AppColors.Divider, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}