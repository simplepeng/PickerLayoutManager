package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.utils.PickerUtils
import me.simple.picker.widget.TextPickerView

class MonthPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    init {
        setMonthInterval()
    }

    @SuppressWarnings
    fun setMonthInterval(
        startMonth: Int = 1,
        endMonth: Int = 12
    ) {
        mItems.clear()
        for (month in startMonth..endMonth) {
            mItems.add(PickerUtils.formatTwoChars(month))
        }
        adapter?.notifyDataSetChanged()
    }

    fun getMonthStr() = getSelectedItem()

    fun getMonth() = getSelectedItem().toInt()

    fun getMonth(position: Int) = mItems[position].toInt()
}