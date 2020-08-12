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

    private var mOnSelected: ((hour: String, minute: String, second: String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        weightSum = 3f

        addView(mHourPickerView)
        addView(mMinutePickerView)
        addView(mSecondPickerView)

        setDivider(mHourPickerView)
        setDivider(mMinutePickerView)
        setDivider(mSecondPickerView)

        mHourPickerView.addOnSelectedItemListener {
            dispatchOnSelectedItem()
        }
        mMinutePickerView.addOnSelectedItemListener {
            dispatchOnSelectedItem()
        }
        mSecondPickerView.addOnSelectedItemListener {
            dispatchOnSelectedItem()
        }
    }

    private fun dispatchOnSelectedItem() {
        val hour = mHourPickerView.getHour()
        val minute = mMinutePickerView.getMinute()
        val second = mSecondPickerView.getSecond()
        mOnSelected?.invoke(hour, minute, second)
    }

    fun getHourMinuteSecond() = arrayOf(
        mHourPickerView.getHour(),
        mMinutePickerView.getMinute(),
        mSecondPickerView.getSecond()
    )

    fun setOnTimeSelectedListener(onSelected: (hour: String, minute: String, second: String) -> Unit) {
        this.mOnSelected = onSelected
    }
}