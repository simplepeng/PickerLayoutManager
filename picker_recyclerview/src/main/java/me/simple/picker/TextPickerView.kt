package me.simple.picker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

open class TextPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PickerRecyclerView(context, attrs, defStyleAttr) {

    val mItems = mutableListOf<String>()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = TextPickerAdapter(mItems)
    }

    fun setItems(items: MutableList<String>) {
        mItems.clear()
        mItems.addAll(items)
        adapter?.notifyDataSetChanged()
    }

    fun getSelectedItem(): String? {
        if (getSelectedPosition() == RecyclerView.NO_POSITION) return null
        return mItems[getSelectedPosition()]
    }

    inner class TextPickerAdapter(private val items: MutableList<String>) :
        RecyclerView.Adapter<TextPickerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : TextPickerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TextPickerViewHolder(inflater.inflate(R.layout.item_text_picker, parent, false))
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: TextPickerViewHolder, position: Int) {
            holder.bindItem(position)
        }
    }

    inner class TextPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textView = itemView as TextView

        fun bindItem(position: Int) {
            val item = mItems[position]
            textView.text = item
        }
    }
}