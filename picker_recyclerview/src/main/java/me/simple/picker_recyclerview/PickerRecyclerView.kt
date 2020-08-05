package me.simple.picker_recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.RecyclerView

class PickerRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        drawDivider(canvas)

        drawCenterLine(canvas)
    }

    private fun drawDivider(canvas: Canvas) {

    }

    private fun drawCenterLine(canvas: Canvas) {
        if (!PickerLayoutManager.DEBUG) return
        canvas.drawLine(0f, height / 2f - 1f, width.toFloat(), height / 2f + 2f, mPaint)
    }

    private fun logDebug(msg: String) {
        if (!PickerLayoutManager.DEBUG) return
        Log.d("PickerRecyclerView", msg)
    }
}