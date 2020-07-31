package me.simple.picker_recyclerview

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class PickerLayoutManager(
    private val orientation: Int = VERTICAL,
    private val visibleCount: Int = 3,
    private val isLoop: Boolean = false
) : RecyclerView.LayoutManager() {

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL
        const val TAG = "PickerLayoutManager"
    }

    private fun debug(msg: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, msg)
    }

    private fun logChildCount(recycler: RecyclerView.Recycler) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "childCount == $childCount -- scrapSize == ${recycler.scrapList.size}")
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (orientation == VERTICAL) {
            RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
        } else {
            RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun getInnerItemCount() = if (isLoop) Int.MAX_VALUE else super.getItemCount()

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        if (state.itemCount == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec)
            return
        }
        if (state.isPreLayout) return

        detachAndScrapAttachedViews(recycler)

        val itemView = recycler.getViewForPosition(0)
        addView(itemView)
        measureChildWithMargins(itemView, 0, 0)

        val itemWidth = getDecoratedMeasuredWidth(itemView)
        val itemHeight = getDecoratedMeasuredHeight(itemView)
        debug("itemWidth = $itemWidth -- itemHeight = $itemHeight")
        detachAndScrapView(itemView, recycler)

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(itemWidth * visibleCount, itemHeight)
        } else {
            setMeasuredDimension(itemWidth, itemHeight * visibleCount)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) return
        detachAndScrapAttachedViews(recycler)
        fill(recycler)
    }

    override fun canScrollHorizontally(): Boolean {
        return orientation == HORIZONTAL
    }

    override fun canScrollVertically(): Boolean {
        return orientation == VERTICAL
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return dx
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (dy == 0 || childCount == 0) return 0
//        debug("dy == $dy")

        val realDy = fillVertically(recycler, dy)
        val consumed = if (isLoop) dy else realDy
        debug("consumed == $consumed")

        recyclerVertically(recycler, consumed)
        offsetChildrenVertical(-consumed)

        logChildCount(recycler)
        return consumed
    }

    private fun fill(recycler: RecyclerView.Recycler) {
        if (orientation == VERTICAL) {
            fillVertically(recycler, 0)
        } else {

        }
    }

    private fun fillVertically(recycler: RecyclerView.Recycler, dy: Int): Int {
        when {
            dy == 0 -> {
                initFillVertically(recycler)
            }
            dy > 0 -> {
                return fillVerticallyEnd(recycler, dy)
            }
            dy < 0 -> {
                fillVerticallyStart(recycler, dy)
            }
        }
        return dy
    }

    private fun initFillVertically(recycler: RecyclerView.Recycler) {
        var top = 0
        var bottom: Int
        for (i in 0 until visibleCount) {
            val child = getViewForPosition(recycler, i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            bottom = top + getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)
            top = bottom
        }
    }

    //dy>0
    private fun fillVerticallyEnd(recycler: RecyclerView.Recycler, dy: Int): Int {
        if (childCount == 0) return 0

        val lastView = getChildAt(childCount - 1) ?: return 0
        val lastBottom = getDecoratedBottom(lastView)

        //如果当前最后一个child的bottom加上偏移量还是大于rv的height
        //就不用填充itemView，直接返回dy
        if (lastBottom - dy > height) return dy

        val nextPosition = getNextPosition(lastView)
        //如果不是无限循环模式且已经是最后一个itemView，
        debug("height == $height")
        if (!isLoop && nextPosition > itemCount - 1) return min(dy, lastBottom - height)

        var top = lastBottom
        var bottom: Int
        for (i in nextPosition until getInnerItemCount()) {
            val child = getViewForPosition(recycler, i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            bottom = top + getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)

            if (bottom > height) break
            top = bottom
        }

        return if (isLoop) dy else dy
    }

    //dy<0
    private fun fillVerticallyStart(recycler: RecyclerView.Recycler, dy: Int) {
        if (childCount == 0) return

        val firstView = getChildAt(0) ?: return
        if (getDecoratedBottom(firstView) - dy < 0) return

        val prePosition = getPrePosition(firstView)
        if (!isLoop && prePosition < 0) return

        var bottom = getDecoratedTop(firstView)
        var top: Int
        for (i in prePosition downTo 0) {
            val child = getViewForPosition(recycler, i)
            addView(child, 0)
            measureChildWithMargins(child, 0, 0)
            top = bottom - getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)

            if (top < 0) break
            bottom = top
        }
    }

    private fun recyclerVertically(
        recycler: RecyclerView.Recycler,
        dy: Int
    ) {
        if (childCount == 0) return
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue

            //dy>0,recyclerStart
            if (getDecoratedBottom(child) - dy < 0) {
                removeAndRecycleView(child, recycler)
            }
            //dy<0,recyclerEnd
            if (getDecoratedTop(child) - dy > height) {
                removeAndRecycleView(child, recycler)
            }
        }
    }

    private fun getNextPosition(view: View): Int {
        val position = getPosition(view)
        if (isLoop && position == itemCount - 1) return 0

        return position + 1
    }

    private fun getPrePosition(view: View): Int {
        val position = getPosition(view)
        if (isLoop && position == 0) return itemCount - 1

        return position - 1
    }

    private fun getViewForPosition(
        recycler: RecyclerView.Recycler,
        position: Int
    ): View {
        return recycler.getViewForPosition(position)
    }
}