package me.simple.picker

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.recyclerview.widget.RecyclerView

class PickerItemDecoration(
    private val color: Int = Color.LTGRAY,
    private val size: Float = 1.0f,
    private val margin: Float = 0f
) : RecyclerView.ItemDecoration() {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@PickerItemDecoration.color
        style = Paint.Style.FILL
    }
    private val mRectF = RectF()

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (parent.layoutManager == null || parent.layoutManager !is PickerLayoutManager)
            return

        calcDivider(c, parent)
    }

    private fun calcDivider(canvas: Canvas, parent: RecyclerView) {
        val lm = parent.layoutManager as PickerLayoutManager
        val itemSize = if (lm.orientation == PickerLayoutManager.HORIZONTAL) {
            parent.width / lm.visibleCount
        } else {
            parent.height / lm.visibleCount
        }

        val startDrawPosition = (lm.visibleCount - 1) / 2
        val endDrawPosition = startDrawPosition + 1

        drawDivider(canvas, itemSize, startDrawPosition, parent, lm)
        drawDivider(canvas, itemSize, endDrawPosition, parent, lm)
    }

    private fun drawDivider(
        canvas: Canvas,
        itemSize: Int,
        position: Int,
        parent: RecyclerView,
        lm: PickerLayoutManager
    ) {
        if (lm.orientation == PickerLayoutManager.HORIZONTAL) {
            val left = position * itemSize.toFloat() - size / 2
            val right = left + size
            mRectF.set(left, margin, right, parent.height - margin)
            canvas.drawRect(mRectF, mPaint)
        } else {
            val top = position * itemSize.toFloat() - size / 2
            val bottom = top + size
            mRectF.set(margin, top, parent.width - margin, bottom)
            canvas.drawRect(mRectF, mPaint)
        }
    }
}