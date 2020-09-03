package demo.simple.picker

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_date_picker.*
import me.simple.picker.PickerLayoutManager

class DatePickerActivity : BaseActivity(), PickerLayoutManager.OnItemFillListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

//        datePickerView.setDateInterval(1949, 2, 24)

        datePickerView.setOnDateSelectedListener { year, month, day ->
            tvDate.text = "$year-$month-$day"
        }
        datePickerView.setOnItemFillListener(this)

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

    override fun onItemSelected(child: View, position: Int) {
        val tv = child as TextView
        tv.setTextColor(Color.RED)
        tv.textSize = 15f
//        tv.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onItemUnSelected(child: View, position: Int) {
        val tv = child as TextView
        tv.setTextColor(Color.BLUE)
        tv.textSize = 11f
//        tv.typeface = Typeface.DEFAULT

    }
}