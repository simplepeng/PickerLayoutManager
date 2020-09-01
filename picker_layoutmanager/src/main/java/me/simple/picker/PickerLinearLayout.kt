package me.simple.picker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout

open class PickerLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var mVisibleCount: Int = 3
    var mIsLoop: Boolean = false
    var mScaleX: Float = 1.0f
    var mScaleY: Float = 1.0f
    var mAlpha: Float = 1.0f
    var mDividerSize: Float = 1.0f
    var mDividerColor: Int = Color.LTGRAY
    var mDividerPadding: Float = 1f

    init {
        initAttrs(attrs)
    }

    open fun initAttrs(attrs: AttributeSet? = null) {
        val typeA = context.obtainStyledAttributes(attrs, R.styleable.PickerLinearLayout)

        mVisibleCount = typeA.getInt(R.styleable.PickerLinearLayout_visibleCount, 3)
        mIsLoop = typeA.getBoolean(R.styleable.PickerLinearLayout_isLoop, false)
        mScaleX = typeA.getFloat(R.styleable.PickerLinearLayout_scaleX, 1.0f)
        mScaleY = typeA.getFloat(R.styleable.PickerLinearLayout_scaleY, 1.0f)
        mAlpha = typeA.getFloat(R.styleable.PickerLinearLayout_alpha, 1.0f)
        mDividerSize = typeA.getDimension(R.styleable.PickerLinearLayout_dividerSize, 1.0f)
        mDividerColor = typeA.getColor(R.styleable.PickerLinearLayout_dividerColor, Color.LTGRAY)
        mDividerPadding = typeA.getDimension(R.styleable.PickerLinearLayout_dividerColor, 1f)

        typeA.recycle()
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

}