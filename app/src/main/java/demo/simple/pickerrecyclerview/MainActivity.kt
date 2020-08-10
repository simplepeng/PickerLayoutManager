package demo.simple.pickerrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val mItems = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 0..100) {
            mItems.add(i.toString())
        }

        initLinearPicker()

        initHorizontalPicker()

    }

    private fun initLinearPicker() {
        val pickerLayoutManager = PickerLayoutManager(PickerLayoutManager.VERTICAL, scaleY = 0.8f)
        setListener(pickerLayoutManager)
        pickerRecyclerView.run {
            layoutManager = pickerLayoutManager
            //            layoutManager = LogLinearLayoutManager(this@MainActivity)
            adapter = PickerAdapter(PickerLayoutManager.VERTICAL)
        }
        pickerRecyclerView.addItemDecoration(PickerItemDecoration())
    }

    private fun initHorizontalPicker() {
        val pickerLayoutManager = PickerLayoutManager(PickerLayoutManager.HORIZONTAL)
        setListener(pickerLayoutManager)
        pickerRecyclerView2.run {
            layoutManager = pickerLayoutManager
//            layoutManager =
//                LogLinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = PickerAdapter(PickerLayoutManager.HORIZONTAL)
        }
        pickerRecyclerView2.addItemDecoration(PickerItemDecoration())
    }

    private fun setListener(pickerLayoutManager: PickerLayoutManager) {
        pickerLayoutManager.addOnSelectedItemListener { position ->
            toast(position.toString())
        }
    }

    fun reLayout(view: View) {
        val isLoop = checkBoxIsLoop.isChecked
        val visibleCount = etVisibleCount.text.toString().toInt()
        val lm = PickerLayoutManager(PickerLayoutManager.VERTICAL, visibleCount, isLoop)
        setListener(lm)
        pickerRecyclerView.layoutManager = lm
    }

    fun scrollTo(view: View) {
        try {
            val position = etScrollTo.text.toString().toInt()
            pickerRecyclerView.scrollToPosition(position)
        } catch (e: Exception) {
            toast(e.message)
        }
    }

    fun smoothScrollTo(view: View) {
        try {
            val position = etSmoothScrollTo.text.toString().toInt()
            pickerRecyclerView.smoothScrollToPosition(position)
        } catch (e: Exception) {
            toast(e.message)
        }
    }

    fun smoothScrollTo2(view: View) {
        try {
            val position = etSmoothScrollTo2.text.toString().toInt()
            pickerRecyclerView.smoothScrollToPosition(position)
        } catch (e: Exception) {
            toast(e.message)
        }
    }

    private fun toast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    inner class PickerAdapter(private val orientation: Int) :
        RecyclerView.Adapter<PickerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
            val layoutId = if (orientation == PickerLayoutManager.HORIZONTAL) {
                R.layout.item_horizontal_picker
            } else {
                R.layout.item_picker
            }
            return PickerViewHolder(
                LayoutInflater.from(this@MainActivity)
                    .inflate(layoutId, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return mItems.size
        }

        override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
            holder.tvItem.text = mItems[position]
        }

    }

    inner class PickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItem = itemView.findViewById<TextView>(R.id.tv_item)!!
    }
}
