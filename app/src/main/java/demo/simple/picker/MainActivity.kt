package demo.simple.picker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import demo.simple.picker.databinding.ActivityMainBinding
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager

@SuppressLint("NotifyDataSetChanged")
class MainActivity : BaseActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(this.layoutInflater) }

    private val mItems = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        startActivity(Intent(this, DatePickerActivity::class.java))
//        startActivity(Intent(this, TextPickerActivity::class.java))

        for (i in 0..29) {
            mItems.add(i.toString())
        }

        initLinearPicker()

        binding.btnNotify.setOnClickListener {
            for (i in 0..10) {
                mItems.removeAt(mItems.size - 1)
            }
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun initLinearPicker() {
        val pickerLayoutManager =
            PickerLayoutManager(PickerLayoutManager.VERTICAL, visibleCount = 1)
//        val pickerLayoutManager = LinearLayoutManager(
//            this@MainActivity,
//            LinearLayoutManager.VERTICAL,
//            false
//        )
        setListener(pickerLayoutManager)
        binding.recyclerView.run {
            layoutManager = pickerLayoutManager
            adapter = PickerAdapter(pickerLayoutManager.orientation)
        }
        binding.recyclerView.addItemDecoration(PickerItemDecoration())
    }

    private fun setListener(pickerLayoutManager: PickerLayoutManager) {
        pickerLayoutManager.addOnItemSelectedListener { position ->
            toast(position.toString())
        }
//        pickerLayoutManager.addOnItemFillListener(object : PickerLayoutManager.OnItemFillListener {
//            override fun onItemSelected(itemView: View, position: Int) {
//                val tvItem = itemView.findViewById<TextView>(R.id.tv_item)
//                tvItem.setTextColor(Color.RED)
//            }
//
//            override fun onItemUnSelected(itemView: View, position: Int) {
//                val tvItem = itemView.findViewById<TextView>(R.id.tv_item)
//                tvItem.setTextColor(Color.BLUE)
//            }
//        })
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
            R.id.menu_text_picker -> {
                startActivity(Intent(this, TextPickerActivity::class.java))
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
            binding.recyclerView.scrollToPosition(position)
        }
        dialog.findViewById<View>(R.id.btnSmoothToPosition)!!.setOnClickListener {
            dialog.dismiss()
            val position = etPosition.text.toString().toInt()
            binding.recyclerView.smoothScrollToPosition(position)
        }
    }
}
