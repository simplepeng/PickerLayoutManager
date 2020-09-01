package me.simple.picker

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


open class PickerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        val typeA = context.obtainStyledAttributes(attrs, R.styleable.PickerRecyclerView)
        val orientation =
            typeA.getInt(R.styleable.PickerRecyclerView_orientation, PickerLayoutManager.VERTICAL)
        val visibleCount = typeA.getInt(R.styleable.PickerRecyclerView_visibleCount, 3)
        val isLoop = typeA.getBoolean(R.styleable.PickerRecyclerView_isLoop, false)
        val scaleX = typeA.getFloat(R.styleable.PickerRecyclerView_scaleX, 1.0f)
        val scaleY = typeA.getFloat(R.styleable.PickerRecyclerView_scaleY, 1.0f)
        val alpha = typeA.getFloat(R.styleable.PickerRecyclerView_alpha, 1.0f)

        val lm =
            PickerLayoutManager(orientation, visibleCount, isLoop, scaleX, scaleY, alpha)
        typeA.recycle()

        layoutManager = lm
    }

    fun resetLayoutManager() {
        layoutManager = PickerLayoutManager.Builder()
            .build()
    }

    fun getSelectedPosition() = layoutManager.getSelectedPosition()

    override fun getLayoutManager(): PickerLayoutManager {
        return super.getLayoutManager() as PickerLayoutManager
    }

    fun addOnSelectedItemListener(listener: (position: Int) -> Unit) {
        layoutManager.addOnItemSelectedListener(listener)
    }

    fun scrollToEnd() {
        if (adapter == null) return
        this.scrollToPosition(adapter!!.itemCount - 1)
//        this.smoothScrollToPosition(adapter!!.itemCount - 1)
    }
}