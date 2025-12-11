package labs.claucookie.pasbuk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import labs.claucookie.pasbuk.ui.navigation.PasbukNavigation
import labs.claucookie.pasbuk.ui.theme.PaskbukEnhancedTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaskbukEnhancedTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PasbukNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}