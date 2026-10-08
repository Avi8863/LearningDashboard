package com.example.learningdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learningdashboard.data.local.AppDatabase
import com.example.learningdashboard.data.remote.MockCourseApi
import com.example.learningdashboard.data.repository.CourseRepositoryImpl
import com.example.learningdashboard.presentation.course.CourseViewModel
import com.example.learningdashboard.presentation.course.CourseViewModelFactory
import com.example.learningdashboard.presentation.dashboard.DashboardScreen
import com.example.learningdashboard.presentation.details.CourseDetailsScreen
import com.example.learningdashboard.presentation.login.LoginScreen
import com.example.learningdashboard.presentation.login.LoginViewModel
import com.example.learningdashboard.ui.theme.LearningDashboardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        val api = MockCourseApi(context = applicationContext)
        val repository = CourseRepositoryImpl(
            api = api,
            dao = database.courseDao()
        )

        setContent {
            LearningDashboardTheme {
                LearningDashboardApp(
                    repository = repository
                )
            }
        }
    }
}

@Composable
private fun LearningDashboardApp(
    repository: CourseRepositoryImpl
) {
    val navController = rememberNavController()

    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = courseViewModel,
                onContinueClick = { course ->
                    courseViewModel.selectCourse(course)
                    navController.navigate("details")
                }
            )
        }

        composable("details") {
            val selectedCourse by courseViewModel.selectedCourse.collectAsState()

            if (selectedCourse != null) {
                CourseDetailsScreen(
                    course = selectedCourse!!,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLessonComplete = { lesson ->
                        courseViewModel.markLessonComplete(
                            lessonId = lesson.id
                        )
                    }
                )
            }
        }
    }
}
