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
import kotlin.math.max
import kotlin.math.min

typealias OnItemSelectedListener = (position: Int) -> Unit

/**
 * @param orientation 摆放子View的方向
 * @param visibleCount 显示多少个子View
 * @param isLoop 是否支持无限滚动
 * @param scaleX x轴缩放的比例
 * @param scaleY y轴缩放的比例
 * @param alpha 未选中item的透明度
 */
open class PickerLayoutManager @JvmOverloads constructor(
    var orientation: Int = ORIENTATION,

    var visibleCount: Int = VISIBLE_COUNT,

    var isLoop: Boolean = IS_LOOP,

    @FloatRange(from = 0.0, to = 1.0)
    var scaleX: Float = SCALE_X,

    @FloatRange(from = 0.0, to = 1.0)
    var scaleY: Float = SCALE_Y,

    @FloatRange(from = 0.0, to = 1.0)
    var alpha: Float = ALPHA
) : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    companion object {
        const val HORIZONTAL = RecyclerView.HORIZONTAL
        const val VERTICAL = RecyclerView.VERTICAL

        const val FILL_START = -1
        const val FILL_END = 1

        const val TAG = "PickerLayoutManager"
        var DEBUG = BuildConfig.DEBUG

        const val ORIENTATION = VERTICAL
        const val VISIBLE_COUNT = 3
        const val IS_LOOP = false
        const val SCALE_X = 1.0f
        const val SCALE_Y = 1.0f
        const val ALPHA = 1.0f
    }

    //将要填充的view的position
    private var mPendingFillPosition: Int = RecyclerView.NO_POSITION

    //保存下item的width和height‘’
    private var mItemWidth: Int = 0
    private var mItemHeight: Int = 0

    //将要滚到的position
    private var mPendingScrollToPosition: Int = RecyclerView.NO_POSITION

    //要回收的View先缓存起来
    private val mRecycleViews = hashSetOf<View>()

    //直接搞个SnapHelper来findCenterView
    private val mSnapHelper = LinearSnapHelper()

    //选中中间item的监听器的集合
    private val mOnItemSelectedListener = mutableListOf<OnItemSelectedListener>()

    //子view填充或滚动监听器的集合
    private val mOnItemFillListener = mutableListOf<OnItemFillListener>()

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

        //用第一个view计算宽高，这种方式可能不太好
        val itemView = recycler.getViewForPosition(0)
        addView(itemView)
        //这里不能用measureChild方法，具体看内部源码实现，内部getWidth默认为0
//        measureChildWithMargins(itemView, 0, 0)
        itemView.measure(widthSpec, heightSpec)
        mItemWidth = getDecoratedMeasuredWidth(itemView)
        mItemHeight = getDecoratedMeasuredHeight(itemView)
        logDebug("mItemWidth == $mItemWidth -- mItemHeight == $mItemHeight")
        detachAndScrapView(itemView, recycler)

        //设置宽高
        setWidthAndHeight(mItemWidth, mItemHeight)
    }

    private fun setWidthAndHeight(
        width: Int,
        height: Int
    ) {
        if (orientation == HORIZONTAL) {
            setMeasuredDimension(width * visibleCount, height)
        } else {
            setMeasuredDimension(width, height * visibleCount)
        }
    }

    // 软键盘的弹出和收起，scrollToPosition
    // 都会再次调用这个方法，自己要记录好偏移量
    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        logDebug("onLayoutChildren")
        //如果itemCount==0了，直接移除全部view
        if (mPendingScrollToPosition != RecyclerView.NO_POSITION) {
            if (state.itemCount == 0) {
                removeAndRecycleAllViews(recycler)
                return
            }
        }

        //不支持预测动画，直接return
        if (state.isPreLayout) return

        logDebug("state.itemCount -- ${state.itemCount}")

        //计算当前开始的position
        mPendingFillPosition = 0
        val isScrollTo = mPendingScrollToPosition != RecyclerView.NO_POSITION
        if (isScrollTo) {
            mPendingFillPosition = mPendingScrollToPosition
        } else if (childCount != 0) {
            mPendingFillPosition = getSelectedPosition()
        }
        logDebug("mPendingFillPosition == $mPendingFillPosition")

        //解决当调用notifyDataChanges时itemCount变小
        //且getSelectedPosition>itemCount的bug
        if (mPendingFillPosition >= state.itemCount) {
            mPendingFillPosition = state.itemCount - 1
        }

        //暂时移除全部view，然后重新fill进来
        detachAndScrapAttachedViews(recycler)

        //开始就向下填充
        var anchor = getOffsetSpace()
        var fillDirection = FILL_END
        fillLayout(recycler, state, anchor, fillDirection)

        //如果是isLoop=true，或者是scrollTo或软键盘弹起，再向上填充
        //getAnchorView可能为null，先判断下childCount
        if (childCount != 0) {
            fillDirection = FILL_START
            mPendingFillPosition = getPendingFillPosition(fillDirection)
            anchor = getAnchor(fillDirection)
            fillLayout(recycler, state, anchor, fillDirection)
        }

        //scrollTo过来的要回调onItemSelected
        if (isScrollTo) {
            val centerPosition = getSelectedPosition()
            dispatchOnItemSelectedListener(centerPosition)
        }

        //变换children
        transformChildren()
        //分发Item Fill事件
        dispatchOnItemFillListener()

        //
        logDebug("width == $width -- height == $height")
        logChildCount(recycler)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
        logDebug("onItemsChanged")
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        mPendingScrollToPosition = RecyclerView.NO_POSITION
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

        mPendingScrollToPosition = position
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
        logDebug("onScrollStateChanged -- $state")

        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            val centerView = getSelectedView() ?: return
            val centerPosition = getPosition(centerView)
            scrollToCenter(centerView, centerPosition)
        }
    }

    //------------------------------------------------------------------
    /**
     * 初始化摆放view
     */
    private fun fillLayout(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        anchor: Int,
        fillDirection: Int
    ) {
        var innerAnchor = anchor
        var count = if (fillDirection == FILL_START) getOffsetCount() else getFixVisibleCount()
        while (count > 0 && hasMore(state)) {
            val child = nextView(recycler, fillDirection)
            if (fillDirection == FILL_START) {
                addView(child, 0)
            } else {
                addView(child)
            }
            measureChildWithMargins(child, 0, 0)
            layoutChunk(child, innerAnchor, fillDirection)
            if (fillDirection == FILL_START) {
                innerAnchor -= mOrientationHelper.getDecoratedMeasurement(child)
            } else {
                innerAnchor += mOrientationHelper.getDecoratedMeasurement(child)
            }
            count--
        }
    }

    /**
     * 获取偏移的item count
     * 例如：开始position == 0居中，就要偏移一个item count的距离
     */
    private fun getOffsetCount() = (visibleCount - 1) / 2

    /**
     * 获取真实可见的visible count
     * 例如：传入的visible count=3，但是在isLoop=false的情况下，
     * 开始只用填充2个item view进来就行了
     */
    private fun getFixVisibleCount(): Int {
        if (isLoop) return visibleCount
        return (visibleCount + 1) / 2
    }

    /**
     * 摆放item view
     */
    private fun layoutChunk(
        child: View,
        anchor: Int,
        fillDirection: Int
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

    /**
     * 滑动的统一处理事件
     */
    private fun scrollBy(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount == 0 || delta == 0) return 0

        //开始填充item view
        val consume = fillScroll(delta, recycler, state)
        //移动全部子view
        mOrientationHelper.offsetChildren(-consume)
        //回收屏幕外的view
        recycleChildren(delta, recycler)

        //变换children
        transformChildren()
        //分发事件
        dispatchOnItemFillListener()

        //输出当前屏幕全部的子view
        logChildCount(recycler)
        return consume
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

        val absDelta = abs(delta)
        var remainSpace = abs(delta)
        logDebug("delta == $delta")

        val fillDirection = if (delta > 0) FILL_END else FILL_START

        //检查滚动距离是否可以填充下一个view
        if (canNotFillScroll(fillDirection, absDelta)) {
            return delta
        }

        //检查是否滚动到了顶部或者底部
        if (checkScrollToEdge(fillDirection, state)) {
            val fixLastScroll = getFixLastScroll(fillDirection)
            return if (fillDirection == FILL_START) {
                max(fixLastScroll, delta)
            } else {
                min(fixLastScroll, delta)
            }
        }

        //获取将要填充的view
        mPendingFillPosition = getPendingFillPosition(fillDirection)

        //
        while (remainSpace > 0 && hasMore(state)) {
            val anchor = getAnchor(fillDirection)
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
    private fun canNotFillScroll(fillDirection: Int, delta: Int): Boolean {
        val anchorView = getAnchorView(fillDirection)
        return if (fillDirection == FILL_START) {
            val start = mOrientationHelper.getDecoratedStart(anchorView)
            start + delta < mOrientationHelper.startAfterPadding
        } else {
            val end = mOrientationHelper.getDecoratedEnd(anchorView)
            end - delta > mOrientationHelper.endAfterPadding
        }
    }

    /**
     * 检查是否滚动到了底部或者顶部
     */
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

    /**
     * 获取锚点view的position
     */
    private fun getAnchorPosition(fillDirection: Int): Int {
        return getPosition(getAnchorView(fillDirection))
    }

    /**
     * 获取要开始填充的锚点位置
     */
    private fun getAnchor(
        fillDirection: Int
    ): Int {
        val anchorView = getAnchorView(fillDirection)
        return if (fillDirection == FILL_START) {
            mOrientationHelper.getDecoratedStart(anchorView)
        } else {
            mOrientationHelper.getDecoratedEnd(anchorView)
        }
    }

    /**
     * 获取将要填充的view的position
     */
    private fun getPendingFillPosition(fillDirection: Int): Int {
        return getAnchorPosition(fillDirection) + fillDirection
    }

    /**
     * 获取下一个view，fill_start就-1，fill_end就是+1
     */
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
            recycleStart()
        } else {
            recycleEnd()
        }

        //
        logRecycleChildren()

        //
        for (view in mRecycleViews) {
            removeAndRecycleView(view, recycler)
        }
        mRecycleViews.clear()
    }

    /**
     *  向右或向下移动时，就回收前面部分超出屏幕的子view
     */
    private fun recycleStart() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val end = mOrientationHelper.getDecoratedEnd(child)
            if (end < mOrientationHelper.startAfterPadding - getItemOffset()) {
                mRecycleViews.add(child)
            } else {
                break
            }
        }
    }

    /**
     *  向左或向上移动时，就回收后面部分超出屏幕的子view
     */
    private fun recycleEnd() {
        for (i in (childCount - 1) downTo 0) {
            val child = getChildAt(i)!!
            val start = mOrientationHelper.getDecoratedStart(child)
            if (start > mOrientationHelper.endAfterPadding + getItemOffset()) {
                mRecycleViews.add(child)
            } else {
                break
            }
        }
    }

    /**
     * 获取居中被选中的view
     */
    private fun getSelectedView(): View? {
        return mSnapHelper.findSnapView(this)
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
     * 获取已经滚动过的偏移量，在软键盘弹出
     * 重新onLayoutChildren的时候有用
     * 这个暂时用不到了，因为加了自动居中，在LinearLayoutManager中
     * 还是用到了的
     */
    private fun getScrollOffset() {

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
        return getOffsetCount() * getItemSpace()
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

        //假设itemCount==100
        //[0,99] -- 100=0,101=1,102=2
        if (isLoop && position > itemCount - 1) {
            return recycler.getViewForPosition(position % itemCount)
        }

        //[0,99] -- -1=99,-2=98,-3=97...-99=1,-100=0
        //              -101=99(-1)
        if (isLoop && position < 0) {
            return recycler.getViewForPosition(itemCount + (position % itemCount))
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

    /**
     * 因为scrollTo是要居中，所以这里要fix一下
     */
    private fun fixSmoothToPosition(toPosition: Int): Int {
        val fixCount = getOffsetCount()
        val centerPosition = getSelectedPosition()
        return if (centerPosition < toPosition) toPosition + fixCount else toPosition - fixCount
    }

    /**
     * 分发回调OnItemSelectedListener
     */
    private fun dispatchOnItemSelectedListener(position: Int) {
        if (mOnItemSelectedListener.isEmpty()) return

        for (listener in mOnItemSelectedListener) {
            listener.invoke(position)
        }
    }

    /**
     * 滚动到中间的item
     */
    private fun scrollToCenter(centerView: View, centerPosition: Int) {
        val destination =
            mOrientationHelper.totalSpace / 2 - mOrientationHelper.getDecoratedMeasurement(
                centerView
            ) / 2
        val distance = destination - mOrientationHelper.getDecoratedStart(centerView)

        //平滑动画的滚动到中心
//        smoothOffsetChildren(distance, centerPosition)
        //直接滚动到中心
        mOrientationHelper.offsetChildren(distance)
        dispatchOnItemSelectedListener(centerPosition)
    }

    /**
     * 加动画平滑的移动
     */
    private fun smoothOffsetChildren(amount: Int, centerPosition: Int) {
        var lastValue = amount
        val animator = ValueAnimator.ofInt(amount, 0).apply {
            interpolator = LinearInterpolator()
            duration = 300
        }
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            mOrientationHelper.offsetChildren(lastValue - value)
            lastValue = value
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                dispatchOnItemSelectedListener(centerPosition)
            }
        })
        animator.start()
    }

    /**
     * 添加中心item选中的监听器
     */
    fun addOnItemSelectedListener(listener: OnItemSelectedListener) {
        mOnItemSelectedListener.add(listener)
    }

    fun removeOnItemSelectedListener(listener: OnItemSelectedListener) {
        mOnItemSelectedListener.remove(listener)
    }

    /**
     * 获取被选中的position
     */
    fun getSelectedPosition(): Int {
        if (childCount == 0) return RecyclerView.NO_POSITION
        val centerView = getSelectedView() ?: return RecyclerView.NO_POSITION
        return getPosition(centerView)
    }

    /**
     * 变换子view，缩放或增加透明度
     */
    open fun transformChildren() {
        if (childCount == 0) return

        val centerView = getSelectedView() ?: return
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
                val scaleX = getScale(this.scaleX, getIntervalCount(centerPosition, position))
                val scaleY = getScale(this.scaleY, getIntervalCount(centerPosition, position))
                child.scaleX = scaleX
                child.scaleY = scaleY
                child.alpha = this.alpha
            }
        }
    }

    private fun getScale(scale: Float, intervalCount: Int): Float {
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

    /**
     * 分发OnItemFillListener事件
     */
    private fun dispatchOnItemFillListener() {
        if (childCount == 0 || mOnItemFillListener.isEmpty()) return

        val centerView = getSelectedView() ?: return
        val centerPosition = getPosition(centerView)

        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)

            if (position == centerPosition) {
                onItemSelected(child, position)
            } else {
                onItemUnSelected(child, position)
            }
        }
    }

    open fun onItemSelected(child: View, position: Int) {
        for (listener in mOnItemFillListener) {
            listener.onItemSelected(child, position)
        }
    }

    open fun onItemUnSelected(child: View, position: Int) {
        for (listener in mOnItemFillListener) {
            listener.onItemUnSelected(child, position)
        }
    }

    /**
     * 当item填充或者滚动的时候回调
     */
    interface OnItemFillListener {
        fun onItemSelected(itemView: View, position: Int)
        fun onItemUnSelected(itemView: View, position: Int)
    }

    fun addOnItemFillListener(listener: OnItemFillListener) {
        mOnItemFillListener.add(listener)
    }

    fun removeOnItemFillListener(listener: OnItemFillListener) {
        mOnItemFillListener.remove(listener)
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
        Log.d(TAG, "${hashCode()} -- " + msg)
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

    private fun logRecycleChildren() {
        if (!BuildConfig.DEBUG) return

        val builder = StringBuilder()
        for (child in mRecycleViews) {
            builder.append(getPosition(child))
            builder.append(",")
        }

        if (builder.isEmpty()) return
        logDebug("recycle children == $builder")
    }
}