package me.simple.picker.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager
import me.simple.picker.PickerRecyclerView
import me.simple.picker.R
import me.simple.picker.utils.PickerUtils
import me.simple.picker.utils.dp

open class TextPickerLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

     var mVisibleCount = 3
    var mIsLoop = false
    var mScaleX = 1.0f
    var mScaleY = 1.0f
    var mAlpha = 1.0f

    var mDividerVisible = true
    var mDividerSize = 1.0f
    var mDividerColor = Color.LTGRAY
    var mDividerMargin = 0f

    var mScrollToEnd: Boolean = false

    var mSelectedTextColor = PickerUtils.SELECTED_TEXT_COLOR
    var mUnSelectedTextColor = PickerUtils.UNSELECTED_TEXT_COLOR
    var mSelectedTextSize = PickerUtils.SELECTED_TEXT_SIZE.dp
    var mUnSelectedTextSize = PickerUtils.UNSELECTED_TEXT_SIZE.dp
    var mSelectedIsBold = PickerUtils.SELECTED_IS_BOLD

    init {
        orientation = HORIZONTAL
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet? = null) {
        val typeA = context.obtainStyledAttributes(
            attrs,
            R.styleable.TextPickerLinearLayout
        )

        mVisibleCount = typeA.getInt(R.styleable.TextPickerLinearLayout_visibleCount, mVisibleCount)
        mIsLoop = typeA.getBoolean(R.styleable.TextPickerLinearLayout_isLoop, mIsLoop)
        mScaleX = typeA.getFloat(R.styleable.TextPickerLinearLayout_scaleX, mScaleX)
        mScaleY = typeA.getFloat(R.styleable.TextPickerLinearLayout_scaleY, mScaleY)
        mAlpha = typeA.getFloat(R.styleable.TextPickerLinearLayout_alpha, mAlpha)

        mDividerVisible =
            typeA.getBoolean(R.styleable.TextPickerLinearLayout_dividerVisible, mDividerVisible)
        mDividerSize =
            typeA.getDimension(R.styleable.TextPickerLinearLayout_dividerSize, mDividerSize)
        mDividerColor =
            typeA.getColor(R.styleable.TextPickerLinearLayout_dividerColor, mDividerColor)
        mDividerMargin =
            typeA.getDimension(R.styleable.TextPickerLinearLayout_dividerMargin, mDividerMargin)

        mScrollToEnd =
            typeA.getBoolean(R.styleable.TextPickerLinearLayout_scrollToEnd, mScrollToEnd)

        mSelectedTextColor =
            typeA.getColor(R.styleable.TextPickerLinearLayout_selectedTextColor, mSelectedTextColor)
        mUnSelectedTextColor =
            typeA.getColor(
                R.styleable.TextPickerLinearLayout_unSelectedTextColor,
                mUnSelectedTextColor
            )
        mSelectedTextSize =
            typeA.getDimension(
                R.styleable.TextPickerLinearLayout_selectedTextSize,
                mSelectedTextSize
            )
        mUnSelectedTextSize =
            typeA.getDimension(
                R.styleable.TextPickerLinearLayout_unSelectedTextSize,
                mUnSelectedTextSize
            )
        mSelectedIsBold =
            typeA.getBoolean(R.styleable.TextPickerLinearLayout_selectedIsBold, mSelectedIsBold)

        typeA.recycle()
    }

    private fun setDivider(pickerView: PickerRecyclerView) {
        pickerView.addItemDecoration(
            PickerItemDecoration(
                mDividerColor,
                mDividerSize,
                mDividerMargin
            )
        )
    }

    private fun removeDivider(pickerView: PickerRecyclerView) {
        val count = pickerView.itemDecorationCount
        for(index in 0 until count){
            pickerView.removeItemDecorationAt(index)
        }
    }

    protected fun generateChildLayoutParams(): LayoutParams {
        val lp = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT
        )
        lp.weight = 1f
        return lp
    }

    private fun getTextPickerViews(): HashSet<TextPickerView> {
        val views = hashSetOf<TextPickerView>()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child is TextPickerView) {
                views.add(child)
            }
        }
        return views
    }

    /**
     * 设置监听
     */
    fun addOnItemFillListener(listener: PickerLayoutManager.OnItemFillListener) {
        getTextPickerViews().forEach {
            it.layoutManager.addOnItemFillListener(listener)
        }
    }

    fun removeOnItemFillListener(listener: PickerLayoutManager.OnItemFillListener) {
        getTextPickerViews().forEach {
            it.layoutManager.removeOnItemFillListener(listener)
        }
    }

    /**
     * 滚动到底部
     */
    fun scrollToEnd() {
        for (view in getTextPickerViews()) {
            view.scrollToEnd()
        }
    }

    //重新设置属性值-------------------------------------------

    fun setVisibleCount(count: Int) {
        this.mVisibleCount = count
    }

    fun setIsLoop(isLoop: Boolean) {
        this.mIsLoop = isLoop
    }

    fun setItemScaleX(scaleX: Float) {
        this.mScaleX = scaleX
    }

    fun setItemScaleY(scaleY: Float) {
        this.mScaleY = scaleY
    }

    fun setItemAlpha(alpha: Float) {
        this.mAlpha = alpha
    }


    fun setSelectedTextColor(textColor: Int) {
        this.mSelectedTextColor = textColor
    }

    fun setUnSelectedTextColor(textColor: Int) {
        this.mUnSelectedTextColor = textColor
    }

    fun setSelectedTextSize(textSize: Float) {
        this.mSelectedTextSize = textSize
    }

    fun setUnSelectedTextSize(textSize: Float) {
        this.mUnSelectedTextSize = textSize
    }

    fun setSelectedIsBold(bold: Boolean) {
        this.mSelectedIsBold = bold
    }


    fun setDividerVisible(visible: Boolean) {
        this.mDividerVisible = visible
    }

    fun setDividerSize(@Px size: Float) {
        this.mDividerSize = size
    }

    fun setDividerColor(@ColorInt color: Int) {
        this.mDividerColor = color
    }

    fun setDividerMargin(margin: Float) {
        this.mDividerMargin = margin
    }

    /**
     * 在设置完属性后，必须调用这个方法
     */
    open fun resetLayoutManager() {
        for (view in getTextPickerViews()) {

            view.setSelectedTextColor(mSelectedTextColor)
            view.setUnSelectedTextColor(mUnSelectedTextColor)
            view.setSelectedTextSize(mSelectedTextSize)
            view.setUnSelectedTextSize(mUnSelectedTextSize)
            view.setSelectedIsBold(mSelectedIsBold)

            removeDivider(view)
            if (mDividerVisible) {
                setDivider(view)
            }

            val lm = PickerLayoutManager(
                PickerLayoutManager.VERTICAL,
                mVisibleCount,
                mIsLoop,
                mScaleX,
                mScaleY,
                mAlpha
            )
            view.resetLayoutManager(lm)
        }
    }
}