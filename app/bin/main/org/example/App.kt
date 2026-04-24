package org.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class Task(
    val id: Int,
    val title: String,
    val deadline: LocalDateTime,
    val isCompleted: Boolean = false
)

enum class SortOption(val label: String) {
    DEADLINE_ASC("Deadline Terdekat"),
    DEADLINE_DESC("Deadline Terjauh"),
    STATUS("Status")
}

object TaskSorting {
    fun sorted(tasks: List<Task>, sortOption: SortOption): List<Task> {
        return when (sortOption) {
            SortOption.DEADLINE_ASC -> tasks.sortedBy(Task::deadline)
            SortOption.DEADLINE_DESC -> tasks.sortedByDescending(Task::deadline)
            SortOption.STATUS -> tasks.sortedWith(compareBy<Task> { it.isCompleted }.thenBy(Task::deadline))
        }
    }
}

private val deadlineFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun parseDeadline(input: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(input.trim(), deadlineFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun formatDeadline(deadline: LocalDateTime): String = deadline.format(deadlineFormatter)

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

@Composable
fun TodoApp() {
    val tasks = remember { mutableStateListOf<Task>() }
    val now = remember { LocalDateTime.now() }

    var titleInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var nextId by remember { mutableIntStateOf(1) }

    val yearOptions = remember(now.year) { (now.year..now.year + 5).toList() }
    val monthOptions = remember { (1..12).toList() }
    val hourOptions = remember { (0..23).toList() }
    val minuteOptions = remember { (0..55 step 5).toList() }

    var selectedYear by remember { mutableIntStateOf(now.year) }
    var selectedMonth by remember { mutableIntStateOf(now.monthValue) }
    var selectedDay by remember { mutableIntStateOf(now.dayOfMonth) }
    var selectedHour by remember { mutableIntStateOf(now.hour) }
    var selectedMinute by remember { mutableIntStateOf((now.minute / 5) * 5) }

    val maxDayInMonth = remember(selectedYear, selectedMonth) {
        YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    }
    val dayOptions = remember(selectedYear, selectedMonth) { (1..maxDayInMonth).toList() }

    LaunchedEffect(selectedYear, selectedMonth) {
        if (selectedDay > maxDayInMonth) {
            selectedDay = maxDayInMonth
        }
    }

    var sortOption by remember { mutableStateOf(SortOption.DEADLINE_ASC) }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    val addTask: () -> Unit = {
        val trimmedTitle = titleInput.trim()
        errorMessage = if (trimmedTitle.isEmpty()) "Judul tugas tidak boleh kosong." else null

        if (errorMessage == null) {
            val selectedDeadline = LocalDateTime.of(
                selectedYear,
                selectedMonth,
                selectedDay,
                selectedHour,
                selectedMinute
            )
            tasks.add(
                Task(
                    id = nextId++,
                    title = trimmedTitle,
                    deadline = selectedDeadline
                )
            )
            titleInput = ""
        }
    }

    val sortedTasks = TaskSorting.sorted(tasks, sortOption)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Daily Task Manager",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pilih deadline dari dropdown tanggal dan waktu.",
                style = MaterialTheme.typography.bodyMedium
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompact = maxWidth < 760.dp
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TaskInputFields(
                            titleInput = titleInput,
                            onTitleInputChange = { titleInput = it },
                            yearOptions = yearOptions,
                            monthOptions = monthOptions,
                            dayOptions = dayOptions,
                            hourOptions = hourOptions,
                            minuteOptions = minuteOptions,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            selectedDay = selectedDay,
                            selectedHour = selectedHour,
                            selectedMinute = selectedMinute,
                            onYearSelected = { selectedYear = it },
                            onMonthSelected = { selectedMonth = it },
                            onDaySelected = { selectedDay = it },
                            onHourSelected = { selectedHour = it },
                            onMinuteSelected = { selectedMinute = it }
                        )
                        Button(
                            onClick = addTask,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Tambah Tugas")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TaskInputFields(
                            titleInput = titleInput,
                            onTitleInputChange = { titleInput = it },
                            yearOptions = yearOptions,
                            monthOptions = monthOptions,
                            dayOptions = dayOptions,
                            hourOptions = hourOptions,
                            minuteOptions = minuteOptions,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            selectedDay = selectedDay,
                            selectedHour = selectedHour,
                            selectedMinute = selectedMinute,
                            onYearSelected = { selectedYear = it },
                            onMonthSelected = { selectedMonth = it },
                            onDaySelected = { selectedDay = it },
                            onHourSelected = { selectedHour = it },
                            onMinuteSelected = { selectedMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = addTask) {
                            Text("Tambah Tugas")
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Urutkan:")
                Box {
                    OutlinedButton(onClick = { isSortMenuOpen = true }) {
                        Text(sortOption.label)
                    }
                    DropdownMenu(
                        expanded = isSortMenuOpen,
                        onDismissRequest = { isSortMenuOpen = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    sortOption = option
                                    isSortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            if (sortedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada tugas.",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedTasks, key = Task::id) { task ->
                        TaskCard(
                            task = task,
                            onToggleStatus = { checked ->
                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index >= 0) {
                                    tasks[index] = tasks[index].copy(isCompleted = checked)
                                }
                            },
                            onDelete = {
                                tasks.removeAll { it.id == task.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskInputFields(
    titleInput: String,
    onTitleInputChange: (String) -> Unit,
    yearOptions: List<Int>,
    monthOptions: List<Int>,
    dayOptions: List<Int>,
    hourOptions: List<Int>,
    minuteOptions: List<Int>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    selectedHour: Int,
    selectedMinute: Int,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onDaySelected: (Int) -> Unit,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = titleInput,
            onValueChange = onTitleInputChange,
            label = { Text("Nama Tugas") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Deadline", style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IntDropdown(
                label = "Tahun",
                selected = selectedYear,
                options = yearOptions,
                onSelected = onYearSelected,
                modifier = Modifier.weight(1f)
            )
            IntDropdown(
                label = "Bulan",
                selected = selectedMonth,
                options = monthOptions,
                onSelected = onMonthSelected,
                formatValue = ::twoDigits,
                modifier = Modifier.weight(1f)
            )
            IntDropdown(
                label = "Hari",
                selected = selectedDay,
                options = dayOptions,
                onSelected = onDaySelected,
                formatValue = ::twoDigits,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IntDropdown(
                label = "Jam",
                selected = selectedHour,
                options = hourOptions,
                onSelected = onHourSelected,
                formatValue = ::twoDigits,
                modifier = Modifier.weight(1f)
            )
            IntDropdown(
                label = "Menit",
                selected = selectedMinute,
                options = minuteOptions,
                onSelected = onMinuteSelected,
                formatValue = ::twoDigits,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun IntDropdown(
    label: String,
    selected: Int,
    options: List<Int>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    formatValue: (Int) -> String = Int::toString
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formatValue(selected))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 260.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(formatValue(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggleStatus: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggleStatus
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = "Deadline: ${formatDeadline(task.deadline)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (task.isCompleted) "Status: Selesai" else "Status: Belum Selesai",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedButton(onClick = onDelete) {
                Text("Hapus")
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Daily Task Manager"
    ) {
        MaterialTheme {
            TodoApp()
        }
    }
}
