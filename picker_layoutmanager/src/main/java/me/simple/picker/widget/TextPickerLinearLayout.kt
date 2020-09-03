package me.simple.picker.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager
import me.simple.picker.PickerRecyclerView
import me.simple.picker.R

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
    var mDividerSize = 1.0f
    var mDividerColor = Color.LTGRAY
    var mDividerPadding = 1f

    var mScrollToEnd: Boolean = false

    var mSelectedTextColor: Int = Color.BLACK
    var mUnSelectedTextColor: Int = Color.DKGRAY

    var mSelectedTextSize = 14f
    var mUnSelectedTextSize = 14f

    var mSelectedIsBold = false

    init {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet? = null) {
        val typeA = context.obtainStyledAttributes(attrs,
            R.styleable.TextPickerLinearLayout
        )

        mVisibleCount = typeA.getInt(R.styleable.TextPickerLinearLayout_visibleCount, 3)
        mIsLoop = typeA.getBoolean(R.styleable.TextPickerLinearLayout_isLoop, false)
        mScaleX = typeA.getFloat(R.styleable.TextPickerLinearLayout_scaleX, 1.0f)
        mScaleY = typeA.getFloat(R.styleable.TextPickerLinearLayout_scaleY, 1.0f)
        mAlpha = typeA.getFloat(R.styleable.TextPickerLinearLayout_alpha, 1.0f)

        mDividerSize = typeA.getDimension(R.styleable.TextPickerLinearLayout_dividerSize, 1.0f)
        mDividerColor =
            typeA.getColor(R.styleable.TextPickerLinearLayout_dividerColor, Color.LTGRAY)
        mDividerPadding = typeA.getDimension(R.styleable.TextPickerLinearLayout_dividerColor, 1f)

        mScrollToEnd =
            typeA.getBoolean(R.styleable.TextPickerLinearLayout_scrollToEnd, mScrollToEnd)

        mSelectedTextColor =
            typeA.getColor(R.styleable.TextPickerLinearLayout_selectedTextColor, mSelectedTextColor)
        mUnSelectedTextColor =
            typeA.getColor(
                R.styleable.TextPickerLinearLayout_selectedTextColor,
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

        setAttrs()
    }

    fun setAttrs() {
        setSelectedTextColor(mSelectedTextColor)
        setUnSelectedTextColor(mUnSelectedTextColor)

        setSelectedTextSize(mSelectedTextSize)
        setUnSelectedTextSize(mUnSelectedTextSize)

        setSelectedIsBold(mSelectedIsBold)
    }

    fun setDivider(pickerView: PickerRecyclerView) {
        pickerView.addItemDecoration(
            PickerItemDecoration(
                mDividerColor,
                mDividerSize,
                mDividerPadding
            )
        )
    }

    open fun generateChildLayoutParams(): LayoutParams {
        val lp = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT
        )
        lp.weight = 1f
        return lp
    }

    fun setSelectedTextColor(textColor: Int) {
        this.mSelectedTextColor = textColor

        getTextPickerViews().forEach {
            it.setSelectedTextColor(textColor)
        }
    }

    fun setUnSelectedTextColor(textColor: Int) {
        this.mUnSelectedTextColor = textColor

        getTextPickerViews().forEach {
            it.setUnSelectedTextColor(textColor)
        }
    }

    fun setSelectedTextSize(textSize: Float) {
        this.mSelectedTextSize = textSize

        getTextPickerViews().forEach {
            it.setSelectedTextSize(textSize)
        }
    }

    fun setUnSelectedTextSize(textSize: Float) {
        this.mUnSelectedTextSize = textSize

        getTextPickerViews().forEach {
            it.setUnSelectedTextSize(textSize)
        }
    }

    fun setSelectedIsBold(bold: Boolean) {
        this.mSelectedIsBold = bold

        getTextPickerViews().forEach {
            it.setSelectedIsBold(bold)
        }
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

    fun setOnItemFillListener(listener:PickerLayoutManager.OnItemFillListener){
        getTextPickerViews().forEach {
            it.layoutManager.addOnItemFillListener(listener)
        }
    }
}