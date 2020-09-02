package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerLinearLayout
import me.simple.picker.PickerUtils

typealias OnTimeSelectedListener = (hour: String, minute: String) -> Unit

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

    private var mOnTimeSelectedListener: OnTimeSelectedListener? = null

    private var mStartHour: Int = PickerUtils.START_HOUR
    private var mStartMinute: Int = PickerUtils.START_MINUTE
    private var mEndHour: Int = PickerUtils.END_HOUR
    private var mEndMinute: Int = PickerUtils.END_MINUTE

    init {
        orientation = HORIZONTAL
        weightSum = 2f

        addView(mHourPickerView)
        addView(mMinutePickerView)

        setDivider(mHourPickerView)
        setDivider(mMinutePickerView)

        mHourPickerView.addOnSelectedItemListener {
            val hour = mHourPickerView.getHour()

            when (hour) {
                mStartHour -> {
                    mMinutePickerView.setMinuteInterval(mStartMinute)
                }
                mEndHour -> {
                    mMinutePickerView.setMinuteInterval(end = mEndMinute)
                }
                else -> {
                    mMinutePickerView.setMinuteInterval()
                }
            }

            dispatchOnSelectedItem()
        }
        mMinutePickerView.addOnSelectedItemListener {
            dispatchOnSelectedItem()
        }

        setTimeInterval(2, 10, 22, 20)
    }

    @SuppressWarnings
    fun setTimeInterval(
        startHour: Int = PickerUtils.START_HOUR,
        startMinute: Int = PickerUtils.START_MINUTE,
        endHour: Int = PickerUtils.END_HOUR,
        endMinute: Int = PickerUtils.END_MINUTE
    ) {
        this.mStartHour = startHour
        this.mStartMinute = startMinute
        this.mEndHour = endHour
        this.mEndMinute = endMinute

        mHourPickerView.setHourInterval(startHour, endHour)
        mMinutePickerView.setMinuteInterval()
    }

    private fun dispatchOnSelectedItem() {
//        val hour = mHourPickerView.getHourStr()
//        val minute = mMinutePickerView.getMinuteStr()
//        val second = mSecondPickerView.getSecond()
    }

    fun getTime() = arrayOf(
        mHourPickerView.getHourStr(),
        mMinutePickerView.getMinuteStr()
    )

    fun setOnTimeSelectedListener(onSelected: OnTimeSelectedListener) {
        this.mOnTimeSelectedListener = onSelected
    }
}