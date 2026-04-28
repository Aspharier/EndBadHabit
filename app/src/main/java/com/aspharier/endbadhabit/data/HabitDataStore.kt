package com.aspharier.endbadhabit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID
import androidx.glance.appwidget.updateAll
import com.aspharier.endbadhabit.widget.HabitWidget
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_prefs")

class HabitDataStore(private val context: Context) {

    companion object {
        // Legacy keys
        private val LEGACY_HABIT_NAME = stringPreferencesKey("habit_name")
        private val LEGACY_START_DATE = longPreferencesKey("start_date")
        private val LEGACY_LAST_OPENED_DATE = longPreferencesKey("last_opened_date")
        
        // New keys
        private val HABITS_JSON = stringPreferencesKey("habits_json")
        private val SELECTED_HABIT_ID = stringPreferencesKey("selected_habit_id")
        private val SELECTED_THEME = stringPreferencesKey("selected_theme")
        private val WIDGET_HABIT_MAPPING = stringPreferencesKey("widget_habit_mapping")
    }

    /**
     * Get all habits as a Flow.
     */
    val habitsFlow: Flow<List<HabitData>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[HABITS_JSON]
        if (jsonString != null) {
            parseHabitsJson(jsonString)
        } else {
            // Check for legacy single habit migration
            val legacyName = prefs[LEGACY_HABIT_NAME]
            val legacyStart = prefs[LEGACY_START_DATE]
            val legacyOpened = prefs[LEGACY_LAST_OPENED_DATE]
            
            if (legacyName != null && legacyStart != null && legacyOpened != null) {
                val legacyHabit = HabitData(
                    id = UUID.randomUUID().toString(),
                    name = legacyName,
                    startDate = LocalDate.ofEpochDay(legacyStart),
                    lastOpenedDate = LocalDate.ofEpochDay(legacyOpened)
                )
                listOf(legacyHabit)
            } else {
                emptyList()
            }
        }
    }
    
    /**
     * Get the currently selected habit ID.
     */
    val selectedHabitIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_HABIT_ID]
    }

    /**
     * Get the selected theme key as a Flow.
     */
    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_THEME] ?: "midnight"
    }

    /**
     * Create a new habit. Sets start date and last opened date to today.
     * Also selects it.
     */
    suspend fun createHabit(name: String) {
        val today = LocalDate.now().toEpochDay()
        val newHabit = HabitData(
            id = UUID.randomUUID().toString(),
            name = name,
            startDate = LocalDate.now(),
            lastOpenedDate = LocalDate.now()
        )
        
        context.dataStore.edit { prefs ->
            val currentHabits = getCurrentHabits(prefs)
            currentHabits.add(newHabit)
            prefs[HABITS_JSON] = buildHabitsJson(currentHabits)
            prefs[SELECTED_HABIT_ID] = newHabit.id
            
            // Clean up legacy keys if they exist
            prefs.remove(LEGACY_HABIT_NAME)
            prefs.remove(LEGACY_START_DATE)
            prefs.remove(LEGACY_LAST_OPENED_DATE)
        }
        updateWidget()
    }

    /**
     * Delete the currently selected habit.
     */
    suspend fun deleteHabit() {
        context.dataStore.edit { prefs ->
            val selectedId = prefs[SELECTED_HABIT_ID] ?: return@edit
            val currentHabits = getCurrentHabits(prefs)
            currentHabits.removeAll { it.id == selectedId }
            
            prefs[HABITS_JSON] = buildHabitsJson(currentHabits)
            
            if (currentHabits.isNotEmpty()) {
                prefs[SELECTED_HABIT_ID] = currentHabits.first().id
            } else {
                prefs.remove(SELECTED_HABIT_ID)
            }
        }
        updateWidget()
    }

    /**
     * Update last opened date to today for the selected habit.
     */
    suspend fun updateLastOpened() {
        context.dataStore.edit { prefs ->
            val selectedId = prefs[SELECTED_HABIT_ID] ?: return@edit
            val currentHabits = getCurrentHabits(prefs)
            
            val index = currentHabits.indexOfFirst { it.id == selectedId }
            if (index != -1) {
                currentHabits[index] = currentHabits[index].copy(
                    lastOpenedDate = LocalDate.now()
                )
                prefs[HABITS_JSON] = buildHabitsJson(currentHabits)
            }
        }
        updateWidget()
    }

    /**
     * Restart the streak for the selected habit.
     */
    suspend fun restartStreak() {
        context.dataStore.edit { prefs ->
            val selectedId = prefs[SELECTED_HABIT_ID] ?: return@edit
            val currentHabits = getCurrentHabits(prefs)
            
            val index = currentHabits.indexOfFirst { it.id == selectedId }
            if (index != -1) {
                currentHabits[index] = currentHabits[index].copy(
                    startDate = LocalDate.now(),
                    lastOpenedDate = LocalDate.now()
                )
                prefs[HABITS_JSON] = buildHabitsJson(currentHabits)
            }
        }
        updateWidget()
    }
    
    suspend fun selectHabit(id: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_HABIT_ID] = id
        }
        updateWidget()
    }

    /**
     * Set the selected theme.
     */
    suspend fun setTheme(themeKey: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = themeKey
        }
    }
    
    /**
     * Save mapping between widget ID and habit ID.
     */
    suspend fun saveWidgetHabitMapping(appWidgetId: Int, habitId: String) {
        context.dataStore.edit { prefs ->
            val currentMapping = prefs[WIDGET_HABIT_MAPPING] ?: "{}"
            val json = JSONObject(currentMapping)
            json.put(appWidgetId.toString(), habitId)
            prefs[WIDGET_HABIT_MAPPING] = json.toString()
        }
    }

    /**
     * Get habit ID for a specific widget ID.
     */
    suspend fun getHabitIdForWidget(appWidgetId: Int): String? {
        val prefs = context.dataStore.data.firstOrNull() ?: return null
        val currentMapping = prefs[WIDGET_HABIT_MAPPING] ?: "{}"
        val json = JSONObject(currentMapping)
        return if (json.has(appWidgetId.toString())) json.getString(appWidgetId.toString()) else null
    }
    
    private suspend fun updateWidget() {
        try {
            HabitWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // --- Helpers ---
    
    private fun getCurrentHabits(prefs: Preferences): MutableList<HabitData> {
        val jsonString = prefs[HABITS_JSON]
        if (jsonString != null) {
            return parseHabitsJson(jsonString).toMutableList()
        }
        
        // Check legacy
        val legacyName = prefs[LEGACY_HABIT_NAME]
        val legacyStart = prefs[LEGACY_START_DATE]
        val legacyOpened = prefs[LEGACY_LAST_OPENED_DATE]
        
        if (legacyName != null && legacyStart != null && legacyOpened != null) {
            val legacyHabit = HabitData(
                id = UUID.randomUUID().toString(),
                name = legacyName,
                startDate = LocalDate.ofEpochDay(legacyStart),
                lastOpenedDate = LocalDate.ofEpochDay(legacyOpened)
            )
            // Save legacy as selected if we migrate right now
            return mutableListOf(legacyHabit)
        }
        
        return mutableListOf()
    }
    
    private fun parseHabitsJson(jsonString: String): List<HabitData> {
        val list = mutableListOf<HabitData>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HabitData(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        startDate = LocalDate.ofEpochDay(obj.getLong("startDate")),
                        lastOpenedDate = LocalDate.ofEpochDay(obj.getLong("lastOpenedDate"))
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
    
    private fun buildHabitsJson(habits: List<HabitData>): String {
        val array = JSONArray()
        for (habit in habits) {
            val obj = JSONObject()
            obj.put("id", habit.id)
            obj.put("name", habit.name)
            obj.put("startDate", habit.startDate.toEpochDay())
            obj.put("lastOpenedDate", habit.lastOpenedDate.toEpochDay())
            array.put(obj)
        }
        return array.toString()
    }
}
