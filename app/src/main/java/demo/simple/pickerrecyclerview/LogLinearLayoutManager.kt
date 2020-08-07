package demo.simple.pickerrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LogLinearLayoutManager : LinearLayoutManager {

    private val TAG = "LogLinearLayoutManager"

    constructor(context: Context?) : super(context)
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        Log.d(TAG, "onLayoutChildren")
    }

    override fun isAutoMeasureEnabled(): Boolean {
//        return false
        return super.isAutoMeasureEnabled()
    }
}