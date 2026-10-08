package com.example.learningdashboard.data.repository

import com.example.learningdashboard.data.local.CourseDao
import com.example.learningdashboard.data.local.CourseEntity
import com.example.learningdashboard.data.remote.MockCourseApi
import com.example.learningdashboard.domain.model.Course
import com.example.learningdashboard.domain.model.Lesson
import com.example.learningdashboard.domain.repository.CourseRepository

class CourseRepositoryImpl(
    private val api: MockCourseApi,
    private val dao: CourseDao,
) : CourseRepository {

    override suspend fun getCourses(): Result<List<Course>> {
        return try {
            val apiCourses = api.getCourses()
            val cachedEntities = dao.getCourses()
            val cachedCourses = cachedEntities.map { it.toDomainModel() }

            val mergedCourses = apiCourses.map { apiCourse ->
                val cachedCourse = cachedCourses.find { it.id == apiCourse.id }

                if (cachedCourse != null && cachedCourse.lessons.isNotEmpty()) {
                    val mergedLessons = apiCourse.lessons.map { apiLesson ->
                        val cachedLesson = cachedCourse.lessons.find { it.id == apiLesson.id }
                        if ((cachedLesson != null) && cachedLesson.isCompleted) {
                            apiLesson.copy(isCompleted = true)
                        } else {
                            apiLesson
                        }
                    }

                    val completedCount = mergedLessons.count { it.isCompleted }
                    val progress = if (mergedLessons.isNotEmpty()) {
                        (completedCount * 100) / mergedLessons.size
                    } else {
                        0
                    }

                    apiCourse.copy(
                        progress = progress,
                        lessons = mergedLessons
                    )
                } else {
                    apiCourse
                }
            }

            val entities = mergedCourses.map { course ->
                CourseEntity(
                    id = course.id,
                    title = course.title,
                    instructor = course.instructor,
                    progress = course.progress,
                    totalLessons = course.totalLessons,
                    lessonsJson = serializeLessons(course.lessons)
                )
            }

            dao.clearCourses()
            dao.insertCourses(entities)

            Result.success(mergedCourses)
        } catch (exception: Exception) {
            val cachedEntities = dao.getCourses()
            if (cachedEntities.isNotEmpty()) {
                val cachedCourses = cachedEntities.map { it.toDomainModel() }
                if (cachedCourses.any { it.lessons.isNotEmpty() }) {
                    Result.success(cachedCourses)
                } else {
                    Result.failure(exception)
                }
            } else {
                Result.failure(exception)
            }
        }
    }

    override suspend fun updateCourse(course: Course) {
        val entity = CourseEntity(
            id = course.id,
            title = course.title,
            instructor = course.instructor,
            progress = course.progress,
            totalLessons = course.totalLessons,
            lessonsJson = serializeLessons(course.lessons)
        )
        dao.insertCourses(listOf(entity))
    }

    private fun serializeLessons(lessons: List<Lesson>): String {
        return lessons.joinToString(";") { lesson ->
            val safeTitle = lesson.title.replace(";", ",")
            "${lesson.id}:$safeTitle:${lesson.isCompleted}"
        }
    }

    private fun CourseEntity.toDomainModel(): Course {
        val lessonsList = deserializeLessons(lessonsJson)

        return Course(
            id = id,
            title = title,
            instructor = instructor,
            progress = progress,
            totalLessons = if (totalLessons > 0) totalLessons else lessonsList.size,
            lessons = lessonsList
        )
    }

    private fun deserializeLessons(lessonsJson: String): List<Lesson> {
        if (lessonsJson.isBlank()) return emptyList()

        // 1. Try colon + semicolon format (id:title:isCompleted;...)
        if (lessonsJson.contains(":")) {
            val list = lessonsJson.split(";").mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size >= 3) {
                    val id = parts[0].toIntOrNull() ?: return@mapNotNull null
                    val isCompleted = parts.last().toBoolean()
                    val title = parts.subList(1, parts.size - 1).joinToString(":")
                    Lesson(id = id, title = title, isCompleted = isCompleted)
                } else null
            }
            if (list.isNotEmpty()) return list
        }

        // 2. Try old pipe + comma format (id,title,isCompleted|...)
        if (lessonsJson.contains("|") || lessonsJson.contains(",")) {
            val list = lessonsJson.split("|").mapNotNull { item ->
                val parts = item.split(",")
                if (parts.size >= 3) {
                    val id = parts[0].toIntOrNull() ?: return@mapNotNull null
                    val isCompleted = parts.last().toBoolean()
                    val title = parts.subList(1, parts.size - 1).joinToString(",")
                    Lesson(id = id, title = title, isCompleted = isCompleted)
                } else null
            }
            if (list.isNotEmpty()) return list
        }

        return emptyList()
    }
}
