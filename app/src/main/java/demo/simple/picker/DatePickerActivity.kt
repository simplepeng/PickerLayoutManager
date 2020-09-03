package demo.simple.picker

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_date_picker.*

class DatePickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

//        datePickerView.setDateInterval(1949, 2, 24)

        datePickerView.setOnDateSelectedListener { year, month, day ->
            tvDate.text = "$year-$month-$day"
        }

        timePickerView.setOnTimeSelectedListener { hour, minute ->
            tvTime.text = "$hour:$minute"
        }

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
}