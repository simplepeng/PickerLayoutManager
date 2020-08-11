package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLinearLayout
import me.simple.picker.PickerRecyclerView
import me.simple.picker.R

class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PickerLinearLayout(context, attrs, defStyleAttr) {

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

        initAttrs(attrs)
    }

    override fun initAttrs(attrs: AttributeSet?) {
        super.initAttrs(attrs)

//        setDivider(mHourPickerView)
//        setDivider(mMinutePickerView)
//        setDivider(mSecondPickerView)
    }

    private fun setDivider(pickerView: PickerRecyclerView) {
        pickerView.addItemDecoration(
            PickerItemDecoration(
                mDividerColor,
                mDividerSize,
                mDividerPadding
            )
        )
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