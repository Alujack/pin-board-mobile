package kh.edu.rupp.fe.ite.pinboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kh.edu.rupp.fe.ite.pinboard.ui.theme.PinBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PinBoardTheme {
                val systemUiController = rememberSystemUiController()
                val darkIcons = materialTheme.colorScheme.isLight
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = color.Transparent,
                        darkIcons = darkIcons
                    )56
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Text(
            text = "Hello $name!",
            modifier = modifier
        )

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PinBoardTheme {
        Greeting("Team at ITE")
    }
}