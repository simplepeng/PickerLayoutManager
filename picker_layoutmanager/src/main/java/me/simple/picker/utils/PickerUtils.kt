package me.simple.picker.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.widget.Toast
import java.util.*

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

val Float.dp: Float
    get() = Resources.getSystem().displayMetrics.density * this + 0.5f

object PickerUtils {

    const val START_YEAR = 1949
    const val START_MONTH = 1
    const val START_DAY = 1

    const val START_HOUR = 0
    const val END_HOUR = 23
    const val START_MINUTE = 0
    const val END_MINUTE = 59

    const val SELECTED_TEXT_COLOR = Color.BLACK
    const val UNSELECTED_TEXT_COLOR = Color.LTGRAY
    const val SELECTED_TEXT_SIZE = 14f
    const val UNSELECTED_TEXT_SIZE = 14f
    const val SELECTED_IS_BOLD = false

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
     * 1949-1-1
     */
    fun getStartCalendar(): Calendar = Calendar.getInstance().apply {
        set(
            START_YEAR,
            START_MONTH - 1,
            START_DAY
        )
    }

    /**
     * 获取结束的日期，就是当天
     */
    fun getEndCalendar(): Calendar = Calendar.getInstance()

    fun getEndYear() = getYear(getEndCalendar())
    fun getEndMonth() = getMonth(getEndCalendar())
    fun getEndDay() = getDay(getEndCalendar())

    fun getYear(calendar: Calendar) = calendar.get(Calendar.YEAR)
    fun getMonth(calendar: Calendar) = calendar.get(Calendar.MONTH) + 1
    fun getDay(calendar: Calendar) = calendar.get(Calendar.DATE)
}