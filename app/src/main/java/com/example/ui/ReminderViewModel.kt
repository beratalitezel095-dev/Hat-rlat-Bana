package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ReminderRepository
import com.example.model.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ReminderTab {
    ACTIVE,
    COMPLETED,
    ALL
}

data class ReminderStats(
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val snoozedCount: Int = 0
)

class ReminderViewModel(
    private val repository: ReminderRepository
) : ViewModel() {

    val selectedTab = MutableStateFlow(ReminderTab.ACTIVE)
    val selectedCategory = MutableStateFlow("Tümü")
    val searchQuery = MutableStateFlow("")

    val allReminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredReminders: StateFlow<List<Reminder>> = combine(
        allReminders,
        selectedTab,
        selectedCategory,
        searchQuery
    ) { list, tab, category, query ->
        list.filter { reminder ->
            val matchesTab = when (tab) {
                ReminderTab.ACTIVE -> !reminder.isCompleted
                ReminderTab.COMPLETED -> reminder.isCompleted
                ReminderTab.ALL -> true
            }

            val matchesCategory = category == "Tümü" || reminder.category.equals(category, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    reminder.title.contains(query, ignoreCase = true) ||
                    reminder.notes.contains(query, ignoreCase = true)

            matchesTab && matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<ReminderStats> = allReminders.combine(MutableStateFlow(Unit)) { list, _ ->
        ReminderStats(
            totalCount = list.size,
            activeCount = list.count { !it.isCompleted },
            completedCount = list.count { it.isCompleted },
            snoozedCount = list.count { it.isSnoozed && !it.isCompleted }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReminderStats())

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.addReminder(reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }

    fun toggleCompleted(reminder: Reminder) {
        viewModelScope.launch {
            repository.toggleCompleted(reminder)
        }
    }

    fun snoozeReminder(reminder: Reminder, snoozeMinutes: Int) {
        viewModelScope.launch {
            repository.snoozeReminder(reminder, snoozeMinutes)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun setTab(tab: ReminderTab) {
        selectedTab.value = tab
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    fun setQuery(query: String) {
        searchQuery.value = query
    }

    class Factory(private val repository: ReminderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                return ReminderViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
