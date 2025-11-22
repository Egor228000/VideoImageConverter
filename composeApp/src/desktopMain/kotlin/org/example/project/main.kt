package org.example.project

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.setWindowsAdaptiveTitleBar
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import org.jetbrains.compose.resources.painterResource
import videoimageconverter.composeapp.generated.resources.Res
import videoimageconverter.composeapp.generated.resources.icon
import java.awt.Dimension

fun main() = application {
    val state = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        size = DpSize(700.dp, 900.dp)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "VideoImageConverter",
        state = state,
        icon = painterResource(Res.drawable.icon)

    ) {
        window.setWindowsAdaptiveTitleBar()
        val dialogSettings = FileKitDialogSettings(this.window)
        window.minimumSize = Dimension(700, 950)
        val appViewModel = rememberSaveable { AppViewModel() }
        App(appViewModel, dialogSettings)
    }
}