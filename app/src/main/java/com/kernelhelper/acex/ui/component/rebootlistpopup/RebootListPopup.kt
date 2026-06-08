package com.kernelhelper.acex.ui.component.rebootlistpopup

import androidx.compose.runtime.Composable
import com.kernelhelper.acex.ui.LocalUiMode
import com.kernelhelper.acex.ui.UiMode

@Composable
fun RebootListPopup() {
    when (LocalUiMode.current) {
        UiMode.Miuix -> RebootListPopupMiuix()
        UiMode.Material -> RebootListPopupMaterial()
    }
}
