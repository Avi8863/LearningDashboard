package com.example.learningdashboard.data.repository

import com.example.learningdashboard.data.local.CourseDao
import com.example.learningdashboard.data.local.CourseEntity
import com.example.learningdashboard.data.remote.MockCourseApi
import com.example.learningdashboard.domain.model.Course
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CourseRepositoryImplTest {

    private lateinit var api: MockCourseApi
    private lateinit var dao: FakeCourseDao
    private lateinit var repository: CourseRepositoryImpl

    @Before
    fun setup() {
        api = MockCourseApi()
        dao = FakeCourseDao()
        repository = CourseRepositoryImpl(api = api, dao = dao)
    }

    @Test
    fun getCourses_online_fetchesFromApiAndSavesToDao() = runTest {
        val result = repository.getCourses()

        assertTrue(result.isSuccess)
        val courses = result.getOrNull()
        assertEquals(3, courses?.size)
        assertEquals(3, dao.savedEntities.size)
    }

    @Test
    fun getCourses_offline_returnsCachedCoursesFromDao() = runTest {
        // Pre-populate DAO cache
        val initialResult = repository.getCourses()
        assertTrue(initialResult.isSuccess)

        // Force network error
        api.simulateNetworkError = true

        val offlineResult = repository.getCourses()

        assertTrue(offlineResult.isSuccess)
        val cachedCourses = offlineResult.getOrNull()
        assertEquals(3, cachedCourses?.size)
    }

    @Test
    fun getCourses_offlineAndNoCache_returnsFailure() = runTest {
        api.simulateNetworkError = true

        val result = repository.getCourses()

        assertTrue(result.isFailure)
    }

    private class FakeCourseDao : CourseDao {
        val savedEntities = mutableListOf<CourseEntity>()

        override suspend fun insertCourses(courses: List<CourseEntity>) {
            savedEntities.removeAll { existing -> courses.any { it.id == existing.id } }
            savedEntities.addAll(courses)
        }

        override suspend fun getCourses(): List<CourseEntity> {
            return savedEntities.toList()
        }

        override suspend fun clearCourses() {
            savedEntities.clear()
        }
    }
}
