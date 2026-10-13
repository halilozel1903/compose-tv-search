package io.github.halilozel1903.tvsearch.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.darkColorScheme

/**
 * D-pad presses can't be timed reliably through adb on a fresh emulator, so
 * `scripts/screenshots.sh` starts the app with `--es scene <scene>` to set up each screenshot:
 *
 * - `empty`: nothing typed, the keyboard focused, recent searches above the empty state
 * - `typed`: "light" typed, the "t" key focused, suggestions and results
 * - `result`: "light" typed and the second result focused
 *
 * Without the extra the app starts on an empty search (same as `empty`).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            MaterialTheme(colorScheme = SampleColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                ) {
                    SampleApp(scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}

private val SampleColors = darkColorScheme(
    primary = Color(0xFF7FD1FF),
    onPrimary = Color(0xFF00243A),
    secondary = Color(0xFFFFB86B),
    background = Color(0xFF0A0D14),
    onBackground = Color(0xFFF1F3F8),
    surface = Color(0xFF0A0D14),
    onSurface = Color(0xFFF1F3F8),
    surfaceVariant = Color(0xFF1E2533),
    onSurfaceVariant = Color(0xFFA9B2C3),
)
