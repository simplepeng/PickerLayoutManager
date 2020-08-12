package me.simple.picker

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min

/**
 * @param orientation 摆放子View的方向
 * @param visibleCount 显示多少个子View
 * @param isLoop 是否支持无线滚动
 */
open class PickerLayoutManager(

    val orientation: Int = VERTICAL,

    val visibleCount: Int = 5,

    val isLoop: Boolean = false,

    @FloatRange(from = 0.0, to = 1.0)
    val scaleX: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    val scaleY: Float = 1.0f,

    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float = 1.0f

) : RecyclerView.LayoutManager(),
    RecyclerView.SmoothScroller.ScrollVectorProvider {

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
    private val mSelectedItemListener = mutableListOf<(position: Int) -> Unit>()

    //
    private val mOnItemLayoutListener = mutableListOf<OnItemLayoutListener>()

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
//        return if (orientation == HORIZONTAL) {
//            RecyclerView.LayoutParams(
//                RecyclerView.LayoutParams.WRAP_CONTENT,
//                RecyclerView.LayoutParams.MATCH_PARENT
//            )
//        } else {
//            RecyclerView.LayoutParams(
//                RecyclerView.LayoutParams.MATCH_PARENT,
//                RecyclerView.LayoutParams.WRAP_CONTENT
//            )
//        }

        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
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
//        measureChildWithMargins(itemView, 0, 0)
        itemView.measure(widthSpec, heightSpec)

        mItemWidth = getDecoratedMeasuredWidth(itemView)
        mItemHeight = getDecoratedMeasuredHeight(itemView)

        logDebug("mItemWidth == $mItemWidth -- mItemHeight == $mItemHeight")

        detachAndScrapView(itemView, recycler)

        if (orientation == HORIZONTAL) {
            setMeasuredDimension(mItemWidth * visibleCount, mItemHeight)
        } else {
            setMeasuredDimension(mItemWidth, mItemHeight * visibleCount)
        }
    }

    // 软键盘的弹出和收起都会再次调用这个方法，自己要记录好偏移量
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) return

        detachAndScrapAttachedViews(recycler)
        fill(recycler)

        dispatchLayout()
        transformChildren()
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

        val realDx = fillHorizontal(recycler, dx)
        val consumed = if (isLoop) dx else realDx
//        logDebug("consumed == $consumed")

        offsetChildrenHorizontal(-consumed)
        recyclerHorizontal(recycler, consumed)

        logChildCount(recycler)
//        logChildrenPosition()

        dispatchLayout()
        transformChildren()

        return consumed
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
//        logDebug("consumed == $consumed")

        offsetChildrenVertical(-consumed)
        recyclerVertically(recycler, consumed)

//        logChildCount(recycler)
//        logChildrenPosition()

        dispatchLayout()
        transformChildren()
        return dy
    }

    private fun fill(recycler: RecyclerView.Recycler) {
        if (orientation == HORIZONTAL) {
            fillHorizontal(recycler, 0)
        } else {
            fillVertically(recycler, 0)
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
        logDebug("${this.hashCode()} -- startPosition == $startPosition")

        var top = getVerticallyTopOffset()
        for (i in 0 until visibleCount) {
            logDebug("initFillVertically -- $i")
            val child = getViewForPosition(recycler, startPosition + i)?:continue
            addView(child)
            measureChildWithMargins(child, 0, 0)
            val bottom = top + getDecoratedMeasuredHeight(child)
            layoutDecorated(child, 0, top, getDecoratedMeasuredWidth(child), bottom)
            top = bottom
        }
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
        mStartPosition = 0
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
            val child = getViewForPosition(recycler, i)?:continue
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
            val child = getViewForPosition(recycler, i)?:continue
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

    private fun fillHorizontal(recycler: RecyclerView.Recycler, dx: Int): Int {
        when {
            dx == 0 -> {
                initFillHorizontal(recycler)
            }
            dx > 0 -> {
                return fillHorizontalEnd(recycler, dx)
            }
            dx < 0 -> {
                return fillHorizontalStart(recycler, dx)
            }
        }
        return dx
    }

    private fun initFillHorizontal(recycler: RecyclerView.Recycler) {
        val startPosition = getStartPosition(mStartPosition)

        var left = getHorizontalStartOffset()
        for (i in 0 until visibleCount) {
            val child = getViewForPosition(recycler, startPosition + i)?:continue
            addView(child)
            measureChildWithMargins(child, 0, 0)
            val right = left + getDecoratedMeasuredWidth(child)
            layoutDecorated(child, left, 0, right, getDecoratedMeasuredHeight(child))
            left = right

//            logDebug("initFillHorizontal -- ${getPosition(child)}")
        }
    }

    private fun fillHorizontalEnd(recycler: RecyclerView.Recycler, dx: Int): Int {
        if (childCount == 0) return 0

        val lastView = getChildAt(childCount - 1) ?: return 0
        val lastRight = getDecoratedRight(lastView)

        if (lastRight - dx > width) return dx

        val nextPosition = getNextPosition(lastView)
        //如果不是无限循环模式且已经是最后一个itemView，就返回
        if (!isLoop && nextPosition > itemCount - 1) {
            return min(dx, lastRight - width + getHorizontalScrollOffset())
        }

        var left = lastRight
        var offsetWidth = 0
        for (i in nextPosition until getInnerItemCount()) {
            val child = getViewForPosition(recycler, i)?:continue
            addView(child)
            measureChildWithMargins(child, 0, 0)
            val right = left + getDecoratedMeasuredWidth(child)
            layoutDecorated(child, left, 0, right, getDecoratedMeasuredHeight(child))
            offsetWidth += getDecoratedMeasuredHeight(child)
            logDebug("fillHorizontalEnd -- $i")

            if (offsetWidth >= dx) break
            left = right
        }

        return dx
    }

    private fun fillHorizontalStart(recycler: RecyclerView.Recycler, dx: Int): Int {
        if (childCount == 0) return 0

        val firstView = getChildAt(0) ?: return 0
        val firstRight = getDecoratedRight(firstView)

        if (firstRight - dx < 0) return dx

        val prePosition = getPrePosition(firstView)

        if (!isLoop && prePosition < 0)
            return max(dx, getDecoratedLeft(firstView) - getHorizontalScrollOffset())

        var right = getDecoratedLeft(firstView)
        var left: Int
        var offsetWidth: Int = 0
        for (i in prePosition downTo 0) {
            val child = getViewForPosition(recycler, i)?:continue
            addView(child, 0)
            measureChildWithMargins(child, 0, 0)
            left = right - getDecoratedMeasuredWidth(child)
            layoutDecorated(child, left, 0, right, getDecoratedMeasuredHeight(child))
            offsetWidth += getDecoratedMeasuredHeight(child)
            logDebug("fillHorizontalStart -- $i")

            if (offsetWidth >= abs(dx)) break
            right = left
        }
        return dx
    }

    private fun recyclerHorizontal(
        recycler: RecyclerView.Recycler,
        dx: Int
    ) {
        if (childCount == 0) return

        if (dx > 0) {
            recycleHorizontalStart(recycler)
        } else {
            recycleHorizontalEnd(recycler)
        }
    }

    //dx>0
    private fun recycleHorizontalStart(
        recycler: RecyclerView.Recycler
    ) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            //stop here
            val right = getDecoratedRight(child)
            if (right > 0) {
                recycleCachedView(recycler)
                break
            }
            mCachedViews.add(child)
        }
    }

    //dx<0
    private fun recycleHorizontalEnd(
        recycler: RecyclerView.Recycler
    ) {
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)!!
            val left = getDecoratedLeft(child)
            if (left < width) {
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

    private fun getFixCount() = (visibleCount - 1) / 2

    private fun getStartPosition(position: Int): Int {
        //如果是无限循环模式且开始position=0
        if (isLoop && position == 0) {
            return itemCount - getFixCount()
        }
        //如果不是无限循环模式且开始position=0
        if (!isLoop && position == 0) {
            return 0
        }
        //只要position != 0，就是scrollTo调用过来的或者软键盘影响重新onLayout来的
        if (position != 0) {
            return position - getFixCount()
        }

        return position
    }

    //3-1,5-2,7-3,9-4
    private fun getVerticallyTopOffset(): Int {
        val offset = (visibleCount - 1) / 2 * mItemHeight
        if (!isLoop && mStartPosition == 0) return offset
        return 0
    }

    private fun getHorizontalStartOffset(): Int {
        val offset = (visibleCount - 1) / 2 * mItemWidth
        if (!isLoop && mStartPosition == 0) return offset
        return 0
    }

    private fun getVerticallyScrollOffset(): Int {
        val offset = (visibleCount - 1) / 2 * mItemHeight
        return if (isLoop) 0 else offset
    }

    private fun getHorizontalScrollOffset(): Int {
        val offset = (visibleCount - 1) / 2 * mItemWidth
        return if (isLoop) 0 else offset
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
    ): View? {
        if (isLoop && position > itemCount - 1) {
            return recycler.getViewForPosition(position % itemCount)
        }
        if (position > itemCount - 1) {
            return null
        }

        return recycler.getViewForPosition(position)
    }

    override fun scrollToPosition(position: Int) {
        if (childCount == 0) return
        checkPosition(position)

        mStartPosition = position
        requestLayout()

        dispatchListener(mStartPosition)
    }

    private fun checkPosition(position: Int) {
        if (position < 0 || position > itemCount - 1)
            throw IllegalArgumentException("position is $position,must be >= 0 and < itemCount,")
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (childCount == 0) return
        checkPosition(position)

        val toPosition = fixSmoothToPosition(position)
        val linearSmoothScroller = LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = toPosition
        startSmoothScroll(linearSmoothScroller)
    }

    private fun fixSmoothToPosition(toPosition: Int): Int {
        val fixCount = getFixCount()
        val centerPosition = getPosition(mSnapHelper.findSnapView(this)!!)
        return if (centerPosition < toPosition) toPosition + fixCount else toPosition - fixCount
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) return null

        val firstChildPos = getPosition(mSnapHelper.findSnapView(this)!!)
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
            val centerView = mSnapHelper.findSnapView(this) ?: return
            val centerPosition = getPosition(centerView)
//            mStartPosition = centerPosition
//            dispatchListener(centerPosition)
            scrollToCenter(centerView, centerPosition)
        }
    }

    private fun dispatchListener(position: Int) {
        for (listener in mSelectedItemListener) {
            listener.invoke(position)
        }
    }

    private fun scrollToCenter(centerView: View, centerPosition: Int) {
        val distance = if (orientation == VERTICAL) {
            val destTop = getVerticallySpace() / 2 - getDecoratedMeasuredHeight(centerView) / 2
            destTop - getDecoratedTop(centerView)
        } else {
            val destLeft = getHorizontalSpace() / 2 - getDecoratedMeasuredWidth(centerView) / 2
            destLeft - getDecoratedLeft(centerView)
        }

        smoothOffsetChildren(distance, centerPosition)
    }

    private fun smoothOffsetChildren(amount: Int, centerPosition: Int) {
        var lastValue = amount
        val animator = ValueAnimator.ofInt(amount, 0).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            offsetChildren(lastValue - value)
            lastValue = value
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                mStartPosition = centerPosition
                dispatchListener(centerPosition)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
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
        mStartPosition = 0
    }

    fun addOnSelectedItemListener(listener: (position: Int) -> Unit) {
        mSelectedItemListener.add(listener)
    }

    fun removeOnSelectedItemListener(listener: (position: Int) -> Unit) {
        mSelectedItemListener.remove(listener)
    }

    fun getSelectedPosition(): Int {
        if (childCount == 0) return RecyclerView.NO_POSITION
        val centerView = mSnapHelper.findSnapView(this) ?: return RecyclerView.NO_POSITION
        return getPosition(centerView)
    }

    open fun transformChildren() {
        if (childCount == 0) return

        val centerView = mSnapHelper.findSnapView(this) ?: return
        val centerPosition = getPosition(centerView)

        if (childCount == 0) return
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val position = getPosition(child)
            if (position == centerPosition) {
                child.scaleX = 1f
                child.scaleY = 1f
                child.alpha = 1.0f
            } else {
                val scaleX = transformScale(this.scaleX, getIntervalCount(centerPosition, position))
                val scaleY = transformScale(this.scaleY, getIntervalCount(centerPosition, position))
//                logDebug("scaleY == $scaleY")
                child.scaleX = scaleX
                child.scaleY = scaleY
                child.alpha = this.alpha
            }
        }
    }

    private fun transformScale(scale: Float, intervalCount: Int): Float {
        if (scale == 1.0f) return scale
        return scale / intervalCount
    }

    /**
     * 获取两个position中间相差的item个数
     */
    private fun getIntervalCount(
        centerPosition: Int,
        position: Int
    ): Int {
        if (!isLoop)
            return abs(centerPosition - position)

        //例如：position=100,centerPosition=0这种情况
        if (position > centerPosition && position - centerPosition > visibleCount)
            return itemCount - position

        //例如：position=0,centerPosition=100这种情况
        if (position < centerPosition && centerPosition - position > visibleCount)
            return position + 1

        return abs(position - centerPosition)
    }

    private fun dispatchLayout() {
        if (childCount == 0) return

        val centerView = mSnapHelper.findSnapView(this) ?: return
        val centerPosition = getPosition(centerView)

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)

            if (position == centerPosition) {
                onSelectedItemLayout(child, position)
            } else {
                onUnSelectedItemLayout(child, position)
            }
        }
    }

    open fun onSelectedItemLayout(child: View, position: Int) {
        for (listener in mOnItemLayoutListener) {
            listener.onSelectedItemLayout(child, position)
        }
    }

    open fun onUnSelectedItemLayout(child: View, position: Int) {
        for (listener in mOnItemLayoutListener) {
            listener.onUnSelectedItemLayout(child, position)
        }
    }

    interface OnItemLayoutListener {
        fun onSelectedItemLayout(child: View, position: Int)
        fun onUnSelectedItemLayout(child: View, position: Int)
    }

    fun addOnItemLayoutListener(listener: OnItemLayoutListener) {
        mOnItemLayoutListener.add(listener)
    }

    fun removeOnItemLayoutListener(listener: OnItemLayoutListener) {
        mOnItemLayoutListener.remove(listener)
    }

    class Builder {
        private var orientation = VERTICAL
        private var visibleCount = 3
        private var isLoop = false
        private var scaleX = 1.0f
        private var scaleY = 1.0f
        private var alpha = 1.0f

        fun setOrientation(orientation: Int) {
            this.orientation = orientation
        }

        fun setVisibleCount(visibleCount: Int) {
            this.visibleCount = visibleCount
        }

        fun setIsLoop(isLoop: Boolean) {
            this.isLoop = isLoop
        }

        fun setScaleX(@FloatRange(from = 0.0, to = 1.0) scaleX: Float) {
            this.scaleX = scaleX
        }

        fun setScaleY(@FloatRange(from = 0.0, to = 1.0) scaleY: Float) {
            this.scaleY = scaleY
        }

        fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
            this.alpha = alpha
        }

        fun build() = PickerLayoutManager(orientation, visibleCount, isLoop, scaleX, scaleY, alpha)
    }
}