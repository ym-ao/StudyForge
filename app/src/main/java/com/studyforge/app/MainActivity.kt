package com.studyforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.studyforge.app.data.StudyDatabase
import com.studyforge.app.ui.StudyForgeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = StudyDatabase.get(applicationContext)
        val themePreferences = getSharedPreferences("studyforge_appearance", MODE_PRIVATE)
        setContent {
            var themeMode by remember {
                mutableStateOf(themePreferences.getString("theme_mode", "System") ?: "System")
            }
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> systemDark
            }
            val colors = if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFB5C7FF),
                    secondary = Color(0xFFC1C7E2),
                    tertiary = Color(0xFF80D5C6),
                    background = Color(0xFF111318),
                    surface = Color(0xFF191B22),
                    surfaceVariant = Color(0xFF292C35)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF3659B8),
                    secondary = Color(0xFF5D6688),
                    tertiary = Color(0xFF217C72),
                    background = Color(0xFFF7F8FC),
                    surface = Color(0xFFFFFFFF)
                )
            }
            MaterialTheme(colorScheme = colors) {
                Surface {
                    StudyForgeApp(
                        database = database,
                        themeMode = themeMode,
                        onThemeModeChange = { selected ->
                            themeMode = selected
                            themePreferences.edit().putString("theme_mode", selected).apply()
                        }
                    )
                }
            }
        }
    }
}
