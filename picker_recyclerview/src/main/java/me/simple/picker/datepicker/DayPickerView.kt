package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerUtils
import me.simple.picker.TextPickerView

class DayPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    private val mDayItems = mutableListOf<String>()

    fun setDayInterval(endDay: Int = 31) {
        mDayItems.clear()
        for (day in 1..endDay) {
            mDayItems.add(PickerUtils.formatTwoChars(day))
        }
        setItems(mDayItems)
    }

    fun setYearAndMonth(year: Int, month: Int) {
        setDayInterval(PickerUtils.getDayCountInMonth(year, month))
    }

    fun getDay() = getSelectedItem()
}