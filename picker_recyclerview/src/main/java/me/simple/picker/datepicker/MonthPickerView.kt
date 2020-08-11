package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerUtils
import me.simple.picker.TextPickerView

class MonthPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    private val mMonthItems = mutableListOf<String>()

    init {
        setMonthInterval()
    }

    fun setMonthInterval(start: Int = 1, end: Int = 12) {
        mMonthItems.clear()
        for (day in start..end) {
            mMonthItems.add(PickerUtils.formatTwoChars(day))
        }
        setItems(mMonthItems)
    }

    fun getMonth() = mMonthItems[mLayoutManager.getSelectedItem()]
}