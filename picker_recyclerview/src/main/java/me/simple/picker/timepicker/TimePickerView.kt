package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val mHourPickerView = HourPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }
    private val mMinutePickerView = MinutePickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }
    private val mSecondPickerView = SecondPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }

    init {
        orientation = HORIZONTAL
        weightSum = 3f

        addView(mHourPickerView)
        addView(mMinutePickerView)
        addView(mSecondPickerView)
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