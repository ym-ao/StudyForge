package com.studyforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.studyforge.app.data.StudyDatabase
import com.studyforge.app.ui.StudyForgeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = StudyDatabase.get(applicationContext)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF3659B8),
                    secondary = Color(0xFF5D6688),
                    tertiary = Color(0xFF217C72),
                    background = Color(0xFFF7F8FC),
                    surface = Color(0xFFFFFFFF)
                )
            ) {
                Surface { StudyForgeApp(database) }
            }
        }
    }
}
