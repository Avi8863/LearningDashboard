package com.example.learningdashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(

    @PrimaryKey
    val id: Int,

    val title: String,

    val instructor: String,

    val progress: Int,

    val totalLessons: Int,

    val lessonsJson: String
)