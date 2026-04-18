package org.example

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AppTest {

    @Test
    fun parsesDeadlineWithExpectedFormat() {
        val parsed = parseDeadline("2026-04-20 13:45")
        assertNotNull(parsed)
        assertEquals(LocalDateTime.of(2026, 4, 20, 13, 45), parsed)
    }

    @Test
    fun parseDeadlineReturnsNullForInvalidInput() {
        val parsed = parseDeadline("20-04-2026 13:45")
        assertNull(parsed)
    }

    @Test
    fun sortsTasksByNearestDeadline() {
        val tasks = listOf(
            Task(1, "Task B", LocalDateTime.of(2026, 4, 21, 10, 0)),
            Task(2, "Task A", LocalDateTime.of(2026, 4, 20, 9, 0)),
            Task(3, "Task C", LocalDateTime.of(2026, 4, 22, 7, 30))
        )

        val sorted = TaskSorting.sorted(tasks, SortOption.DEADLINE_ASC)

        assertEquals(listOf(2, 1, 3), sorted.map(Task::id))
    }

    @Test
    fun sortsTasksByStatusThenDeadline() {
        val tasks = listOf(
            Task(1, "Task Selesai", LocalDateTime.of(2026, 4, 21, 10, 0), isCompleted = true),
            Task(2, "Task Belum 2", LocalDateTime.of(2026, 4, 22, 9, 0), isCompleted = false),
            Task(3, "Task Belum 1", LocalDateTime.of(2026, 4, 20, 7, 30), isCompleted = false)
        )

        val sorted = TaskSorting.sorted(tasks, SortOption.STATUS)

        assertEquals(listOf(3, 2, 1), sorted.map(Task::id))
    }
}
