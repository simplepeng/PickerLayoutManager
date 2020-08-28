package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerUtils
import me.simple.picker.TextPickerView

class YearPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    private val mYearItems = mutableListOf<String>()

    fun setYearInterval(
        startYear: Int = 1949,
        endYear: Int = PickerUtils.getEndYear()
    ) {
        mYearItems.clear()
        for (year in startYear..endYear) {
            mYearItems.add(year.toString())
        }
        setItems(mYearItems)
    }

    fun getYear(): String? {
        return getSelectedItem()
    }
}