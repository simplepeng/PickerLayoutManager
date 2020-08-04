package me.simple.picker_recyclerview

import android.graphics.PointF
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PickerLayoutManager(
    private val orientation: Int = VERTICAL,
    private val visibleCount: Int = 3,
    private val isLoop: Boolean = false
) : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    private var mStartPosition = 0
    private var mItemWidth = 0
    private var mItemHeight = 0

    //增加一个偏移量减少误差
    private var mItemOffset = 0

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL
        const val TAG = "PickerLayoutManager"
    }

    private fun logDebug(msg: String) {
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
        return false
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

        mItemWidth = getDecoratedMeasuredWidth(itemView)
        mItemHeight = getDecoratedMeasuredHeight(itemView)
        mItemOffset = if (orientation == VERTICAL) mItemHeight / 2 else mItemWidth / 2
        logDebug("itemWidth = $mItemWidth -- itemHeight = $mItemHeight -- mItemOffset == $mItemOffset")
        detachAndScrapView(itemView, recycler)

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(mItemWidth * visibleCount, mItemHeight)
        } else {
            setMeasuredDimension(mItemWidth, mItemHeight * visibleCount)
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
//        logDebug("dy == $dy")

        val realDy = fillVertically(recycler, dy)
        val consumed = if (isLoop) dy else realDy
        logDebug("consumed == $consumed")

        offsetChildrenVertical(-consumed)
//        recyclerVertically(recycler, consumed)

//        logChildCount(recycler)
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
                return fillVerticallyStart(recycler, dy)
            }
        }
        return dy
    }

    private fun initFillVertically(recycler: RecyclerView.Recycler) {
        var top = 0
        var bottom: Int
        var endPosition = mStartPosition + visibleCount
        if (!isLoop && endPosition > itemCount - 1) {
            endPosition = itemCount
        }

        for (i in mStartPosition until endPosition) {
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
//        debug("height == $height")
        if (!isLoop && nextPosition > itemCount - 1) return min(dy, lastBottom - height)

        var top = lastBottom
        var bottom: Int
        var offsetHeight: Int = 0
        for (i in nextPosition until getInnerItemCount()) {
            val child = getViewForPosition(recycler, i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            bottom = top + getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)
            offsetHeight += getDecoratedMeasuredHeight(child)
            logDebug("fillVerticallyEnd -- $i")

            //这里判断修改，修改为：dy还剩余多少就还可以摆放多少个itemView
//            if (bottom > height) break
            if (offsetHeight >= dy) break
            top = bottom
        }

        return dy
    }

    //dy<0
    private fun fillVerticallyStart(recycler: RecyclerView.Recycler, dy: Int): Int {
        if (childCount == 0) return 0

        val firstView = getChildAt(0) ?: return 0
        val firstBottom = getDecoratedBottom(firstView)
        //如果第一个itemView的bottom+y的偏移量还是<0就不填充item
        if (firstBottom - dy < 0) return dy

        val prePosition = getPrePosition(firstView)
        //如果不是无限循环模式且已经填充了position=0的item，就返回大的偏移量
        if (!isLoop && prePosition < 0) return max(dy, getDecoratedTop(firstView))

        var bottom = getDecoratedTop(firstView)
        var top: Int
        var offsetHeight: Int = 0
        for (i in prePosition downTo 0) {
            val child = getViewForPosition(recycler, i)
            addView(child, 0)
            measureChildWithMargins(child, 0, 0)
            top = bottom - getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)
            offsetHeight += getDecoratedMeasuredHeight(child)
            logDebug("fillVerticallyStart -- $i")

            if (offsetHeight >= abs(dy)) break
            bottom = top
        }

        return dy
    }

    private fun recyclerVertically(
        recycler: RecyclerView.Recycler,
        dy: Int
    ) {
        if (childCount == 0) return

        if (dy > 0) {
            recycleVerticallyStart(recycler, dy)
        } else {
            recycleVerticallyEnd(recycler)
        }
    }

    private fun recycleVerticallyStart(
        recycler: RecyclerView.Recycler,
        dy: Int
    ) {
        var offsetHeight: Int = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            //stop here
            if (getDecoratedBottom(child) >= 0 - mItemHeight) {
                recycleChildren(recycler, 0, i)
                break
            }
            offsetHeight += getDecoratedMeasuredHeight(child)
        }
    }

    private fun recycleVerticallyEnd(recycler: RecyclerView.Recycler) {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)!!
            //stop here
            if (getDecoratedTop(child) < height + mItemHeight) {
                recycleChildren(recycler, childCount - 1, i)
                break
            }
        }
    }

    private fun recycleChildren(
        recycler: RecyclerView.Recycler,
        startIndex: Int,
        endIndex: Int
    ) {
        //recycleStart
        if (startIndex < endIndex) {
            for (i in startIndex until endIndex) {
                getChildAt(i) ?: continue
                val position = getPosition(getChildAt(i)!!)
                logDebug("recycleStart -- $position")
                removeAndRecycleViewAt(i, recycler)
            }
        }
        //recycleEnd
        if (startIndex > endIndex) {
            for (i in startIndex downTo endIndex + 1) {
                getChildAt(i) ?: continue
                val position = getPosition(getChildAt(i)!!)
                logDebug("recycleEnd -- $position")
                removeAndRecycleViewAt(i, recycler)
            }
        }
    }

    private fun removeChildren(recycler: RecyclerView.Recycler) {
        val scrapList = recycler.scrapList
        logChildCount(recycler)
        scrapList.forEach {
            removeAndRecycleView(it.itemView, recycler)
        }
        logChildCount(recycler)
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
        if (isLoop && position > itemCount - 1) {
            return recycler.getViewForPosition(position % itemCount)
        }
        return recycler.getViewForPosition(position)
    }

    override fun scrollToPosition(position: Int) {
        if (childCount == 0) return

        mStartPosition = position
        if (!isLoop && position > itemCount - 1) {
            mStartPosition = itemCount - 1
        }

        requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (childCount == 0) return

        var toPosition = position
        if (!isLoop && position > itemCount - 1) {
            toPosition = itemCount - 1
        }

//        val scroller = object : LinearSmoothScroller(recyclerView.context) {
//            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
//                val firstView = getChildAt(0) ?: return null
//                val y = targetPosition * mItemHeight - getDecoratedTop(firstView) + height
//                return PointF(0f, y.toFloat())
//            }
//        }
//        scroller.targetPosition = toPosition
//        startSmoothScroll(scroller)

        val linearSmoothScroller = LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) return null
        val firstChildPos = getPosition(getChildAt(0)!!)
        val direction = if (targetPosition < firstChildPos) -1 else 1
        return if (orientation == LinearLayoutManager.HORIZONTAL) {
            PointF(direction.toFloat(), 0f)
        } else {
            PointF(0f, direction.toFloat())
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        logDebug("onSaveInstanceState")
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        logDebug("onRestoreInstanceState")
        super.onRestoreInstanceState(state)
    }


}