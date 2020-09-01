package demo.simple.picker

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_date_picker.*

class DatePickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

        datePickerView.setOnDateSelectedListener { year, month, day ->
            tvDate.text = "$year-$month-$day"
        }

        timePickerView.setOnTimeSelectedListener { hour, minute, second ->
            tvTime.text = "$hour:$minute:$second"
        }

        btnGetDate.setOnClickListener {

            val dateArr = datePickerView.getYearMonthDay()
            val timeArr = timePickerView.getHourMinuteSecond()

            val year = dateArr[0]
            val month = dateArr[1]
            val day = dateArr[2]

            val hour = timeArr[0]
            val minute = timeArr[1]
            val second = timeArr[2]

            val date = "$year-$month-$day"
            val time = "$hour:$minute:$second"

            tvDate.text = date
            tvTime.text = time
        }

    }
}