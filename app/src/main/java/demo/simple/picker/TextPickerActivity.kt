package demo.simple.picker

import android.os.Bundle
import demo.simple.picker.databinding.ActivityTextPickerBinding

class TextPickerActivity : BaseActivity() {

    private val binding by lazy { ActivityTextPickerBinding.inflate(this.layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val items = mutableListOf<String>()
        for (index in 0 until 100) {
            items.add(index.toString())
        }

        binding.textPickerView.setData(items)

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