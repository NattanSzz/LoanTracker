package com.loantracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.loantracker.app.ui.navigation.LoanTrackerNavGraph
import com.loantracker.app.ui.theme.LoanTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoanTrackerRoot()
        }
    }
}

@Composable
fun LoanTrackerRoot() {
    LoanTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoanTrackerNavGraph()
        }
    }
}
