package me.simple.picker_recyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class PickerRecyclerView : RecyclerView {

    private val mLinearSnapHelper = LinearSnapHelper()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        mLinearSnapHelper.attachToRecyclerView(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        mLinearSnapHelper.attachToRecyclerView(null)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        logDebug("onScrollStateChanged -- $state")
        if (state == SCROLL_STATE_IDLE) {
            val itemView = mLinearSnapHelper.findSnapView(layoutManager) ?: return
            val position = getChildAdapterPosition(itemView)
            logDebug("selected position == $position")
        }
    }

    private fun logDebug(msg: String) {
        if (!BuildConfig.DEBUG) return
        Log.d("PickerRecyclerView", msg)
    }
}