package com.kernelhelper.acex.ui.component.uninstalldialog

import androidx.compose.runtime.Composable
import com.kernelhelper.acex.ui.LocalUiMode
import com.kernelhelper.acex.ui.UiMode

@Composable
fun UninstallDialog(
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> UninstallDialogMiuix(show, onDismissRequest)
        UiMode.Material -> UninstallDialogMaterial(show, onDismissRequest)
    }
}
