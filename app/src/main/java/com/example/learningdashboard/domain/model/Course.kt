package com.example.learningdashboard.domain.model

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val totalLessons: Int,
    val lessons: List<Lesson>
)