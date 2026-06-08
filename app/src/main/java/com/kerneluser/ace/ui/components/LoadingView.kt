package com.kerneluser.ace.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kerneluser.ace.ui.theme.LocalAceColors

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    val c = LocalAceColors.current
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = c.primary)
    }
}