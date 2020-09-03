package me.simple.picker.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import me.simple.picker.PickerLayoutManager

internal class CenterHelperLineItemDecoration : RecyclerView.ItemDecoration() {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        if (parent.layoutManager == null || parent.layoutManager !is PickerLayoutManager) return

        drawCenterLine(canvas, parent)
    }

    private fun drawCenterLine(canvas: Canvas, parent: RecyclerView) {
        val width = parent.width.toFloat()
        val height = parent.height.toFloat()
        val lm = parent.layoutManager as PickerLayoutManager

        if (lm.orientation == PickerLayoutManager.HORIZONTAL) {
            canvas.drawLine(width / 2, 0f, width / 2, height, mPaint)
        } else {
            canvas.drawLine(0f, height / 2f, width, height / 2f, mPaint)
        }
    }
}