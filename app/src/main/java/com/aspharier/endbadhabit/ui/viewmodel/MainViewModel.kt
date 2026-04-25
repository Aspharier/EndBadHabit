package com.aspharier.endbadhabit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aspharier.endbadhabit.data.HabitData
import com.aspharier.endbadhabit.data.HabitDataStore
import com.aspharier.endbadhabit.data.StreakCalculator
import com.aspharier.endbadhabit.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HabitUiState {
    /** No habits exist across the whole app — show the create screen */
    data object NoHabit : HabitUiState()

    /** Selected habit is active, streak is going */
    data class ActiveStreak(
        val habitId: String,
        val habitName: String,
        val streakDays: Long
    ) : HabitUiState()

    /** User missed a day, streak is broken for the selected habit */
    data class StreakBroken(
        val habitId: String,
        val habitName: String,
        val previousStreak: Long
    ) : HabitUiState()

    /** Loading state */
    data object Loading : HabitUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = HabitDataStore(application)

    private val _uiState = MutableStateFlow<HabitUiState>(HabitUiState.Loading)
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private val _appTheme = MutableStateFlow(AppTheme.MIDNIGHT)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()
    
    val allHabits: StateFlow<List<HabitData>> = dataStore.habitsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Observe theme
        viewModelScope.launch {
            dataStore.themeFlow.collect { themeKey ->
                _appTheme.value = AppTheme.fromKey(themeKey)
            }
        }

        // Observe selected habit
        viewModelScope.launch {
            combine(dataStore.habitsFlow, dataStore.selectedHabitIdFlow) { habits, selectedId ->
                if (habits.isEmpty()) return@combine null
                habits.find { it.id == selectedId } ?: habits.firstOrNull()
            }.collect { habit ->
                _uiState.value = computeState(habit)
            }
        }
    }

    private suspend fun computeState(habit: HabitData?): HabitUiState {
        if (habit == null) return HabitUiState.NoHabit

        return if (StreakCalculator.isStreakBroken(habit.lastOpenedDate)) {
            // Streak is broken — show the broken state
            val previousStreak = StreakCalculator.calculateStreak(habit.startDate)
            HabitUiState.StreakBroken(
                habitId = habit.id,
                habitName = habit.name,
                previousStreak = previousStreak
            )
        } else {
            // Streak is alive — update last opened and show active state
            dataStore.updateLastOpened()
            val streak = StreakCalculator.calculateStreak(habit.startDate)
            HabitUiState.ActiveStreak(
                habitId = habit.id,
                habitName = habit.name,
                streakDays = streak
            )
        }
    }

    fun createHabit(name: String) {
        viewModelScope.launch {
            dataStore.createHabit(name)
        }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            dataStore.deleteHabit()
        }
    }

    fun restartStreak() {
        viewModelScope.launch {
            dataStore.restartStreak()
        }
    }
    
    fun selectHabit(id: String) {
        viewModelScope.launch {
            dataStore.selectHabit(id)
        }
    }

    fun changeTheme(theme: AppTheme) {
        viewModelScope.launch {
            dataStore.setTheme(theme.key)
        }
    }
}
