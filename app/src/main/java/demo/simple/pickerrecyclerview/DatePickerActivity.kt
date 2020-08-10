package demo.simple.pickerrecyclerview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_date_picker.*

class DatePickerActivity : AppCompatActivity() {

    private val mItems = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_picker)

        for (i in 0 .. 100){
            mItems.add(i.toString())
        }

        textPickerView.setItems(mItems)
    }
}