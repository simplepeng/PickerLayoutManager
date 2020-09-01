package me.simple.picker

import java.util.*

object PickerUtils {

    const val START_YEAR = 1949
    const val START_MONTH = 1
    const val START_DAY = 1

    fun formatTwoChars(text: Int) = String.format("%02d", text)

    private val bigMonthSet = hashSetOf(1, 3, 5, 7, 8, 10, 12)

    fun getDayCountInMonth(year: Int, month: Int): Int {
        if (bigMonthSet.contains(month)) return 31

        if (month == 2) {
            return if (year % 4 == 0) 29 else 28
        }

        return 30
    }

    /**
     * 获取开始的日期
     */
    fun getStartCalendar(): Calendar = Calendar.getInstance().apply {
        set(START_YEAR, START_MONTH, START_DAY)
    }

    /**
     * 获取结束的日期，就是当天
     */
    fun getEndCalendar(): Calendar = Calendar.getInstance()

    fun getEndYear() = getEndCalendar().get(Calendar.YEAR)
    fun getEndMonth() = getEndCalendar().get(Calendar.MONTH) + 1
    fun getEndDay() = getEndCalendar().get(Calendar.DATE)

    fun getYear(calendar: Calendar) = calendar.get(Calendar.YEAR)
    fun getMonth(calendar: Calendar) = calendar.get(Calendar.MONTH) + 1
    fun getDay(calendar: Calendar) = calendar.get(Calendar.DATE)
}