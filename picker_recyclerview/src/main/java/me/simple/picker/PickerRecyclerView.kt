package me.simple.picker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.RecyclerView


open class PickerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var mLayoutManager: PickerLayoutManager

    init {
        val typeA = context.obtainStyledAttributes(attrs, R.styleable.PickerRecyclerView)
        val orientation =
            typeA.getInt(R.styleable.PickerRecyclerView_orientation, PickerLayoutManager.VERTICAL)
        val visibleCount = typeA.getInt(R.styleable.PickerRecyclerView_visibleCount, 3)
        val isLoop = typeA.getBoolean(R.styleable.PickerRecyclerView_isLoop, false)
        val scaleX = typeA.getFloat(R.styleable.PickerRecyclerView_scaleX, 1.0f)
        val scaleY = typeA.getFloat(R.styleable.PickerRecyclerView_scaleY, 1.0f)
        val alpha = typeA.getFloat(R.styleable.PickerRecyclerView_alpha, 1.0f)

        mLayoutManager =
            PickerLayoutManager(orientation, visibleCount, isLoop, scaleX, scaleY, alpha)
        typeA.recycle()

        layoutManager = mLayoutManager
    }
}