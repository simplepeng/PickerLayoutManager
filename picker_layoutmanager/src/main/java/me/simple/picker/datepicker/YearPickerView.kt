package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.utils.PickerUtils
import me.simple.picker.widget.TextPickerView

class YearPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    fun setYearInterval(
        startYear: Int = PickerUtils.START_YEAR,
        endYear: Int = PickerUtils.getEndYear()
    ) {
        mItems.clear()
        for (year in startYear..endYear) {
            mItems.add(year.toString())
        }
        adapter?.notifyDataSetChanged()
    }

    fun getYear(position: Int): Int {
        return mItems[position].toInt()
    }

    fun getYear(): Int {
        return getYearStr().toInt()
    }

    fun getYearStr(): String {
        return getSelectedItem()
    }
}