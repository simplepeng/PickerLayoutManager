package demo.simple.picker

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_date_picker.*
import me.simple.picker.PickerLayoutManager
import me.simple.picker.widget.TextPickerLinearLayout
import java.text.SimpleDateFormat
import java.util.*

class DatePickerActivity : BaseActivity(), PickerLayoutManager.OnItemFillListener {

    val TAG = "DatePickerActivity"
    val dfDate = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

//        initPickerViewStyle(datePickerView)
//        initPickerViewStyle(timePickerView)

        initDatePicker()
        initTimePicker()
        initListener()
    }

    private fun initDatePicker() {
        datePickerView.setOnDateSelectedListener { year, month, day ->
            Log.d(TAG, "date = $year-$month-$day")
            tvDate.text = "$year-$month-$day"
        }
        datePickerView.setOnDateSelectedListener { calendar ->
            val format = dfDate.format(calendar.time)
            Log.d(TAG, "calendar = $format")
        }

//        datePickerView.setDateInterval(
//            1949, 1, 1,
//            2030, 1, 1
//        )
//        datePickerView.selectedEndItem()
        datePickerView.selectedTodayItem()

        btnDatePickerScrollTo.setOnClickListener {
            val year = 2020
            val month = 2
            val day = 15

            val calendar = Calendar.getInstance().apply {
                set(year, month - 1, day)
            }

            datePickerView.selectedTodayItem()
//            datePickerView.setSelectedItem(calendar)
//            datePickerView.setSelectedItem(year, month, day)
        }

        btnSelectedEndItem.setOnClickListener {
            datePickerView.selectedEndItem()
        }
    }

    private fun initTimePicker() {
        timePickerView.setOnTimeSelectedListener { hour, minute ->
            tvTime.text = "$hour:$minute"
            Log.d(TAG, "time = $hour:$minute")
        }
        timePickerView.setOnTimeSelectedListener { calendar ->
            val format = dfDate.format(calendar.time)
            Log.d(TAG, "calendar = $format")
        }
    }

    private fun initListener() {
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
    }

    private fun initPickerViewStyle(pickerView: TextPickerLinearLayout) {
        pickerView.run {
            setVisibleCount(5)
            setIsLoop(true)
            setItemScaleX(0.75f)
            setItemScaleY(0.75f)
            setItemAlpha(0.75f)

            setSelectedTextColor(Color.RED)
            setUnSelectedTextColor(Color.GREEN)
            setSelectedTextSize(16f.dp)
            setUnSelectedTextSize(12f.dp)
            setSelectedIsBold(true)

            setDividerVisible(true)
            setDividerSize(2f.dp)
            setDividerColor(Color.RED)
            setDividerMargin(10f.dp)

            resetLayoutManager()
        }
        pickerView.addOnItemFillListener(this)
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