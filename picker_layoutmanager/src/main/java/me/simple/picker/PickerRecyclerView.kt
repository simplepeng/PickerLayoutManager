package me.simple.picker

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import me.simple.picker.OnItemSelectedListener
import me.simple.picker.PickerLayoutManager
import me.simple.picker.R


open class PickerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var mOrientation = PickerLayoutManager.VERTICAL
    var mVisibleCount = 3
    var mIsLoop = false
    var mScaleX = 1.0f
    var mScaleY = 1.0f
    var mAlpha = 1.0f

    init {
        val typeA = context.obtainStyledAttributes(
            attrs,
            R.styleable.PickerRecyclerView
        )

        mOrientation = typeA.getInt(R.styleable.PickerRecyclerView_orientation, mOrientation)
        mVisibleCount = typeA.getInt(R.styleable.PickerRecyclerView_visibleCount, mVisibleCount)
        mIsLoop = typeA.getBoolean(R.styleable.PickerRecyclerView_isLoop, mIsLoop)
        mScaleX = typeA.getFloat(R.styleable.PickerRecyclerView_scaleX, mScaleX)
        mScaleY = typeA.getFloat(R.styleable.PickerRecyclerView_scaleY, mScaleY)
        mAlpha = typeA.getFloat(R.styleable.PickerRecyclerView_alpha, mAlpha)

        typeA.recycle()

        resetLayoutManager(mOrientation, mVisibleCount, mIsLoop, mScaleX, mScaleY, mAlpha)
    }

    fun resetLayoutManager(
        orientation: Int,
        visibleCount: Int,
        isLoop: Boolean,
        scaleX: Float,
        scaleY: Float,
        alpha: Float
    ) {
        layoutManager = PickerLayoutManager(
            orientation,
            visibleCount,
            isLoop,
            scaleX,
            scaleY,
            alpha
        )
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        if (layout !is PickerLayoutManager) {
            throw IllegalArgumentException("LayoutManager only can use PickerLayoutManager")
        }
    }

    fun getSelectedPosition() = layoutManager.getSelectedPosition()

    override fun getLayoutManager(): PickerLayoutManager {
        return super.getLayoutManager() as PickerLayoutManager
    }

    fun addOnSelectedItemListener(listener: OnItemSelectedListener) {
        layoutManager.addOnItemSelectedListener(listener)
    }

    fun removeOnItemSelectedListener(listener: OnItemSelectedListener) {
        layoutManager.removeOnItemSelectedListener(listener)
    }

    fun scrollToEnd() {
        if (adapter == null) return
        this.post {
            this.scrollToPosition(adapter!!.itemCount - 1)
//        this.smoothScrollToPosition(adapter!!.itemCount - 1)
        }
    }
}