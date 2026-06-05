package com.example.fruitgrade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fruitgrade.ui.screens.HistoryScreen
import com.example.fruitgrade.ui.screens.HomeScreen
import com.example.fruitgrade.ui.screens.ResultScreen
import com.example.fruitgrade.ui.screens.ScanScreen
import com.example.fruitgrade.ui.theme.FruitGradeTheme
import com.example.fruitgrade.viewmodel.ScanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FruitGradeTheme {
                val navController = rememberNavController()
                val viewModel: ScanViewModel = viewModel()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onScan = { model, mode ->
                                viewModel.setModelAndMode(model, mode)
                                navController.navigate("scan")
                            },
                            onHistory = { navController.navigate("history") }
                        )
                    }
                    composable("scan") {
                        ScanScreen(
                            viewModel = viewModel,
                            onResult = { navController.navigate("result") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("result") {
                        ResultScreen(
                            viewModel = viewModel,
                            onHome = { navController.popBackStack("home", false) },
                            onHistory = { navController.navigate("history") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
