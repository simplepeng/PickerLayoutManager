package me.simple.picker.timepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.OnItemSelectedListener
import me.simple.picker.widget.TextPickerLinearLayout
import me.simple.picker.utils.PickerUtils
import java.util.*

open class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextPickerLinearLayout(context, attrs, defStyleAttr) {

    private val hourPickerView = HourPickerView(context)

    private val minutePickerView = MinutePickerView(context)

    private var mOnTimeSelectedListener1: ((hour: String, minute: String) -> Unit)? = null
    private var mOnTimeSelectedListener2: ((calendar: Calendar) -> Unit)? = null

    private var mStartHour: Int = PickerUtils.START_HOUR
    private var mStartMinute: Int = PickerUtils.START_MINUTE

    private var mEndHour: Int = PickerUtils.END_HOUR
    private var mEndMinute: Int = PickerUtils.END_MINUTE

    private val mHourOnSelectedItemListener: OnItemSelectedListener = {
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

    private val mMinuteOnSelectedItemListener: OnItemSelectedListener = {
        dispatchOnSelectedItem()
    }

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
        hourPickerView.addOnSelectedItemListener(mHourOnSelectedItemListener)

        minutePickerView.addOnSelectedItemListener(mMinuteOnSelectedItemListener)
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

    private val mDispatchOnSelectedItemRun = Runnable {
        val hour = hourPickerView.getHourStr()
        val minute = minutePickerView.getMinuteStr()
        mOnTimeSelectedListener1?.invoke(hour, minute)

        mOnTimeSelectedListener2?.let {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour.toInt())
            calendar.set(Calendar.MINUTE, minute.toInt())
            calendar.set(Calendar.SECOND, 0)
            it.invoke(calendar)
        }
    }

    /**
     *
     */
    private fun dispatchOnSelectedItem() {
        this.post(mDispatchOnSelectedItemRun)
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
        this.mOnTimeSelectedListener1 = onSelected
    }

    /**
     * 设置时间选中的监听
     */
    fun setOnTimeSelectedListener(onSelected: (calendar: Calendar) -> Unit) {
        this.mOnTimeSelectedListener2 = onSelected
    }
}