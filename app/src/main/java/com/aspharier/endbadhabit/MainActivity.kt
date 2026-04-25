package com.aspharier.endbadhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aspharier.endbadhabit.ui.screens.HomeScreen
import com.aspharier.endbadhabit.ui.screens.ThemePickerSheet
import com.aspharier.endbadhabit.ui.screens.HabitListSheet
import com.aspharier.endbadhabit.ui.screens.CreateHabitSheet
import com.aspharier.endbadhabit.ui.theme.EndBadHabitTheme
import com.aspharier.endbadhabit.ui.viewmodel.MainViewModel
import com.aspharier.endbadhabit.ui.viewmodel.HabitUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val appTheme by viewModel.appTheme.collectAsState()
            val uiState by viewModel.uiState.collectAsState()
            val allHabits by viewModel.allHabits.collectAsState()
            var showThemePicker by remember { mutableStateOf(false) }
            var showHabitList by remember { mutableStateOf(false) }
            var showCreateHabit by remember { mutableStateOf(false) }

            EndBadHabitTheme(appTheme = appTheme) {
                HomeScreen(
                    uiState = uiState,
                    allHabits = allHabits,
                    onCreateHabit = viewModel::createHabit,
                    onDeleteHabit = viewModel::deleteHabit,
                    onRestartStreak = viewModel::restartStreak,
                    onThemePickerOpen = { showThemePicker = true },
                    onHabitListOpen = { showHabitList = true }
                )

                if (showThemePicker) {
                    ThemePickerSheet(
                        currentTheme = appTheme,
                        onThemeSelected = viewModel::changeTheme,
                        onDismiss = { showThemePicker = false }
                    )
                }

                if (showHabitList) {
                    val currentState = uiState
                    val selectedId = when (currentState) {
                        is HabitUiState.ActiveStreak -> currentState.habitId
                        is HabitUiState.StreakBroken -> currentState.habitId
                        else -> null
                    }
                    
                    HabitListSheet(
                        habits = allHabits,
                        currentSelectedId = selectedId,
                        onHabitSelected = viewModel::selectHabit,
                        onCreateNew = {
                            showHabitList = false
                            showCreateHabit = true
                        },
                        onDismiss = { showHabitList = false }
                    )
                }

                if (showCreateHabit) {
                    CreateHabitSheet(
                        onCreateHabit = { name ->
                            viewModel.createHabit(name)
                            showCreateHabit = false
                        },
                        onDismiss = { showCreateHabit = false }
                    )
                }
            }
        }
    }
}