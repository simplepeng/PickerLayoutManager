package me.simple.picker

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

class CenterHelperLineItemDecoration : RecyclerView.ItemDecoration() {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        drawCenterLine(canvas, parent)
    }

    private fun drawCenterLine(canvas: Canvas, parent: RecyclerView) {
        val width = parent.width
        val height = parent.height
        canvas.drawLine(
            0f, height / 2f - 1f,
            width.toFloat(), height / 2f + 2f, mPaint
        )
    }
}