package com.aspharier.endbadhabit.data

import java.time.LocalDate

import java.util.UUID

data class HabitData(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: LocalDate,
    val lastOpenedDate: LocalDate
)
