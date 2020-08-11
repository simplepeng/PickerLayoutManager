package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import java.util.*

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
    private val mDayPickerView = DayPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }

    private var mOnSelected: ((year: String, month: String, day: String) -> Unit)? = null

    private fun generateChildLayoutParams(): LayoutParams {
        val lp = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT
        )
        lp.weight = 1f
        return lp
    }

    init {
        orientation = HORIZONTAL
        weightSum = 3f

        addView(mYearPickerView)
        addView(mMonthPickerView)
        addView(mDayPickerView)

        initDate()
    }

    private fun initDate() {
        mYearPickerView.setYearInterval()
        mMonthPickerView.setMonthInterval()
        mDayPickerView.setDayCountInMonth(31)

        mYearPickerView.addOnSelectedItemListener {
            resetDate()
//            dispatchOnSelected()
        }
        mMonthPickerView.addOnSelectedItemListener {
            resetDate()
//            dispatchOnSelected()
        }
        mDayPickerView.addOnSelectedItemListener {
//            dispatchOnSelected()
        }
    }

    fun setDateInterval(start: Calendar, end: Calendar = Calendar.getInstance()) {

    }

    private fun resetDate() {
        val year = mYearPickerView.getYear().toInt()
        val month = mMonthPickerView.getMonth().toInt()
        mDayPickerView.setYearAndMonth(year, month)
    }

    private fun dispatchOnSelected() {
        val year = mYearPickerView.getYear()
        val month = mMonthPickerView.getMonth()
        val day = mDayPickerView.getDay()
        mOnSelected?.invoke(year, month, day)
    }

    fun setOnDateSelectedListener(onSelected: (year: String, month: String, day: String) -> Unit) {
        this.mOnSelected = onSelected
    }

    fun getCalendar() = Calendar.getInstance().apply {
        set(Calendar.YEAR, mYearPickerView.getYear().toInt())
        set(Calendar.MONTH, mMonthPickerView.getMonth().toInt() - 1)
        set(Calendar.DATE, mDayPickerView.getDay().toInt())
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    fun getDate() = getCalendar().time
}