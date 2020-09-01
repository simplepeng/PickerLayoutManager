package demo.simple.picker

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager
import java.lang.Exception

class MainActivity : BaseActivity() {

    private val mItems = mutableListOf<String>()
    private val mRecyclerViews = hashSetOf<RecyclerView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (i in 0..29) {
            mItems.add(i.toString())
        }

        mRecyclerViews.add(pickerRecyclerView)
        mRecyclerViews.add(pickerRecyclerView2)

        initLinearPicker()

        initHorizontalPicker()

    }

    private fun initLinearPicker() {
        val pickerLayoutManager = PickerLayoutManager(
            orientation = PickerLayoutManager.VERTICAL,
            visibleCount = 3,
            isLoop = false
        )
        setListener(pickerLayoutManager)
        pickerRecyclerView.run {
            layoutManager = pickerLayoutManager
            //            layoutManager = LogLinearLayoutManager(this@MainActivity)
            adapter = PickerAdapter(PickerLayoutManager.VERTICAL)
        }
        pickerRecyclerView.addItemDecoration(PickerItemDecoration())
    }

    private fun initHorizontalPicker() {
        val pickerLayoutManager = PickerLayoutManager(
            orientation = PickerLayoutManager.HORIZONTAL,
            visibleCount = 3,
            isLoop = false
        )
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
//        val isLoop = checkBoxIsLoop.isChecked
//        val visibleCount = etVisibleCount.text.toString().toInt()
//        val lm = PickerLayoutManager(PickerLayoutManager.VERTICAL, visibleCount, isLoop)
//        setListener(lm)
//        pickerRecyclerView.layoutManager = lm
    }

    fun createLayoutManager() = PickerLayoutManager.Builder()
        .build()

//    fun scrollTo(view: View) {
//        try {
//            val position = etScrollTo.text.toString().toInt()
//            pickerRecyclerView.scrollToPosition(position)
//        } catch (e: Exception) {
//            toast(e.message)
//        }
//    }

//    fun smoothScrollTo(view: View) {
//        try {
//            val position = etSmoothScrollTo.text.toString().toInt()
//            pickerRecyclerView.smoothScrollToPosition(position)
//        } catch (e: Exception) {
//            toast(e.message)
//        }
//    }

//    fun smoothScrollTo2(view: View) {
//        try {
//            val position = etSmoothScrollTo2.text.toString().toInt()
//            pickerRecyclerView.smoothScrollToPosition(position)
//        } catch (e: Exception) {
//            toast(e.message)
//        }
//    }


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> {

            }
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

    }

    private fun showToPositionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_to_position)
            .show()
        val etPosition = dialog.findViewById<EditText>(R.id.etToPosition)!!

        dialog.findViewById<View>(R.id.btnToPosition)!!.setOnClickListener {
            dialog.dismiss()
            val position = etPosition.text.toString().toInt()
            for (view in mRecyclerViews) {
                view.scrollToPosition(position)
            }
        }
        dialog.findViewById<View>(R.id.btnSmoothToPosition)!!.setOnClickListener {
            dialog.dismiss()
            val position = etPosition.text.toString().toInt()
            for (view in mRecyclerViews) {
                view.smoothScrollToPosition(position)
            }
        }
    }
}
