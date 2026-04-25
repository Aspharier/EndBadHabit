package com.aspharier.endbadhabit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.aspharier.endbadhabit.ui.viewmodel.HabitUiState

import com.aspharier.endbadhabit.data.HabitData

@Composable
fun HomeScreen(
    uiState: HabitUiState,
    allHabits: List<HabitData>,
    onCreateHabit: (String) -> Unit,
    onDeleteHabit: () -> Unit,
    onRestartStreak: () -> Unit,
    onThemePickerOpen: () -> Unit,
    onHabitListOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Theme picker icon — top right
        IconButton(
            onClick = onThemePickerOpen,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }

        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)))
                    .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200)))
            },
            modifier = Modifier.fillMaxSize(),
            label = "state_transition"
        ) { state ->
            when (state) {
                is HabitUiState.Loading -> LoadingContent()
                is HabitUiState.NoHabit -> CreateHabitContent(onCreateHabit = onCreateHabit)
                is HabitUiState.ActiveStreak -> ActiveStreakContent(
                    habitName = state.habitName,
                    streakDays = state.streakDays,
                    onDeleteHabit = onDeleteHabit,
                    onHabitListOpen = onHabitListOpen
                )
                is HabitUiState.StreakBroken -> StreakBrokenContent(
                    habitName = state.habitName,
                    previousStreak = state.previousStreak,
                    onRestartStreak = onRestartStreak,
                    onDeleteHabit = onDeleteHabit,
                    onHabitListOpen = onHabitListOpen
                )
            }
        }
    }
}

// ── Loading ─────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }
}

// ── Create Habit ────────────────────────────────────────────────

@Composable
private fun CreateHabitContent(onCreateHabit: (String) -> Unit) {
    var habitName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle label
        Text(
            text = "end it.",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "what bad habit\ndo you want to end?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = habitName,
            onValueChange = { habitName = it },
            placeholder = {
                Text(
                    text = "e.g. smoking",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (habitName.isNotBlank()) {
                    onCreateHabit(habitName.trim())
                }
            },
            enabled = habitName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "start the streak",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ── Active Streak ───────────────────────────────────────────────

@Composable
private fun ActiveStreakContent(
    habitName: String,
    streakDays: Long,
    onDeleteHabit: () -> Unit,
    onHabitListOpen: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Lottie fire animation
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Fire.json")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        // Fire animation — big and prominent
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(280.dp)
            )
        }

        // Streak number — overlapping slightly with fire
        Text(
            text = "$streakDays",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 120.sp,
                letterSpacing = (-4).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (streakDays == 1L) "day" else "days",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "without",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = habitName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(0.3f))

        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "delete",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Button(
                onClick = onHabitListOpen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "all habits",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "delete habit?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "this will reset your streak and remove \"$habitName\" permanently.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteHabit()
                    }
                ) {
                    Text(
                        text = "delete",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Streak Broken ───────────────────────────────────────────────

@Composable
private fun StreakBrokenContent(
    habitName: String,
    previousStreak: Long,
    onRestartStreak: () -> Unit,
    onDeleteHabit: () -> Unit,
    onHabitListOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "streak\nbroken",
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFFEF4444),
            textAlign = TextAlign.Center,
            lineHeight = 56.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "you had a $previousStreak day streak",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "of ending \"$habitName\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Try again button — restart same habit
        Button(
            onClick = onRestartStreak,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "try again",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDeleteHabit,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = "delete",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Button(
                onClick = onHabitListOpen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = "all habits",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
