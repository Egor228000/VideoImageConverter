package org.example.project.Theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
     primary = Color.Black, // Оглавление
     onPrimary = Color(0xFF8B8B8B), // Вспомогательный текст
     primaryContainer = Color(0xFFD7D7D7), // Карточка
     onPrimaryContainer = Color(0xFFBA85FA), // Акцентный цвет
     secondary = Color.White,// Фон
     onSecondary = Color.White, // Куб
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB3261E),
    onError = Color.White
)

 val DarkColors = darkColorScheme(
    primary = Color.White, // Оглавление
    onPrimary = Color(0xFF9E9E9E), // Вспомогательный текст
    primaryContainer = Color(0xFF282828), // Карточка
    onPrimaryContainer = Color(0xFFBA85FA), // Акцентный цвет
    secondary = Color.Black,// Фон
    onSecondary = Color.Black, // Куб
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
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