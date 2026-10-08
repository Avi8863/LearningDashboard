package com.example.learningdashboard.domain.repository

import com.example.learningdashboard.domain.model.Course

interface CourseRepository {

    suspend fun getCourses(): Result<List<Course>>

    suspend fun updateCourse(course: Course)
}