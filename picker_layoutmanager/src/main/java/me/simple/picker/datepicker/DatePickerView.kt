package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import me.simple.picker.PickerLinearLayout
import me.simple.picker.PickerUtils
import java.lang.Exception
import java.util.*

typealias OnDateSelectedListener = (year: String, month: String, day: String) -> Unit

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

    private var mOnDateSelectedListener: OnDateSelectedListener? = null

    private var mStartYear: Int = PickerUtils.START_YEAR
    private var mStartMonth: Int = PickerUtils.START_YEAR
    private var mStartDay: Int = PickerUtils.START_YEAR

    private var mEndYear: Int = PickerUtils.getEndYear()
    private var mEndMonth: Int = PickerUtils.getEndMonth()
    private var mEndDay: Int = PickerUtils.getEndDay()

    init {
        orientation = HORIZONTAL
        weightSum = 3f

        addView(mYearPickerView)
        addView(mMonthPickerView)
        addView(mDayPickerView)

        setDivider(mYearPickerView)
        setDivider(mMonthPickerView)
        setDivider(mDayPickerView)

//        mYearPickerView.addOnSelectedItemListener { position ->
//            val year = mYearPickerView.getYear()
//            when (year) {
//                mStartYear -> {
//                    mMonthPickerView.setMonthInterval(mStartMonth)
//                }
//                mEndYear -> {
//                    mMonthPickerView.setMonthInterval(endMonth = mEndMonth)
//                }
//                else -> {
//                    mMonthPickerView.setMonthInterval()
//                }
//            }
//            val month = mMonthPickerView.getMonth()
//            setDayInterval(year, month)
//
//            dispatchOnItemSelected()
//        }

//        mMonthPickerView.addOnSelectedItemListener { position ->
//            val year = mYearPickerView.getYear()
//            val month = mMonthPickerView.getMonth()
//
//            setDayInterval(year, month)
//
//            dispatchOnItemSelected()
//        }
//
//        mDayPickerView.addOnSelectedItemListener { position ->
//            dispatchOnItemSelected()
//        }

        setDateInterval()
        mYearPickerView.scrollToEnd()
    }

    private fun setDayInterval(
        year: Int,
        month: Int
    ) {
        if (year == mStartYear && month == mStartMonth) {
            mDayPickerView.setDayInterval(startDay = mStartDay)
        } else if (year == mEndYear && month == mEndMonth) {
            mDayPickerView.setDayInterval(endDay = mEndDay)
        } else {
            mDayPickerView.setDayIntervalByMonth(year, month)
        }
    }

    private fun dispatchOnItemSelected() {
        try {
            val year = mYearPickerView.getYearStr()
            val month = mMonthPickerView.getMonthStr()
            val day = mDayPickerView.getDayStr()

            Log.d("DatePickerView", "dispatchOnItemSelected: $year-$month-$day")
            mOnDateSelectedListener?.invoke(year, month, day)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置日期时间的区间
     */
    @SuppressWarnings("")
    fun setDateInterval(
        start: Calendar = PickerUtils.getStartCalendar(),
        end: Calendar = PickerUtils.getEndCalendar()
    ) {
        setDateInterval(
            PickerUtils.getYear(start), PickerUtils.getMonth(start), PickerUtils.getDay(start),
            PickerUtils.getYear(end), PickerUtils.getMonth(end), PickerUtils.getDay(end)
        )
    }

    /**
     * 设置日期时间的区间
     */
    @SuppressWarnings("")
    fun setDateInterval(
        startYear: Int = PickerUtils.START_YEAR,
        startMonth: Int = PickerUtils.START_MONTH,
        startDay: Int = PickerUtils.START_DAY,
        endYear: Int = PickerUtils.getEndYear(),
        endMonth: Int = PickerUtils.getEndMonth(),
        endDay: Int = PickerUtils.getEndDay()
    ) {
        this.mStartYear = startYear
        this.mStartMonth = startMonth
        this.mStartDay = startDay
        this.mEndYear = endYear
        this.mEndMonth = endMonth
        this.mEndDay = endDay

        mYearPickerView.setYearInterval(startYear, endYear)
        mMonthPickerView.setMonthInterval(startMonth)
        mDayPickerView.setDayIntervalByMonth(startYear, startMonth, startDay)
    }

    @SuppressWarnings
    fun getCalendar(): Calendar = Calendar.getInstance().apply {
        set(
            mYearPickerView.getYear(), mMonthPickerView.getMonth() - 1, mDayPickerView.getDay(),
            0, 0, 0
        )
    }

    fun getDate(): Date = getCalendar().time

    fun getYearMonthDay() = arrayOf(
        mYearPickerView.getYearStr(),
        mMonthPickerView.getMonth(),
        mDayPickerView.getDayStr()
    )

    fun setOnDateSelectedListener(onSelected: OnDateSelectedListener) {
        this.mOnDateSelectedListener = onSelected
    }
}