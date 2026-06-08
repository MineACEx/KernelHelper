package com.kernelhelper.acex.ui.component.choosekmidialog

import androidx.compose.runtime.Composable
import com.kernelhelper.acex.ui.LocalUiMode
import com.kernelhelper.acex.ui.UiMode

@Composable
fun ChooseKmiDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> ChooseKmiDialogMiuix(show, onDismissRequest, onSelected)
        UiMode.Material -> ChooseKmiDialogMaterial(show, onDismissRequest, onSelected)
    }
}
