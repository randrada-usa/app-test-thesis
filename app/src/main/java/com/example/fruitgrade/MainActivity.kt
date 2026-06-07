package com.example.fruitgrade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable(
                        "home",
                        enterTransition = { fadeIn(animationSpec = tween(300)) },
                        exitTransition = { fadeOut(animationSpec = tween(300)) }
                    ) {
                        HomeScreen(
                            onScan = { fruitName, scientificName, model, mode ->
                                viewModel.setModelAndMode(fruitName, scientificName, model, mode)
                                navController.navigate("scan")
                            },
                            onHistory = { navController.navigate("history") }
                        )
                    }
                    composable(
                        "scan",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        ScanScreen(
                            viewModel = viewModel,
                            onResult = { navController.navigate("result") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "result",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        ResultScreen(
                            viewModel = viewModel,
                            onHome = { navController.popBackStack("home", false) },
                            onHistory = { navController.navigate("history") },
                            onRetry = {
                                viewModel.resetForRetry()
                                navController.popBackStack("scan", false)
                            }
                        )
                    }
                    composable(
                        "history",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        HistoryScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
