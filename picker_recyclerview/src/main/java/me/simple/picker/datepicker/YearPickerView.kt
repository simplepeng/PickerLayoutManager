package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.TextPickerView
import java.util.*

class YearPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerView(context, attrs, defStyleAttr) {

    private val mYearItems = mutableListOf<String>()

    fun setYearInterval(start: Int = 1949, end: Int = Calendar.getInstance().get(Calendar.YEAR)) {
        mYearItems.clear()
        for (year in start..end) {
            mYearItems.add(year.toString())
        }
        setItems(mYearItems)
    }

    fun getYear(): String {
        val position = mLayoutManager.getSelectedItem()
        return mYearItems[position]
    }
}