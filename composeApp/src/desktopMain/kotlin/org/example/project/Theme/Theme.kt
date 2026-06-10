package org.example.project.Theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

 val DarkColors = darkColorScheme(
    primary = Color.White, // Оглавление
    onPrimary = Color(145,165,167), // Вспомогательный текст
    primaryContainer = Color(5,11,28), // Карточка
    onPrimaryContainer = Color(0xFF3D2626), // Акцентный цвет
    secondary = Color(2,6,23),// Фон
    onSecondary = Color.Black, // Куб
    secondaryContainer = Color(59,208,151), // Кнопка Convert
    onSecondaryContainer = Color(194,139,134), // Кнопка ОТмена
    tertiary = Color(19,27,46), // Карточкак расширение
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)


@Composable
fun MyAppTheme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}