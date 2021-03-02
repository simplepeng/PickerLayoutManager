package demo.simple.picker

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_date_picker.*
import me.simple.picker.PickerLayoutManager
import java.util.*

class DatePickerActivity : BaseActivity(), PickerLayoutManager.OnItemFillListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

//        datePickerView.setDateInterval(
//            1949, 2, 24,
//            2021, 5, 1
//        )
        datePickerView.setDateInterval(
            1949, 2, 22,
            2030, 4, 3
        )
        datePickerView.selectedEndItem()
//        datePickerView.scrollToCurrentDate()

        datePickerView.setOnDateSelectedListener { year, month, day ->
            tvDate.text = "$year-$month-$day"
        }
        datePickerView.run {
//            setVisibleCount(5)
//            setIsLoop(true)
//            setItemScaleX(0.75f)
//            setItemScaleY(0.75f)
//            setItemAlpha(0.75f)
//
//            setSelectedTextColor(Color.RED)
//            setUnSelectedTextColor(Color.GREEN)
//            setSelectedTextSize(16f.dp)
//            setUnSelectedTextSize(12f.dp)
//            setSelectedIsBold(true)
//
//
//            setDividerVisible(true)
//            setDividerSize(2f.dp)
//            setDividerColor(Color.RED)
//            setDividerMargin(10f.dp)
//
//            resetLayoutManager()
        }
//        datePickerView.setOnItemFillListener(this)

        timePickerView.setOnTimeSelectedListener { hour, minute ->
            tvTime.text = "$hour:$minute"
        }
//        timePickerView.run {
//            setVisibleCount(5)
//            setIsLoop(true)
//            setItemScaleX(0.75f)
//            setItemScaleY(0.75f)
//            setItemAlpha(0.75f)
//
//            setSelectedTextColor(Color.RED)
//            setUnSelectedTextColor(Color.GREEN)
//            setSelectedTextSize(16f.dp)
//            setUnSelectedTextSize(12f.dp)
//            setSelectedIsBold(true)
//
//
//            setDividerVisible(true)
//            setDividerSize(2f.dp)
//            setDividerColor(Color.RED)
//            setDividerMargin(10f.dp)
//
//            resetLayoutManager()
//        }
//        timePickerView.setOnItemFillListener(this)

        btnGetDate.setOnClickListener {

            val dateArr = datePickerView.getYearMonthDay()
            val timeArr = timePickerView.getTime()

            val year = dateArr[0]
            val month = dateArr[1]
            val day = dateArr[2]

            val hour = timeArr[0]
            val minute = timeArr[1]

            val date = "$year-$month-$day"
            val time = "$hour:$minute"

            tvDate.text = date
            tvTime.text = time

            toast("$date   $time")
        }

        btnDatePickerScrollTo.setOnClickListener {
            val year = 2020
            val month = 2
            val day = 15

            val calendar = Calendar.getInstance().apply {
                set(year, month - 1, day)
            }

            val smoothScroll = false
//            datePickerView.scrollTo(calendar, smoothScroll)
//            datePickerView.scrollTo(year, month, day)
            datePickerView.selectedCurrentDateItem()
        }
    }

    override fun onItemSelected(child: View, position: Int) {
        val tv = child as TextView
        tv.setTextColor(Color.RED)
        tv.textSize = 15f
        tv.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onItemUnSelected(child: View, position: Int) {
        val tv = child as TextView
        tv.setTextColor(Color.BLUE)
        tv.textSize = 11f
        tv.typeface = Typeface.DEFAULT
    }
}