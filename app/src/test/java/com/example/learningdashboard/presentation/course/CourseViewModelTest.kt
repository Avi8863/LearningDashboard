package com.example.learningdashboard.presentation.course

import com.example.learningdashboard.domain.model.Course
import com.example.learningdashboard.domain.model.Lesson
import com.example.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCourseRepository
    private lateinit var viewModel: CourseViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCourseRepository()
        viewModel = CourseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCourses_success_updatesUiStateToSuccess() = runTest {
        val courses = listOf(
            Course(
                id = 1,
                title = "Kotlin Basics",
                instructor = "John Smith",
                progress = 50,
                totalLessons = 4,
                lessons = listOf(
                    Lesson(id = 1, title = "Intro", isCompleted = true),
                    Lesson(id = 2, title = "Variables", isCompleted = true),
                    Lesson(id = 3, title = "Functions", isCompleted = false),
                    Lesson(id = 4, title = "Classes", isCompleted = false)
                )
            )
        )
        repository.coursesResult = Result.success(courses)

        viewModel.loadCourses()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CourseUiState.Success)
        assertEquals(courses, (state as CourseUiState.Success).courses)
    }

    @Test
    fun loadCourses_empty_updatesUiStateToEmpty() = runTest {
        repository.coursesResult = Result.success(emptyList())

        viewModel.loadCourses()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CourseUiState.Empty)
    }

    @Test
    fun loadCourses_failure_updatesUiStateToError() = runTest {
        repository.coursesResult = Result.failure(RuntimeException("Network error"))

        viewModel.loadCourses()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CourseUiState.Error)
        assertEquals("Network error", (state as CourseUiState.Error).message)
    }

    @Test
    fun markLessonComplete_updatesCourseProgressAndPersists() = runTest {
        val course = Course(
            id = 1,
            title = "Kotlin Basics",
            instructor = "John Smith",
            progress = 50,
            totalLessons = 4,
            lessons = listOf(
                Lesson(id = 1, title = "Intro", isCompleted = true),
                Lesson(id = 2, title = "Variables", isCompleted = true),
                Lesson(id = 3, title = "Functions", isCompleted = false),
                Lesson(id = 4, title = "Classes", isCompleted = false)
            )
        )

        viewModel.selectCourse(course)
        viewModel.markLessonComplete(3)
        advanceUntilIdle()

        val updatedCourse = viewModel.selectedCourse.value

        assertEquals(75, updatedCourse?.progress)
        assertEquals(true, updatedCourse?.lessons?.first { it.id == 3 }?.isCompleted)
        assertEquals(updatedCourse, repository.updatedCourse)
    }

    private class FakeCourseRepository : CourseRepository {
        var coursesResult: Result<List<Course>> = Result.success(emptyList())
        var updatedCourse: Course? = null

        override suspend fun getCourses(): Result<List<Course>> {
            return coursesResult
        }

        override suspend fun updateCourse(course: Course) {
            updatedCourse = course
        }
    }
}
