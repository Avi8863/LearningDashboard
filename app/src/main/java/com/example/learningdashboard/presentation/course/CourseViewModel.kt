package com.example.learningdashboard.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningdashboard.domain.model.Course
import com.example.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CourseUiState {
    data object Loading : CourseUiState
    data class Success(val courses: List<Course>) : CourseUiState
    data object Empty : CourseUiState
    data class Error(val message: String) : CourseUiState
}

class CourseViewModel(
    private val repository: CourseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseUiState>(CourseUiState.Loading)
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = CourseUiState.Loading

            val result = repository.getCourses()

            result
                .onSuccess { courses ->
                    if (courses.isEmpty()) {
                        _uiState.value = CourseUiState.Empty
                    } else {
                        _uiState.value = CourseUiState.Success(courses)

                        _selectedCourse.value?.let { currentSelected ->
                            courses.find { it.id == currentSelected.id }?.let { updatedSelected ->
                                _selectedCourse.value = updatedSelected
                            }
                        }
                    }
                }
                .onFailure { exception ->
                    _uiState.value = CourseUiState.Error(
                        exception.message ?: "Failed to load courses. Please check your connection."
                    )
                }
        }
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun selectCourseById(courseId: Int) {
        val currentState = _uiState.value
        if (currentState is CourseUiState.Success) {
            currentState.courses.find { it.id == courseId }?.let { course ->
                _selectedCourse.value = course
            }
        }
    }

    fun clearSelectedCourse() {
        _selectedCourse.value = null
    }

    fun markLessonComplete(lessonId: Int) {
        val currentCourse = _selectedCourse.value ?: return

        val updatedLessons = currentCourse.lessons.map { lesson ->
            if (lesson.id == lessonId) {
                lesson.copy(isCompleted = true)
            } else {
                lesson
            }
        }

        val completedCount = updatedLessons.count { it.isCompleted }
        val totalLessons = updatedLessons.size
        val updatedProgress = if (totalLessons > 0) {
            (completedCount * 100) / totalLessons
        } else {
            0
        }

        val updatedCourse = currentCourse.copy(
            progress = updatedProgress,
            lessons = updatedLessons
        )

        _selectedCourse.value = updatedCourse

        val currentState = _uiState.value
        if (currentState is CourseUiState.Success) {
            val updatedCourses = currentState.courses.map { course ->
                if (course.id == updatedCourse.id) {
                    updatedCourse
                } else {
                    course
                }
            }
            _uiState.value = CourseUiState.Success(updatedCourses)
        }

        viewModelScope.launch {
            repository.updateCourse(updatedCourse)
        }
    }
}
