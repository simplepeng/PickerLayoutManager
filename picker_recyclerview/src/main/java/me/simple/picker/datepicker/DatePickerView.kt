package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class DatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val mYearPickerView = YearPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }
    private val mMonthPickerView = MonthPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }

    init {
        orientation = HORIZONTAL
        weightSum = 3f

        mYearPickerView.setYearInterval()

        addView(mYearPickerView)
        addView(mMonthPickerView)
    }

    private fun generateChildLayoutParams(): LayoutParams {
        val lp = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT
        )
        lp.weight = 1f
        return lp
    }
}