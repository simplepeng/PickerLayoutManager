package me.simple.picker.widget

import android.content.Context
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

/**
 * 文本类型的PickerView
 */
open class TextPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PickerRecyclerView(context, attrs, defStyleAttr),
    PickerLayoutManager.OnItemFillListener {

    val mItems = mutableListOf<String>()

    var mSelectedTextColor = PickerUtils.SELECTED_TEXT_COLOR
    var mUnSelectedTextColor = PickerUtils.UNSELECTED_TEXT_COLOR

    var mSelectedTextSize = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        PickerUtils.SELECTED_TEXT_SIZE,
        resources.displayMetrics
    )
    var mUnSelectedTextSize = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        PickerUtils.UNSELECTED_TEXT_SIZE,
        resources.displayMetrics
    )

    var mSelectedIsBold = PickerUtils.SELECTED_IS_BOLD

    init {
        initAttrs(attrs)

        mOrientation = PickerLayoutManager.VERTICAL
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = TextPickerAdapter()

        resetLayoutManager()
    }

    override fun initAttrs(attrs: AttributeSet?) {
        super.initAttrs(attrs)

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
        adapter!!.notifyDataSetChanged()
    }

    /**
     * 获取数据源
     */
    fun getData() = mItems

    //重新设置属性值-------------------------------------------

    /**
     * 设置选中时文字的颜色
     */
    fun setSelectedTextColor(@ColorInt textColor: Int) {
        this.mSelectedTextColor = textColor
    }

    /**
     * 设置未选中时文字的颜色
     */
    fun setUnSelectedTextColor(@ColorInt textColor: Int) {
        this.mUnSelectedTextColor = textColor
    }

    /**
     * 设置选中时文字的大小
     */
    fun setSelectedTextSize(@Px textSize: Float) {
        this.mSelectedTextSize = textSize
    }

    /**
     * 设置未选中时文字的大小
     */
    fun setUnSelectedTextSize(@Px textSize: Float) {
        this.mUnSelectedTextSize = textSize
    }

    /**
     * 设置选中时文字是否加粗
     */
    fun setSelectedIsBold(bold: Boolean) {
        this.mSelectedIsBold = bold
    }

    /**
     * 获取选中那个item的文本
     */
    fun getSelectedItem(): String {
        val selectedPosition = getSelectedPosition()
        if (selectedPosition == -1) return ""
        return mItems[getSelectedPosition()]
    }

    /**
     * 选中某个item
     */
    open fun selectedItem(item: String) {
        this.post {
            val position = mItems.indexOf(item)
            scrollToPosition(position)
        }
    }

    /**
     * 选中最后一个item
     */
    open fun selectedEndItem() {
        if (adapter == null) return
        this.post {
            scrollToPosition(adapter!!.itemCount - 1)
        }
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)

        layoutManager.removeAllItemFillListener()
        layoutManager.addOnItemFillListener(this)
    }

    /**
     *
     */
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

    /**
     *
     */
    inner class TextPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textView = itemView as TextView

        fun bindItem(position: Int) {
            val item = mItems[position]
            textView.text = item
        }
    }

    override fun onItemSelected(itemView: View, position: Int) {
        val isItemSelected = itemView.tag as? Boolean
        if (isItemSelected == null || !isItemSelected) {
            val tv = itemView as TextView
            tv.setTextColor(mSelectedTextColor)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectedTextSize)
            if (mSelectedIsBold) {
                tv.typeface = Typeface.DEFAULT_BOLD
            }
            itemView.tag = true
        }
    }

    override fun onItemUnSelected(itemView: View, position: Int) {
        val isItemSelected = itemView.tag as? Boolean
        if (isItemSelected == null || isItemSelected) {
            val tv = itemView as TextView
            tv.setTextColor(mUnSelectedTextColor)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mUnSelectedTextSize)
            if (mSelectedIsBold) {
                tv.typeface = Typeface.DEFAULT
            }
            itemView.tag = false
        }
    }
}