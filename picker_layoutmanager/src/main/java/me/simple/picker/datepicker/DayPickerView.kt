package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.utils.PickerUtils
import me.simple.picker.widget.TextPickerView

open class DayPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    init {
        setDayInterval()
    }

    fun setDayInterval(
        startDay: Int = 1,
        endDay: Int = 31
    ) {
        mItems.clear()
        for (day in startDay..endDay) {
            mItems.add(PickerUtils.formatTwoChars(day))
        }
        adapter?.notifyDataSetChanged()
    }

    fun setDayIntervalByMonth(
        year: Int,
        month: Int,
        startDay: Int = 1
    ) {
        setDayInterval(startDay, endDay = PickerUtils.getDayCountInMonth(year, month))
    }

    fun getDayStr() = getSelectedItem()

    fun getDay() = getDayStr().toInt()

    fun selectedItem(number: Int) {
        selectedItem(PickerUtils.formatTwoChars(number))
    }
}