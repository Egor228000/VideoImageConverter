package org.example.project

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyle
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "VideoImageConverter",
    ) {
        WindowStyle(
            isDarkTheme = true,
            backdropType = WindowBackdrop.Mica,
            frameStyle = WindowFrameStyle(cornerPreference = WindowCornerPreference.ROUNDED),
        )
        val dialogSettings = FileKitDialogSettings(this.window)
        window.minimumSize = Dimension(700, 900)
        val appViewModel = rememberSaveable { AppViewModel() }
        App(appViewModel, dialogSettings)
    }
}