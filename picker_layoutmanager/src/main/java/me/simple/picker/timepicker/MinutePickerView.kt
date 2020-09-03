package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.utils.PickerUtils
import me.simple.picker.widget.TextPickerView

class MinutePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    init {
        setMinuteInterval()
    }

    fun setMinuteInterval(
        start: Int = PickerUtils.START_MINUTE,
        end: Int = PickerUtils.END_MINUTE
    ) {
        mItems.clear()
        for (minute in start..end) {
            mItems.add(PickerUtils.formatTwoChars(minute))
        }
        adapter!!.notifyDataSetChanged()
    }

    fun getMinuteStr() = getSelectedItem()

    fun getMinute() = getMinuteStr().toInt()
}