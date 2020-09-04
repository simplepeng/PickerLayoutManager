package demo.simple.picker

import android.graphics.Color
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_text_picker.*

class TextPickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_picker)

        val items = mutableListOf<String>()
        for (index in 0 until 100) {
            items.add(index.toString())
        }

        textPickerView.setData(items)

//        textPickerView.run {
//            setVisibleCount(5)
//            setIsLoop(true)
//            setSelectedIsBold(true)
//            setSelectedTextColor(Color.RED)
//
//            resetLayoutManager()
//        }
    }
}