package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerUtils
import me.simple.picker.TextPickerView

class HourPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    init {
        setHourInterval()
    }

    fun setHourInterval(start: Int = 0, end: Int = 24) {
        mItems.clear()
        for (hour in start until end) {
            mItems.add(PickerUtils.formatTwoChars(hour))
        }
        mAdapter.notifyDataSetChanged()
    }

    fun getHour() = mItems[layoutManager.getSelectedItem()]
}