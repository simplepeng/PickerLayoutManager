package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.utils.PickerUtils
import me.simple.picker.widget.TextPickerView

class HourPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    init {
        setHourInterval()
    }

    @SuppressWarnings
    fun setHourInterval(
        start: Int = PickerUtils.START_HOUR,
        end: Int = PickerUtils.END_HOUR
    ) {
        mItems.clear()
        for (hour in start..end) {
            mItems.add(PickerUtils.formatTwoChars(hour))
        }
        adapter?.notifyDataSetChanged()
    }

    fun getHourStr() = getSelectedItem()

    fun getHour() = getHourStr().toInt()
}