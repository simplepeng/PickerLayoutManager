package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerLinearLayout
import me.simple.picker.PickerUtils
import java.util.*

//274306954
@SuppressWarnings("")
class DatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PickerLinearLayout(context, attrs, defStyleAttr) {

    private val mYearPickerView = YearPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }
    private val mMonthPickerView = MonthPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }
    private val mDayPickerView = DayPickerView(context).apply {
        layoutParams = generateChildLayoutParams()
    }

    private var mEndYear: Int = PickerUtils.getEndYear()
    private var mEndMonth: Int = PickerUtils.getEndMonth()
    private var mEndDay: Int = PickerUtils.getEndDay()

    private var mIsScrollToEnd = true

    private var mOnSelected: ((year: String, month: String, day: String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        weightSum = 3f

        addView(mYearPickerView)
        addView(mMonthPickerView)
        addView(mDayPickerView)

        initDate()

        setDivider(mYearPickerView)
        setDivider(mMonthPickerView)
        setDivider(mDayPickerView)
    }

    private fun initDate() {
        mYearPickerView.setYearInterval(endYear = mEndYear)
        mMonthPickerView.setMonthInterval(mEndMonth)
        mDayPickerView.setDayInterval(mEndDay)

        checkScrollToEnd()

        mYearPickerView.addOnSelectedItemListener {
            resetDate()
            dispatchOnSelected()
        }
        mMonthPickerView.addOnSelectedItemListener {
            resetDate()
            dispatchOnSelected()
        }
        mDayPickerView.addOnSelectedItemListener {
            dispatchOnSelected()
        }
    }

    fun setDateInterval(start: Calendar, end: Calendar = Calendar.getInstance()) {

    }

    fun setDateInterval(
        startYear: Int, startMonth: Int, startDay: Int,
        endYear: Int, endMonth: Int, endDay: Int
    ) {

    }

    private fun resetDate() {
        val year = mYearPickerView.getYear()?.toInt() ?: return
        val month = mMonthPickerView.getMonth()?.toInt() ?: return

        if (year == mEndYear && month == mEndMonth) {
            mDayPickerView.setDayInterval(mEndDay)
        } else {
            mDayPickerView.setYearAndMonth(year, month)
        }
    }

    private fun checkScrollToEnd() {
        if (!mIsScrollToEnd) return

        scrollToEnd()
    }

    fun scrollToEnd() {
        mYearPickerView.scrollToEnd()
        mMonthPickerView.scrollToEnd()
        mDayPickerView.scrollToEnd()
    }

    private fun dispatchOnSelected() {
        val year = mYearPickerView.getYear()
        val month = mMonthPickerView.getMonth()
        val day = mDayPickerView.getDay()
        if (year.isNullOrEmpty() || month.isNullOrEmpty() || day.isNullOrEmpty())
            return

        mOnSelected?.invoke(year, month, day)
    }

    fun setOnDateSelectedListener(onSelected: (year: String, month: String, day: String) -> Unit) {
        this.mOnSelected = onSelected
    }

    fun getCalendar() = Calendar.getInstance().apply {
//        set(Calendar.YEAR, mYearPickerView.getYear().toInt())
//        set(Calendar.MONTH, mMonthPickerView.getMonth().toInt() - 1)
//        set(Calendar.DATE, mDayPickerView.getDay().toInt())
//        set(Calendar.HOUR_OF_DAY, 0)
//        set(Calendar.MINUTE, 0)
//        set(Calendar.SECOND, 0)
    }

    fun getDate() = getCalendar().time

    fun getYearMonthDay() = arrayOf(
        mYearPickerView.getYear(),
        mMonthPickerView.getMonth(),
        mDayPickerView.getDay()
    )
}