package me.simple.picker.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px
import me.simple.picker.PickerItemDecoration
import me.simple.picker.PickerLayoutManager
import me.simple.picker.PickerRecyclerView
import me.simple.picker.R
import me.simple.picker.utils.PickerUtils

/**
 * PickerView的包装器
 */
open class TextPickerLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var mVisibleCount = PickerLayoutManager.VISIBLE_COUNT
    var mIsLoop = PickerLayoutManager.IS_LOOP
    var mScaleX = PickerLayoutManager.SCALE_X
    var mScaleY = PickerLayoutManager.SCALE_Y
    var mAlpha = PickerLayoutManager.ALPHA

    var mDividerVisible = PickerRecyclerView.DIVIDER_VISIBLE
    var mDividerSize = PickerRecyclerView.DIVIDER_SIZE
    var mDividerColor = PickerRecyclerView.DIVIDER_COLOR
    var mDividerMargin = PickerRecyclerView.DIVIDER_MARGIN

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
        orientation = HORIZONTAL
        initAttrs(attrs)
    }

    open fun initAttrs(attrs: AttributeSet? = null) {
        val typeA = context.obtainStyledAttributes(
            attrs,
            R.styleable.TextPickerLinearLayout
        )

        mVisibleCount = typeA.getInt(
            R.styleable.TextPickerLinearLayout_visibleCount,
            PickerLayoutManager.VISIBLE_COUNT
        )
        mIsLoop =
            typeA.getBoolean(R.styleable.TextPickerLinearLayout_isLoop, PickerLayoutManager.IS_LOOP)
        mScaleX =
            typeA.getFloat(R.styleable.TextPickerLinearLayout_scaleX, PickerLayoutManager.SCALE_X)
        mScaleY =
            typeA.getFloat(R.styleable.TextPickerLinearLayout_scaleY, PickerLayoutManager.SCALE_Y)
        mAlpha = typeA.getFloat(R.styleable.TextPickerLinearLayout_alpha, PickerLayoutManager.ALPHA)

        mDividerVisible =
            typeA.getBoolean(
                R.styleable.TextPickerLinearLayout_dividerVisible,
                PickerRecyclerView.DIVIDER_VISIBLE
            )
        mDividerSize =
            typeA.getDimension(
                R.styleable.TextPickerLinearLayout_dividerSize,
                PickerRecyclerView.DIVIDER_SIZE
            )
        mDividerColor =
            typeA.getColor(
                R.styleable.TextPickerLinearLayout_dividerColor,
                PickerRecyclerView.DIVIDER_COLOR
            )
        mDividerMargin =
            typeA.getDimension(
                R.styleable.TextPickerLinearLayout_dividerMargin,
                PickerRecyclerView.DIVIDER_MARGIN
            )

        mSelectedTextColor =
            typeA.getColor(
                R.styleable.TextPickerLinearLayout_selectedTextColor,
                PickerUtils.SELECTED_TEXT_COLOR
            )
        mUnSelectedTextColor =
            typeA.getColor(
                R.styleable.TextPickerLinearLayout_unSelectedTextColor,
                PickerUtils.UNSELECTED_TEXT_COLOR
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

    override fun generateDefaultLayoutParams(): LayoutParams {
        val lp = LayoutParams(0, LayoutParams.WRAP_CONTENT)
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

    /**
     * 删除监听
     */
    fun removeOnItemFillListener(listener: PickerLayoutManager.OnItemFillListener) {
        getTextPickerViews().forEach {
            it.layoutManager.removeOnItemFillListener(listener)
        }
    }

    /**
     * 滚动到底部
     */
    open fun scrollToEnd() {
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

            view.setDividerVisible(mDividerVisible)
            view.setDividerColor(mDividerColor)
            view.setDividerSize(mDividerSize)
            view.setDividerMargin(mDividerMargin)

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