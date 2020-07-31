package demo.simple.pickerrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_picker.*
import me.simple.picker_recyclerview.PickerLayoutManager

class MainActivity : AppCompatActivity() {

    private val mItems = mutableListOf<String>()
    private val mAdapter = PickerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickerRecyclerView.run {
            layoutManager = PickerLayoutManager()
//            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mAdapter
        }

        for (i in 0..20) {
            mItems.add(i.toString())
        }
        mAdapter.notifyDataSetChanged()
    }

    inner class PickerAdapter : RecyclerView.Adapter<PickerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
            return PickerViewHolder(
                LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.item_picker, parent, false)
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
        val tvItem = itemView.findViewById<TextView>(R.id.tv_item)
    }
}
