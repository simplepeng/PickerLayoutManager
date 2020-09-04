package me.simple.picker.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import me.simple.picker.PickerLayoutManager
import me.simple.picker.PickerRecyclerView
import me.simple.picker.R
import me.simple.picker.utils.PickerUtils
import me.simple.picker.utils.dp

open class TextPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PickerRecyclerView(context, attrs, defStyleAttr),
    PickerLayoutManager.OnItemFillListener {

    val mItems = mutableListOf<String>()

    var mSelectedTextColor = PickerUtils.SELECTED_TEXT_COLOR
    var mUnSelectedTextColor = PickerUtils.UNSELECTED_TEXT_COLOR
    var mSelectedTextSize = PickerUtils.SELECTED_TEXT_SIZE.dp
    var mUnSelectedTextSize = PickerUtils.UNSELECTED_TEXT_SIZE.dp
    var mSelectedIsBold = PickerUtils.SELECTED_IS_BOLD

    init {
        initAttrs(attrs)

        mOrientation = PickerLayoutManager.VERTICAL
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = TextPickerAdapter()

        resetLayoutManager()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val typeA = context.obtainStyledAttributes(
            attrs,
            R.styleable.TextPickerView
        )
        mSelectedTextColor =
            typeA.getColor(R.styleable.TextPickerView_selectedTextColor, mSelectedTextColor)
        mUnSelectedTextColor =
            typeA.getColor(R.styleable.TextPickerView_unSelectedTextColor, mUnSelectedTextColor)
        mSelectedTextSize =
            typeA.getDimension(R.styleable.TextPickerView_selectedTextSize, mSelectedTextSize)
        mUnSelectedTextSize =
            typeA.getDimension(R.styleable.TextPickerView_unSelectedTextSize, mUnSelectedTextSize)
        mSelectedIsBold =
            typeA.getBoolean(R.styleable.TextPickerView_selectedIsBold, mSelectedIsBold)
        typeA.recycle()
    }

    /**
     * 设置数据源
     */
    fun setData(data: List<String>) {
        mItems.clear()
        mItems.addAll(data)
        adapter?.notifyDataSetChanged()
    }

    //重新设置属性值-------------------------------------------

    fun setSelectedTextColor(@ColorInt textColor: Int) {
        this.mSelectedTextColor = textColor
    }

    fun setUnSelectedTextColor(@ColorInt textColor: Int) {
        this.mUnSelectedTextColor = textColor
    }

    fun setSelectedTextSize(@Px textSize: Float) {
        this.mSelectedTextSize = textSize
    }

    fun setUnSelectedTextSize(@Px textSize: Float) {
        this.mUnSelectedTextSize = textSize
    }

    fun setSelectedIsBold(bold: Boolean) {
        this.mSelectedIsBold = bold
    }

     override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)

        layoutManager.addOnItemFillListener(this)
    }

    fun getSelectedItem(): String {
        return mItems[getSelectedPosition()]
    }

    inner class TextPickerAdapter :
        RecyclerView.Adapter<TextPickerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : TextPickerViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return TextPickerViewHolder(inflater.inflate(R.layout.item_text_picker, parent, false))
        }

        override fun getItemCount(): Int {
            return mItems.size
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

    override fun onItemSelected(child: View, position: Int) {
        val tv = child as TextView
        tv.setTextColor(mSelectedTextColor)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectedTextSize)
        if (mSelectedIsBold) {
            tv.typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun onItemUnSelected(child: View, position: Int) {
        val tv = child as TextView
        tv.setTextColor(mUnSelectedTextColor)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mUnSelectedTextSize)
        if (mSelectedIsBold) {
            tv.typeface = Typeface.DEFAULT
        }
    }
}