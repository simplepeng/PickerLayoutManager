package me.simple.picker.datepicker

import android.content.Context
import android.util.AttributeSet
import me.simple.picker.PickerLinearLayout
import me.simple.picker.PickerUtils
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

    private var mOnSelected: OnDateSelectedListener? = null

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

        setDateInterval()
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

    fun setOnDateSelectedListener(onSelected: OnDateSelectedListener) {
        this.mOnSelected = onSelected
    }
}