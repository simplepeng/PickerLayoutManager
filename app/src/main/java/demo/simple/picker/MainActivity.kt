package demo.simple.picker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager

class MainActivity : BaseActivity() {

    private val mItems = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 0..29) {
            mItems.add(i.toString())
        }

        initLinearPicker()
    }

    private fun initLinearPicker() {
        val pickerLayoutManager = PickerLayoutManager(
            orientation = PickerLayoutManager.VERTICAL,
            visibleCount = 5,
            isLoop = false,
            scaleY = 0.75f,
            alpha = 0.5f
        )
//        val pickerLayoutManager = LinearLayoutManager(
//            this@MainActivity,
//            LinearLayoutManager.VERTICAL,
//            false
//        )
        setListener(pickerLayoutManager)
        pickerRecyclerView.run {
            layoutManager = pickerLayoutManager
            adapter = PickerAdapter(pickerLayoutManager.orientation)
        }
        pickerRecyclerView.addItemDecoration(PickerItemDecoration())
    }

    private fun setListener(pickerLayoutManager: PickerLayoutManager) {
        pickerLayoutManager.addOnItemSelectedListener { position ->
            toast(position.toString())
        }
        pickerLayoutManager.addOnItemFillListener(object : PickerLayoutManager.OnItemFillListener {
            override fun onItemSelected(child: View, position: Int) {
                val tvItem = child.findViewById<TextView>(R.id.tv_item)
                tvItem.setTextColor(Color.RED)
            }

            override fun onItemUnSelected(child: View, position: Int) {
                val tvItem = child.findViewById<TextView>(R.id.tv_item)
                tvItem.setTextColor(Color.BLUE)
            }
        })
    }

    inner class PickerAdapter(private val orientation: Int) :
        RecyclerView.Adapter<PickerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : PickerViewHolder {
            val layoutId = if (orientation == PickerLayoutManager.HORIZONTAL) {
                R.layout.item_horizontal_picker
            } else {
                R.layout.item_vertical_picker
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.menu_setting -> {
//                showSettingDialog()
//            }
            R.id.menu_scroll_to -> {
                showToPositionDialog()
            }
            R.id.menu_date_picker -> {
                startActivity(Intent(this, DatePickerActivity::class.java))
            }
            else -> {
            }
        }
        return true
    }

    private fun showSettingDialog() {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_setting)
            .show()

        val rgOrientation = dialog.findViewById<RadioGroup>(R.id.rgOrientation)!!
        val etVisibleCount = dialog.findViewById<EditText>(R.id.etVisibleCount)!!
        val cbIsLoop = dialog.findViewById<CheckBox>(R.id.cbIsLoop)!!
        dialog.findViewById<View>(R.id.btnOk)!!.setOnClickListener {
            dialog.dismiss()

        }
    }

    private fun showToPositionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_to_position)
            .show()
        val etPosition = dialog.findViewById<EditText>(R.id.etToPosition)!!

        dialog.findViewById<View>(R.id.btnToPosition)!!.setOnClickListener {
            dialog.dismiss()
            val position = etPosition.text.toString().toInt()
            pickerRecyclerView.scrollToPosition(position)
        }
        dialog.findViewById<View>(R.id.btnSmoothToPosition)!!.setOnClickListener {
            dialog.dismiss()
            val position = etPosition.text.toString().toInt()
            pickerRecyclerView.smoothScrollToPosition(position)
        }
    }
}
