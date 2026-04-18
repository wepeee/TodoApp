package org.example.todoapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoAppAndroid()
            }
        }
    }
}

data class Task(
    val id: Int,
    val title: String,
    val deadline: LocalDateTime,
    val isCompleted: Boolean = false
)

enum class SortOption(val label: String) {
    DEADLINE_ASC("Deadline"),
    DEADLINE_DESC("Deadline (Desc)"),
    STATUS("Status")
}

private val deadlineFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val headerDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

private val taskListSaver = listSaver<SnapshotStateList<Task>, List<Any>>(
    save = { taskList ->
        taskList.map { task ->
            listOf(
                task.id,
                task.title,
                task.deadline.format(deadlineFormatter),
                task.isCompleted
            )
        }
    },
    restore = { restored ->
        mutableStateListOf<Task>().apply {
            addAll(
                restored.map { raw ->
                    Task(
                        id = raw[0] as Int,
                        title = raw[1] as String,
                        deadline = LocalDateTime.parse(raw[2] as String, deadlineFormatter),
                        isCompleted = raw[3] as Boolean
                    )
                }
            )
        }
    }
)

private fun formatDeadline(deadline: LocalDateTime): String = deadline.format(deadlineFormatter)

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

@Composable
fun TodoAppAndroid() {
    val initialNow = remember { LocalDateTime.now() }
    val tasks = rememberSaveable(saver = taskListSaver) { mutableStateListOf<Task>() }

    var titleInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var nextId by rememberSaveable {
        mutableStateOf(
            if (tasks.isEmpty()) 1 else tasks.maxOf(Task::id) + 1
        )
    }

    val yearOptions = remember(initialNow.year) { (initialNow.year..initialNow.year + 5).toList() }
    val monthOptions = remember { (1..12).toList() }
    val hourOptions = remember { (0..23).toList() }
    val minuteOptions = remember { (0..55 step 5).toList() }

    var selectedYear by rememberSaveable { mutableStateOf(initialNow.year) }
    var selectedMonth by rememberSaveable { mutableStateOf(initialNow.monthValue) }
    var selectedDay by rememberSaveable { mutableStateOf(initialNow.dayOfMonth) }
    var selectedHour by rememberSaveable { mutableStateOf(initialNow.hour) }
    var selectedMinute by rememberSaveable { mutableStateOf((initialNow.minute / 5) * 5) }

    val maxDayInMonth = remember(selectedYear, selectedMonth) {
        YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    }
    val clampedSelectedDay = selectedDay.coerceAtMost(maxDayInMonth)
    val dayOptions = remember(maxDayInMonth) { (1..maxDayInMonth).toList() }

    LaunchedEffect(selectedDay, maxDayInMonth) {
        if (selectedDay != clampedSelectedDay) {
            selectedDay = clampedSelectedDay
        }
    }

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showCompleted by rememberSaveable { mutableStateOf(true) }

    var sortOptionName by rememberSaveable { mutableStateOf(SortOption.DEADLINE_ASC.name) }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    val sortOption = SortOption.valueOf(sortOptionName)

    val sortedTasks by remember(sortOption) {
        derivedStateOf {
            when (sortOption) {
                SortOption.DEADLINE_ASC -> tasks.sortedBy(Task::deadline)
                SortOption.DEADLINE_DESC -> tasks.sortedByDescending(Task::deadline)
                SortOption.STATUS -> tasks.sortedWith(compareBy<Task> { it.isCompleted }.thenBy(Task::deadline))
            }
        }
    }
    val activeTasks = sortedTasks.filterNot(Task::isCompleted)
    val completedTasks = sortedTasks.filter(Task::isCompleted)

    fun tryAddTask(): Boolean {
        val trimmedTitle = titleInput.trim()
        val selectedDeadline = LocalDateTime.of(
            selectedYear,
            selectedMonth,
            clampedSelectedDay,
            selectedHour,
            selectedMinute
        )

        errorMessage = when {
            trimmedTitle.isEmpty() -> "Judul tugas tidak boleh kosong."
            selectedDeadline.isBefore(LocalDateTime.now()) -> "Deadline tidak boleh di masa lalu."
            else -> null
        }

        if (errorMessage != null) return false

        tasks.add(
            Task(
                id = nextId,
                title = trimmedTitle,
                deadline = selectedDeadline
            )
        )
        nextId += 1
        titleInput = ""
        errorMessage = null
        return true
    }

    Scaffold(
        containerColor = Color(0xFFF4F6FB),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1A9AF5),
                contentColor = Color.White
            ) {
                Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(
                    dateText = "Today, ${LocalDate.now().format(headerDateFormatter)}",
                    completed = completedTasks.size,
                    total = tasks.size
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3C3C3C)
                    )
                    Box {
                        TextButton(onClick = { isSortMenuOpen = true }) {
                            Text("Sort: ${sortOption.label}")
                        }
                        DropdownMenu(
                            expanded = isSortMenuOpen,
                            onDismissRequest = { isSortMenuOpen = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        sortOptionName = option.name
                                        isSortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (activeTasks.isEmpty()) {
                item {
                    EmptySectionCard(message = "Belum ada task aktif.")
                }
            } else {
                items(activeTasks, key = Task::id) { task ->
                    TaskItemCard(
                        task = task,
                        onToggleStatus = { checked ->
                            val index = tasks.indexOfFirst { it.id == task.id }
                            if (index >= 0) {
                                tasks[index] = tasks[index].copy(isCompleted = checked)
                            }
                        },
                        onDelete = { tasks.removeAll { it.id == task.id } }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF3C3C3C)
                    )
                    TextButton(onClick = { showCompleted = !showCompleted }) {
                        Text(if (showCompleted) "Hide" else "Show")
                    }
                }
            }

            if (showCompleted) {
                if (completedTasks.isEmpty()) {
                    item {
                        EmptySectionCard(message = "Belum ada task selesai.")
                    }
                } else {
                    items(completedTasks, key = Task::id) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleStatus = { checked ->
                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index >= 0) {
                                    tasks[index] = tasks[index].copy(isCompleted = checked)
                                }
                            },
                            onDelete = { tasks.removeAll { it.id == task.id } }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                errorMessage = null
            },
            title = { Text("Tambah Tugas Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Nama Tugas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Deadline", style = MaterialTheme.typography.labelLarge)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IntDropdown(
                            label = "Tahun",
                            selected = selectedYear,
                            options = yearOptions,
                            onSelected = { selectedYear = it },
                            modifier = Modifier.weight(1f)
                        )
                        IntDropdown(
                            label = "Bulan",
                            selected = selectedMonth,
                            options = monthOptions,
                            onSelected = { selectedMonth = it },
                            formatValue = ::twoDigits,
                            modifier = Modifier.weight(1f)
                        )
                        IntDropdown(
                            label = "Hari",
                            selected = clampedSelectedDay,
                            options = dayOptions,
                            onSelected = { selectedDay = it },
                            formatValue = ::twoDigits,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IntDropdown(
                            label = "Jam",
                            selected = selectedHour,
                            options = hourOptions,
                            onSelected = { selectedHour = it },
                            formatValue = ::twoDigits,
                            modifier = Modifier.weight(1f)
                        )
                        IntDropdown(
                            label = "Menit",
                            selected = selectedMinute,
                            options = minuteOptions,
                            onSelected = { selectedMinute = it },
                            formatValue = ::twoDigits,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tryAddTask()) {
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        errorMessage = null
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(
    dateText: String,
    completed: Int,
    total: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A9AF5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color(0xFF1A9AF5),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$completed/$total",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = dateText,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TaskItemCard(
    task: Task,
    onToggleStatus: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggleStatus
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = formatDeadline(task.deadline),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Text("Hapus")
                }
            }
        }
    }
}

@Composable
private fun EmptySectionCard(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 86.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
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

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6C6C6C)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(formatValue(selected), maxLines = 1)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 220.dp)
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

@Preview(showBackground = true)
@Composable
fun TodoAppAndroidPreview() {
    MaterialTheme {
        TodoAppAndroid()
    }
}
