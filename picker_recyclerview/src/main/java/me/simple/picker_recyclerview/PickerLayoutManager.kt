package me.simple.picker_recyclerview

import android.animation.ValueAnimator
import android.graphics.PointF
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @param orientation 摆放子View的方向
 * @param visibleCount 显示多少个子View
 * @param isLoop 是否支持无线滚动
 */
class PickerLayoutManager(
    private val orientation: Int = VERTICAL,
    private val visibleCount: Int = 3,
    private val isLoop: Boolean = false
) : RecyclerView.LayoutManager(),
    RecyclerView.SmoothScroller.ScrollVectorProvider {

    private var mRecyclerView: RecyclerView? = null

    private var mStartPosition = 0
    private var mItemWidth = 0
    private var mItemHeight = 0

    //增加一个偏移量减少误差
    private var mItemOffset = 0

    //要回收的View先缓存起来
    private val mCachedViews = mutableListOf<View>()

    //
    private val mSnapHelper = LinearSnapHelper()

    //
    private val mSelectedItemListener = mutableListOf<OnSelectedItemListener>()

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL
        const val TAG = "PickerLayoutManager"
        var DEBUG = true
    }

    init {
        if (visibleCount % 2 == 0)
            throw IllegalArgumentException("visibleCount == $visibleCount 不能是偶数")
    }

    private fun logDebug(msg: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, msg)
    }

    private fun logChildCount(recycler: RecyclerView.Recycler) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, "childCount == $childCount -- scrapSize == ${recycler.scrapList.size}")
    }

    private fun logChildrenPosition() {
        if (!BuildConfig.DEBUG) return
        val builder = StringBuilder()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            builder.append(getPosition(child!!))
            builder.append(",")
        }
        logDebug("children == $builder")
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
        if (childCount != 0) {
            removeAndRecycleAllViews(recycler)
        }

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
        logDebug("dy == $dy")

        val realDy = fillVertically(recycler, dy)
        val consumed = if (isLoop) dy else realDy
        logDebug("consumed == $consumed")

        offsetChildrenVertical(-consumed)
        recyclerVertically(recycler, consumed)

        logChildCount(recycler)
        logChildrenPosition()
        return dy
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
        val startPosition = getStartPosition(mStartPosition)

        var top = getVerticallyTopOffset()
        for (i in 0 until visibleCount) {
            val child = getViewForPosition(recycler, startPosition + i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            val bottom = top + getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)
            top = bottom
        }
    }

    private fun getStartPosition(position: Int): Int {
        if (isLoop && position == 0) {
            return itemCount - 1
        }
        if (position != 0) {
            return position - 1
        }

        return position
    }

    //3-1,5-2,7-3,9-4
    private fun getVerticallyTopOffset(): Int {
        val offset = (visibleCount - 1) / 2 * mItemHeight
        if (!isLoop && mStartPosition == 0) return offset
        return 0
    }

    private fun getVerticallyScrollOffset(): Int {
        val offset = (visibleCount - 1) / 2 * mItemHeight
        return if (isLoop) 0 else offset
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
        if (!isLoop && prePosition < 0) return max(
            dy,
            getDecoratedTop(firstView) - getVerticallyScrollOffset()
        )

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

    //dy>0
    private fun fillVerticallyEnd(recycler: RecyclerView.Recycler, dy: Int): Int {
        if (childCount == 0) return 0

        val lastView = getChildAt(childCount - 1) ?: return 0
        val lastBottom = getDecoratedBottom(lastView)

        //如果当前最后一个child的bottom加上偏移量还是大于rv的height，就不用填充itemView，直接返回dy
        if (lastBottom - dy > height) return dy

        val nextPosition = getNextPosition(lastView)
        //如果不是无限循环模式且已经是最后一个itemView，就返回
        if (!isLoop && nextPosition > itemCount - 1) {
            return min(dy, lastBottom - height + getVerticallyScrollOffset())
        }

        var top = lastBottom
        var offsetHeight = 0
        for (i in nextPosition until getInnerItemCount()) {
            val child = getViewForPosition(recycler, i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            val bottom = top + getDecoratedMeasuredHeight(child)
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

    private fun recyclerVertically(
        recycler: RecyclerView.Recycler,
        dy: Int
    ) {
        if (childCount == 0) return

        if (dy > 0) {
            recycleVerticallyStart(recycler)
        } else {
            recycleVerticallyEnd(recycler)
        }
    }

    //dy>0
    private fun recycleVerticallyStart(
        recycler: RecyclerView.Recycler
    ) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            //stop here
            val bottom = getDecoratedBottom(child)
            if (bottom > 0) {
                recycleCachedView(recycler)
                break
            }
            mCachedViews.add(child)
            logDebug("bottom == $bottom -- position == ${getPosition(child)}")
        }
    }

    //dy<0
    private fun recycleVerticallyEnd(
        recycler: RecyclerView.Recycler
    ) {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)!!
            val top = getDecoratedTop(child)
            //stop here，-dy = +abs(dy)
            if (top < height) {
//                recycleChildren(recycler, childCount - 1, i)
                recycleCachedView(recycler)
                break
            }
            mCachedViews.add(child)
        }
    }

    private fun recycleCachedView(recycler: RecyclerView.Recycler) {
        for (view in mCachedViews) {
            logDebug("position == ${getPosition(view)}")
            removeAndRecycleView(view, recycler)
        }
        mCachedViews.clear()
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
//        if (!isLoop && position > itemCount - 1) {
//            mStartPosition = itemCount - 1
//        }

//        removeAllViews()
        requestLayout()

//        dispatchListener(mStartPosition)
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

        val linearSmoothScroller = LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = toPosition
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

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (childCount == 0) return
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val centerView = mSnapHelper.findSnapView(this)
            if (centerView == null) {
                dispatchListener(RecyclerView.NO_POSITION)
                return
            }
            val position = getPosition(centerView)
            dispatchListener(position)
            logDebug("selected position == $position")
            scrollToCenter(centerView)
        }
    }

    private fun dispatchListener(position: Int) {
        for (listener in mSelectedItemListener) {
            listener.onSelected(position)
        }
    }

    private fun scrollToCenter(centerView: View) {
        val distance = if (orientation == VERTICAL) {
            val destTop = getVerticallySpace() / 2 - getDecoratedMeasuredHeight(centerView) / 2
            destTop - getDecoratedTop(centerView)
        } else {
            0
        }

        smoothOffsetChildren(distance)
    }

    private fun smoothOffsetChildren(amount: Int) {
        logDebug("amount -- $amount")
        var lastValue = amount
        val animator = ValueAnimator.ofInt(amount, 0).apply {
            interpolator = LinearInterpolator()
            duration = 500
        }
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            logDebug("animatedValue -- $value")
            offsetChildren(lastValue - value)
            lastValue = value
        }
        animator.start()
    }

    private fun offsetChildren(amount: Int) {
        if (orientation == VERTICAL) {
            offsetChildrenVertical(amount)
        } else {
            offsetChildrenHorizontal(amount)
        }
    }

    private fun getVerticallySpace() = height - paddingTop - paddingBottom

    private fun getHorizontalSpace() = width - paddingLeft - paddingRight

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        removeAllViews()
    }

    fun addOnSelectedItemListener(listener: OnSelectedItemListener) {
        mSelectedItemListener.add(listener)
    }

    fun removeOnSelectedItemListener(listener: OnSelectedItemListener) {
        mSelectedItemListener.remove(listener)
    }

    interface OnSelectedItemListener {
        fun onSelected(position: Int)
    }

    override fun onSaveInstanceState(): Parcelable? {
        logDebug("onSaveInstanceState")
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        logDebug("onRestoreInstanceState")
        super.onRestoreInstanceState(state)
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)
        mRecyclerView = view
    }

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)
        mRecyclerView == null
    }
}