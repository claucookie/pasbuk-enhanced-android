package labs.claucookie.pasbuk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import labs.claucookie.pasbuk.ui.navigation.PasbukNavigation
import labs.claucookie.pasbuk.ui.theme.PasbukEnhancedTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasbukEnhancedTheme {
                PasbukNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}