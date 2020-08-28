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

    fun setMonthInterval(endMonth: Int = 12) {
        mMonthItems.clear()
        for (month in 1..endMonth) {
            mMonthItems.add(PickerUtils.formatTwoChars(month))
        }
        setItems(mMonthItems)
    }

    fun getMonth() = getSelectedItem()
}