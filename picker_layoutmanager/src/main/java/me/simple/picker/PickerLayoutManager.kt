package me.simple.picker

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.*
import kotlin.math.abs

typealias OnSelectedItemListener = (position: Int) -> Unit

/**
 * @param orientation 摆放子View的方向
 * @param visibleCount 显示多少个子View
 * @param isLoop 是否支持无线滚动
 * @param scaleX x轴缩放的比例
 * @param scaleY y轴缩放的比例
 * @param alpha 未选中item的透明度
 */
open class PickerLayoutManager @JvmOverloads constructor(
    val orientation: Int = VERTICAL,
    val visibleCount: Int = 3,
    val isLoop: Boolean = false,
    @FloatRange(from = 0.0, to = 1.0)
    val scaleX: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    val scaleY: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    val alpha: Float = 1.0f
) : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL

        const val FILL_START = -1
        const val FILL_END = 1

        const val TAG = "PickerLayoutManager"
        var DEBUG = true
    }

    //当前居中item的position
    private var mCurrentPosition: Int = 0

    //将要填充的view的position
    private var mPendingFillPosition: Int = RecyclerView.NO_POSITION

    //保存下item的width和height‘’
    private var mItemWidth: Int = 0
    private var mItemHeight: Int = 0

    //将要滚到的position
    private var mPendingScrollPosition: Int = RecyclerView.NO_POSITION

    //要回收的View先缓存起来
    private val mRecycleViews = hashSetOf<View>()

    //直接搞个SnapHelper来findCenterView
    private val mSnapHelper = LinearSnapHelper()

    //选中中间item的监听器
    private val mOnSelectedItemListener = mutableListOf<OnSelectedItemListener>()

    //
    private val mOnItemLayoutListener = mutableListOf<OnItemLayoutListener>()

    //Recyclerview内置的帮助类
    private val mOrientationHelper: OrientationHelper by lazy {
        if (orientation == HORIZONTAL) {
            OrientationHelper.createHorizontalHelper(this)
        } else {
            OrientationHelper.createVerticalHelper(this)
        }
    }

    init {
        if (visibleCount % 2 == 0) {
            throw IllegalArgumentException("visibleCount == $visibleCount 不能是偶数")
        }
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (orientation == HORIZONTAL) {
            RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
        } else {
            RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }
    }

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

    // 软键盘的弹出和收起，scrollToPosition
    // 都会再次调用这个方法，自己要记录好偏移量
    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }

        //不支持预测动画，直接return
        if (state.isPreLayout) return

        calculateCurrentPosition()

        detachAndScrapAttachedViews(recycler)
        fillLayout(recycler)

        //
        logDebug("width == $width -- height == $height")
        logChildCount(recycler)
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        mPendingScrollPosition = RecyclerView.NO_POSITION
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
        if (orientation == VERTICAL) return 0

        return scrollBy(dx, recycler, state)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (orientation == HORIZONTAL) return 0

        return scrollBy(dy, recycler, state)
    }

    override fun scrollToPosition(position: Int) {
        if (childCount == 0) return
        checkToPosition(position)

        mPendingScrollPosition = position
        requestLayout()
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (childCount == 0) return
        checkToPosition(position)

        val toPosition = fixSmoothToPosition(position)
        val linearSmoothScroller = LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = toPosition
        startSmoothScroll(linearSmoothScroller)
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

//        if (state == RecyclerView.SCROLL_STATE_IDLE) {
//            val centerView = mSnapHelper.findSnapView(this) ?: return
//            val centerPosition = getPosition(centerView)
//            scrollToCenter(centerView, centerPosition)
//        }
    }

    //------------------------------------------------------------------

    /**
     * 获取itemCount，无限模式就是无限大
     */
    private fun getInnerItemCount() = if (isLoop) Int.MAX_VALUE else super.getItemCount()

    /**
     * 初始化摆放view
     */
    private fun fillLayout(
        recycler: RecyclerView.Recycler
    ) {
        val startPosition = getStartPosition()
        var anchor = getOffsetSpace()
        for (i in 0 until getFixVisibleCount()) {
            val child = getViewForPosition(recycler, startPosition + i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            layoutChunk(child, anchor)
            anchor += mOrientationHelper.getDecoratedMeasurement(child)
        }
    }

    /**
     *
     */
    private fun getFixVisibleCount(): Int {
        if (isLoop) return visibleCount
        return visibleCount / 2 + 1
    }

    /**
     * 摆放item view
     */
    private fun layoutChunk(
        child: View,
        anchor: Int,
        fillDirection: Int = FILL_END
    ) {
        var left: Int = 0
        var top: Int = 0
        var right: Int = 0
        var bottom: Int = 0
        if (orientation == HORIZONTAL) {
            top = paddingTop
            bottom =
                paddingTop + mOrientationHelper.getDecoratedMeasurementInOther(child) - paddingBottom
            if (fillDirection == FILL_START) {
                right = anchor
                left = right - mOrientationHelper.getDecoratedMeasurement(child)
            } else {
                left = anchor
                right = left + mOrientationHelper.getDecoratedMeasurement(child)
            }
        } else {
            left = paddingLeft
            right = mOrientationHelper.getDecoratedMeasurementInOther(child) - paddingRight
            if (fillDirection == FILL_START) {
                bottom = anchor
                top = bottom - mOrientationHelper.getDecoratedMeasurement(child)
            } else {
                top = anchor
                bottom = anchor + mOrientationHelper.getDecoratedMeasurement(child)
            }
        }

        layoutDecoratedWithMargins(child, left, top, right, bottom)
    }

    private fun scrollBy(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount == 0 || delta == 0) return 0

        val consume = fillScroll(delta, recycler, state)
        mOrientationHelper.offsetChildren(-consume)
        recycleChildren(delta, recycler)

        //
        logChildCount(recycler)
        return delta
    }

    /**
     * 在滑动的时候填充view，
     * delta > 0 向右或向下移动
     * delta < 0 向左或向上移动
     */
    private fun fillScroll(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {

        var remainSpace = abs(delta)
        logDebug("remainSpace == $remainSpace")

        val fillDirection = if (delta > 0) FILL_END else FILL_START

        //检查滚动距离是否可以填充下一个view
        if (!canFillScroll(fillDirection, delta)) {
            return delta
        }

        //检查是否滚动到了顶部或者底部
        if (checkScrollToEdge(fillDirection, state)) {
            return getFixLastScroll(fillDirection)
        }

        mPendingFillPosition = getPendingFillPosition(fillDirection)

        while (remainSpace > 0 && hasMore(state)) {
            val anchor = getAnchorByScroll(fillDirection)
            val child = nextView(recycler, fillDirection)
            if (fillDirection == FILL_START) {
                addView(child, 0)
            } else {
                addView(child)
            }
            measureChildWithMargins(child, 0, 0)
            layoutChunk(child, anchor, fillDirection)
            remainSpace -= mOrientationHelper.getDecoratedMeasurement(child)
        }

        return delta
    }

    /**
     * 如果anchorView的(start或end)+delta还是没出现在屏幕内，
     * 就继续滚动，不填充view
     */
    private fun canFillScroll(fillDirection: Int, delta: Int): Boolean {
        val anchorView = getAnchorView(fillDirection)
        return if (fillDirection == FILL_START) {
            val start = mOrientationHelper.getDecoratedStart(anchorView)
            start + delta >= mOrientationHelper.startAfterPadding
        } else {
            val end = mOrientationHelper.getDecoratedEnd(anchorView)
            end - delta <= mOrientationHelper.endAfterPadding
        }
    }

    private fun checkScrollToEdge(
        fillDirection: Int,
        state: RecyclerView.State
    ): Boolean {
        if (isLoop) return false
        val anchorPosition = getAnchorPosition(fillDirection)
        return anchorPosition == 0 || anchorPosition == (state.itemCount - 1)
    }

    private fun getFixLastScroll(fillDirection: Int): Int {
        val anchorView = getAnchorView(fillDirection)
        return if (fillDirection == FILL_START) {
            mOrientationHelper.getDecoratedStart(anchorView) - mOrientationHelper.startAfterPadding - getOffsetSpace()
        } else {
            mOrientationHelper.getDecoratedEnd(anchorView) - mOrientationHelper.endAfterPadding + getOffsetSpace()
        }
    }

    /**
     * 如果不是循环模式，将要填充的view的position不在合理范围内
     * 就返回false
     */
    private fun hasMore(state: RecyclerView.State): Boolean {
        if (isLoop) return true

        return mPendingFillPosition >= 0 && mPendingFillPosition < state.itemCount
    }

    /**
     * 获取锚点view，fill_end是最后一个，fill_start是第一个
     */
    private fun getAnchorView(fillDirection: Int): View {
        return if (fillDirection == FILL_START) {
            getChildAt(0)!!
        } else {
            getChildAt(childCount - 1)!!
        }
    }

    private fun getAnchorPosition(fillDirection: Int) =
        getPosition(getAnchorView(fillDirection))

    private fun getAnchorByScroll(
        fillDirection: Int
    ): Int {
        val anchorView = getAnchorView(fillDirection)
        return if (fillDirection == FILL_START) {
            mOrientationHelper.getDecoratedStart(anchorView)
        } else {
            mOrientationHelper.getDecoratedEnd(anchorView)
        }
    }

    private fun getPendingFillPosition(fillDirection: Int): Int {
        return getAnchorPosition(fillDirection) + fillDirection
    }

    private fun nextView(
        recycler: RecyclerView.Recycler,
        fillDirection: Int
    ): View {
        val child = getViewForPosition(recycler, mPendingFillPosition)
        mPendingFillPosition += fillDirection
        return child
    }

    /**
     * 回收在屏幕外的item view
     */
    private fun recycleChildren(
        delta: Int,
        recycler: RecyclerView.Recycler
    ) {
        if (delta > 0) {
            recycleStart(recycler)
        } else {
            recycleEnd(recycler)
        }
        for (view in mRecycleViews) {
            removeAndRecycleView(view, recycler)
        }
        mRecycleViews.clear()
    }

    private fun recycleStart(recycler: RecyclerView.Recycler) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val end = mOrientationHelper.getDecoratedEnd(child)
            if (end > mOrientationHelper.startAfterPadding) {
                break
            }
            mRecycleViews.add(child)
        }
    }

    private fun recycleEnd(recycler: RecyclerView.Recycler) {
        for (i in (childCount - 1) downTo 0) {
            val child = getChildAt(i)!!
            val start = mOrientationHelper.getDecoratedStart(child)
            if (start < mOrientationHelper.endAfterPadding) {
                break
            }
            mRecycleViews.add(child)
        }
    }

    /**
     * 获取偏移的item count
     * 例如：开始position == 0居中，就要偏移一个item count的距离
     */
    private fun getOffsetCount() = (visibleCount - 1) / 2

    /**
     *  计算当前开始的position
     */
    private fun calculateCurrentPosition() {
        if (mPendingScrollPosition != RecyclerView.NO_POSITION) {
            mCurrentPosition = mPendingScrollPosition
            return
        }

    }

    /**
     * 获取开始fill layout的position
     */
    private fun getStartPosition(): Int {
        val position = mCurrentPosition

        //如果是无限循环模式且开始position=0
        //例如：currentPosition == 0，visibleCount = 3，那么startPosition
        //就应该是100
        if (isLoop && position == 0) {
            return itemCount - getOffsetCount()
        }

        //如果不是无限循环模式且开始position=0
        if (!isLoop && position == 0) {
            return 0
        }

        //只要position != 0，就是scrollTo调用过来的或者软键盘影响重新onLayout来的
        if (position != 0) {
            return position - getOffsetCount()
        }

        return position
    }

    /**
     * 获取一个item占用的空间，横向为宽，竖向为高
     */
    private fun getItemSpace() = if (orientation == HORIZONTAL) {
        mItemWidth
    } else {
        mItemHeight
    }

    /**
     * 增加一个偏移量让滚动顺滑点
     */
    private fun getItemOffset() = getItemSpace() / 2

    /**
     * 获取开始item距离开始位置的偏移量
     * 或者结束item距离尾端的偏移量
     */
    private fun getOffsetSpace(): Int {
        val offset = getOffsetCount() * getItemSpace()
        if (!isLoop) return offset
        return 0
    }

    /**
     * 根据position获取一个item view
     */
    private fun getViewForPosition(
        recycler: RecyclerView.Recycler,
        position: Int
    ): View {
        if (!isLoop && (position < 0 || position >= itemCount)) {
            throw IllegalArgumentException("position <0 or >= itemCount with !isLoop")
        }

        if (isLoop && position > itemCount - 1) {
            return recycler.getViewForPosition(position % itemCount)
        }

        return recycler.getViewForPosition(position)
    }

    /**
     * 检查toPosition是否合法
     */
    private fun checkToPosition(position: Int) {
        if (position < 0 || position > itemCount - 1)
            throw IllegalArgumentException("position is $position,must be >= 0 and < itemCount,")
    }


    private fun fixSmoothToPosition(toPosition: Int): Int {
        val fixCount = getOffsetCount()
        val centerPosition = getPosition(mSnapHelper.findSnapView(this)!!)
        return if (centerPosition < toPosition) toPosition + fixCount else toPosition - fixCount
    }

    /**
     * 分发回调OnSelectedItemListener
     */
    private fun dispatchListener(position: Int) {
        for (listener in mOnSelectedItemListener) {
            listener.invoke(position)
        }
    }

    /**
     * 滚动到中间的item
     */
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
                mCurrentPosition = centerPosition
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

    fun addOnSelectedItemListener(listener: OnSelectedItemListener) {
        mOnSelectedItemListener.add(listener)
    }

    fun removeOnSelectedItemListener(listener: OnSelectedItemListener) {
        mOnSelectedItemListener.remove(listener)
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

    /**
     *
     */
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

    /**
     * 搞个Builder模式，构造函数难得写就用这个
     */
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


// 用来测试的方法--------------------------------------------------

    private fun logDebug(msg: String) {
        if (!DEBUG) return
        Log.d(TAG, msg)
    }

    private fun logChildCount(recycler: RecyclerView.Recycler) {
        if (!BuildConfig.DEBUG) return
        logDebug("childCount == $childCount -- scrapSize == ${recycler.scrapList.size}")
        logChildrenPosition()
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
}