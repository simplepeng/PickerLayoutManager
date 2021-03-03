package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.widget.TextPickerLinearLayout
import me.simple.picker.utils.PickerUtils

open class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerLinearLayout(context, attrs, defStyleAttr) {

    private val hourPickerView = HourPickerView(context)

    private val minutePickerView = MinutePickerView(context)

    private var mOnTimeSelectedListener: ((hour: String, minute: String) -> Unit)? = null

    private var mStartHour: Int = PickerUtils.START_HOUR
    private var mStartMinute: Int = PickerUtils.START_MINUTE

    private var mEndHour: Int = PickerUtils.END_HOUR
    private var mEndMinute: Int = PickerUtils.END_MINUTE

    init {
        weightSum = 2f

        addViewInLayout(hourPickerView, 0, generateDefaultLayoutParams(), true)
        addViewInLayout(minutePickerView, 1, generateDefaultLayoutParams(), true)
        requestLayout()

        resetLayoutManager()

        setTimeInterval()
    }

    override fun resetLayoutManager() {
        super.resetLayoutManager()
        setListener()
    }

    private fun setListener() {
        hourPickerView.removeAllOnItemSelectedListener()
        hourPickerView.addOnSelectedItemListener {
            val hour = hourPickerView.getHour()

            when (hour) {
                mStartHour -> {
                    minutePickerView.setMinuteInterval(start = mStartMinute)
                }
                mEndHour -> {
                    minutePickerView.setMinuteInterval(end = mEndMinute)
                }
                else -> {
                    minutePickerView.setMinuteInterval()
                }
            }

            dispatchOnSelectedItem()
        }

        minutePickerView.removeAllOnItemSelectedListener()
        minutePickerView.addOnSelectedItemListener {
            dispatchOnSelectedItem()
        }
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

        hourPickerView.setHourInterval(startHour, endHour)
        minutePickerView.setMinuteInterval(startMinute)
    }

    private fun dispatchOnSelectedItem() {
        this.post {
            val hour = hourPickerView.getHourStr()
            val minute = minutePickerView.getMinuteStr()

            mOnTimeSelectedListener?.invoke(hour, minute)
        }
    }

    /**
     * 获取时间的字符串数组
     */
    fun getTime() = arrayOf(
        hourPickerView.getHourStr(),
        minutePickerView.getMinuteStr()
    )

    /**
     * 设置时间选中的监听
     */
    fun setOnTimeSelectedListener(onSelected: (hour: String, minute: String) -> Unit) {
        this.mOnTimeSelectedListener = onSelected
    }
}