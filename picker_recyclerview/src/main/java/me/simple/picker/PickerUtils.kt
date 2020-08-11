package me.simple.picker

import java.util.*

object PickerUtils {

    fun formatTwoChars(text: Int) = String.format("%02d", text)

    private val bigMonthSet = hashSetOf(1, 3, 5, 7, 8, 10, 12)

    fun getDayCountInMonth(year: Int, month: Int): Int {
        if (bigMonthSet.contains(month)) return 31

        if (month == 2) {
            return if (year % 4 == 0) 29 else 28
        }

        return 30
    }

    fun getTodayCalendar() = Calendar.getInstance()
}