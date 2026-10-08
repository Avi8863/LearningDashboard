package com.example.learningdashboard.data.remote

import android.content.Context
import com.example.learningdashboard.domain.model.Course
import com.example.learningdashboard.domain.model.Lesson
import com.example.learningdashboard.util.NetworkUtils
import kotlinx.coroutines.delay
import java.io.IOException

class MockCourseApi(
    private val context: Context? = null
) {
    var simulateNetworkError: Boolean = false

    suspend fun getCourses(): List<Course> {
        delay(1000)

        if (simulateNetworkError || (context != null && !NetworkUtils.isNetworkAvailable(context))) {
            throw IOException("No internet connection")
        }

        return listOf(
            Course(
                id = 1,
                title = "Python Programming",
                instructor = "John Smith",
                progress = 50,
                totalLessons = 4,
                lessons = listOf(
                    Lesson(
                        id = 1,
                        title = "Introduction",
                        isCompleted = true
                    ),
                    Lesson(
                        id = 2,
                        title = "Variables & Data Types",
                        isCompleted = true
                    ),
                    Lesson(
                        id = 3,
                        title = "Functions",
                        isCompleted = false
                    ),
                    Lesson(
                        id = 4,
                        title = "OOP",
                        isCompleted = false
                    )
                )
            ),

            Course(
                id = 2,
                title = "Generative AI",
                instructor = "Sarah Williams",
                progress = 25,
                totalLessons = 4,
                lessons = listOf(
                    Lesson(
                        id = 5,
                        title = "Introduction to Generative AI",
                        isCompleted = true
                    ),
                    Lesson(
                        id = 6,
                        title = "Large Language Models",
                        isCompleted = false
                    ),
                    Lesson(
                        id = 7,
                        title = "Prompt Engineering",
                        isCompleted = false
                    ),
                    Lesson(
                        id = 8,
                        title = "AI Applications",
                        isCompleted = false
                    )
                )
            ),

            Course(
                id = 3,
                title = "Full Stack Development",
                instructor = "David Brown",
                progress = 25,
                totalLessons = 4,
                lessons = listOf(
                    Lesson(
                        id = 9,
                        title = "Introduction",
                        isCompleted = true
                    ),
                    Lesson(
                        id = 10,
                        title = "HTML & CSS",
                        isCompleted = false
                    ),
                    Lesson(
                        id = 11,
                        title = "JavaScript",
                        isCompleted = false
                    ),
                    Lesson(
                        id = 12,
                        title = "Backend Development",
                        isCompleted = false
                    )
                )
            )
        )
    }
}
