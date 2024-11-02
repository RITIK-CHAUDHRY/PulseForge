package com.pulseforge
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulseforge.data.db.AppDatabase
import com.pulseforge.data.repository.WorkoutRepository
import com.pulseforge.data.RecommendationEngine
import com.pulseforge.service.GoogleFitService
import com.pulseforge.ui.*
import com.pulseforge.ui.theme.PulseForgeTheme
import com.pulseforge.viewmodel.AuthViewModel
import com.pulseforge.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {
    private lateinit var googleFitService: GoogleFitService

    private val googleFitPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Google Fit permissions granted
        } else {
            // Google Fit permissions not granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleFitService = GoogleFitService(this)

        setContent {
            PulseForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PulseForgeApp(googleFitService)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!googleFitService.hasPermissions()) {
            googleFitService.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)
        }
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    }
}

@Composable
fun PulseForgeApp(googleFitService: GoogleFitService) {
    val navController = rememberNavController()
    val database = AppDatabase.getDatabase(LocalContext.current)
    val repository = WorkoutRepository(database.workoutRoutineDao())
    val recommendationEngine = RecommendationEngine()
    val workoutViewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModel.WorkoutViewModelFactory(LocalContext.current.applicationContext as android.app.Application, repository, recommendationEngine)
    )
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory())

    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { navController.navigate("main") }
            )
        }
        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = { navController.navigate("main") }
            )
        }
        composable("main") {
            MainScreen(
                workoutViewModel = workoutViewModel,
                authViewModel = authViewModel,
                googleFitService = googleFitService,
                onLogout = { navController.navigate("login") }
            )
        }
        // ... (other composables remain unchanged)
    }
}

@Composable
fun MainScreen(
    workoutViewModel: WorkoutViewModel,
    authViewModel: AuthViewModel,
    googleFitService: GoogleFitService,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PulseForge") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = { BottomNavigation(navController) }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "workoutList") {
            composable("workoutList") {
                WorkoutRoutineListScreen(
                    workoutViewModel = workoutViewModel,
                    onCreateRoutine = { navController.navigate("createEditRoutine") },
                    onEditRoutine = { routine ->
                        navController.navigate("createEditRoutine/${routine.id}")
                    },
                    onViewExerciseProgress = { routineId, exerciseIndex ->
                        navController.navigate("exerciseProgress/$routineId/$exerciseIndex")
                    }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    workoutViewModel = workoutViewModel,
                    onEditRoutine = { routine ->
                        navController.navigate("createEditRoutine/${routine.id}")
                    }
                )
            }
            composable("recommendedWorkouts") {
                RecommendedWorkoutsScreen(
                    workoutViewModel = workoutViewModel,
                    onWorkoutSelected = { workout ->
                        navController.navigate("recommendedWorkoutDetail/${workout.id}")
                    }
                )
            }
            composable("overallProgress") {
                OverallProgressScreen(
                    workoutViewModel = workoutViewModel,
                    authViewModel = authViewModel
                )
            }
            composable("fitnessData") {
                FitnessDataScreen(googleFitService = googleFitService)
            }
            // ... (other composables remain unchanged)
        }
    }
}

@Composable
fun BottomNavigation(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Workouts", Icons.Default.FitnessCenter, "workoutList"),
        BottomNavItem("Calendar", Icons.Default.CalendarToday, "calendar"),
        BottomNavItem("Recommended", Icons.Default.Recommend, "recommendedWorkouts"),
        BottomNavItem("Progress", Icons.Default.ShowChart, "overallProgress"),
        BottomNavItem("Fitness Data", Icons.Default.DataUsage, "fitnessData")
    )
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
