package com.example.people

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.people.theme.PeopleTheme
import com.example.people.ui.PeopleViewModel
import com.example.people.ui.PeopleViewModelFactory
import com.example.people.ui.screens.DetailScreen
import com.example.people.ui.screens.DirectoryScreen
import com.example.people.ui.screens.EditPersonScreen

class MainActivity : ComponentActivity() {

    private val viewModel: PeopleViewModel by viewModels {
        PeopleViewModelFactory((application as PeopleApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PeopleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "directory"
                    ) {
                        composable("directory") {
                            DirectoryScreen(
                                viewModel = viewModel,
                                onAddPersonClick = { navController.navigate("edit") },
                                onPersonClick = { id -> navController.navigate("detail/$id") }
                            )
                        }
                        
                        composable(
                            "detail/{personId}",
                            arguments = listOf(navArgument("personId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val personId = backStackEntry.arguments?.getInt("personId")
                            if (personId != null) {
                                LaunchedEffect(personId) {
                                    viewModel.selectPerson(personId)
                                }
                                DetailScreen(
                                    viewModel = viewModel,
                                    onBackClick = { navController.popBackStack() },
                                    onEditClick = { id -> navController.navigate("edit?personId=$id") }
                                )
                            }
                        }
                        
                        composable(
                            "edit?personId={personId}",
                            arguments = listOf(
                                navArgument("personId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val personIdString = backStackEntry.arguments?.getString("personId")
                            val personId = personIdString?.toIntOrNull()
                            
                            EditPersonScreen(
                                viewModel = viewModel,
                                personId = personId,
                                onBackClick = { navController.popBackStack() },
                                onComplete = { savedId ->
                                    navController.popBackStack()
                                    if (personId == null) {
                                        navController.navigate("detail/$savedId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
