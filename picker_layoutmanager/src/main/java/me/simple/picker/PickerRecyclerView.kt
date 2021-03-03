package me.simple.picker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

/**
 * 包装PickerLayoutManager的PickerRecyclerView
 */
open class PickerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        const val DIVIDER_VISIBLE = true
        const val DIVIDER_SIZE = 1.0f
        const val DIVIDER_COLOR = Color.LTGRAY
        const val DIVIDER_MARGIN = 0f
    }

    var mOrientation = PickerLayoutManager.VERTICAL
    var mVisibleCount = PickerLayoutManager.VISIBLE_COUNT
    var mIsLoop = PickerLayoutManager.IS_LOOP
    var mScaleX = PickerLayoutManager.SCALE_X
    var mScaleY = PickerLayoutManager.SCALE_Y
    var mAlpha = PickerLayoutManager.ALPHA

    var mDividerVisible = DIVIDER_VISIBLE
    var mDividerSize = DIVIDER_SIZE
    var mDividerColor = DIVIDER_COLOR
    var mDividerMargin = DIVIDER_MARGIN

    private var mDecor: PickerItemDecoration? = null

    init {
        initAttrs(attrs)
        resetLayoutManager(mOrientation, mVisibleCount, mIsLoop, mScaleX, mScaleY, mAlpha)
    }

    open fun initAttrs(attrs: AttributeSet?) {
        val typeA = context.obtainStyledAttributes(
            attrs,
            R.styleable.PickerRecyclerView
        )

        mOrientation = typeA.getInt(R.styleable.PickerRecyclerView_orientation, mOrientation)
        mVisibleCount = typeA.getInt(R.styleable.PickerRecyclerView_visibleCount, mVisibleCount)
        mIsLoop = typeA.getBoolean(R.styleable.PickerRecyclerView_isLoop, mIsLoop)
        mScaleX = typeA.getFloat(R.styleable.PickerRecyclerView_scaleX, mScaleX)
        mScaleY = typeA.getFloat(R.styleable.PickerRecyclerView_scaleY, mScaleY)
        mAlpha = typeA.getFloat(R.styleable.PickerRecyclerView_alpha, mAlpha)

        mDividerVisible =
            typeA.getBoolean(R.styleable.PickerRecyclerView_dividerVisible, mDividerVisible)
        mDividerSize =
            typeA.getDimension(R.styleable.PickerRecyclerView_dividerSize, mDividerSize)
        mDividerColor =
            typeA.getColor(R.styleable.PickerRecyclerView_dividerColor, mDividerColor)
        mDividerMargin =
            typeA.getDimension(R.styleable.PickerRecyclerView_dividerMargin, mDividerMargin)

        typeA.recycle()
    }

    /**
     * 重新设置LayoutManager
     */
    open fun resetLayoutManager(
        orientation: Int = mOrientation,
        visibleCount: Int = mVisibleCount,
        isLoop: Boolean = mIsLoop,
        scaleX: Float = mScaleX,
        scaleY: Float = mScaleY,
        alpha: Float = mAlpha
    ) {
        val lm = PickerLayoutManager(
            orientation,
            visibleCount,
            isLoop,
            scaleX,
            scaleY,
            alpha
        )
        resetLayoutManager(lm)
    }

    open fun resetLayoutManager(lm: PickerLayoutManager) {
        layoutManager = lm
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        initDivider()
        if (layout !is PickerLayoutManager) {
            throw IllegalArgumentException("LayoutManager only can use PickerLayoutManager")
        }
    }

    /**
     * 获取选中的那个item的position
     */
    open fun getSelectedPosition() = layoutManager.getSelectedPosition()

    /**
     *
     */
    override fun getLayoutManager(): PickerLayoutManager {
        return super.getLayoutManager() as PickerLayoutManager
    }

    //重新设置属性值

    open fun setOrientation(orientation: Int) {
        this.mOrientation = orientation
    }

    open fun setVisibleCount(count: Int) {
        this.mVisibleCount = count
    }

    open fun setIsLoop(isLoop: Boolean) {
        this.mIsLoop = isLoop
    }

    open fun setItemScaleX(scaleX: Float) {
        this.mScaleX = scaleX
    }

    open fun setItemScaleY(scaleY: Float) {
        this.mScaleY = scaleY
    }

    open fun setItemAlpha(alpha: Float) {
        this.mAlpha = alpha
    }

    open fun setDividerVisible(visible: Boolean) {
        this.mDividerVisible = visible
    }

    open fun setDividerSize(@Px size: Float) {
        this.mDividerSize = size
    }

    open fun setDividerColor(@ColorInt color: Int) {
        this.mDividerColor = color
    }

    open fun setDividerMargin(margin: Float) {
        this.mDividerMargin = margin
    }

    //设置分割线
    open fun initDivider() {
        removeDivider()

        if (!mDividerVisible) return
        mDecor = PickerItemDecoration(mDividerColor, mDividerSize, mDividerMargin)
        this.addItemDecoration(mDecor!!)
    }

    //删除分割线
    open fun removeDivider() {
        mDecor?.let { removeItemDecoration(it) }
    }

    /**
     *
     */
    fun addOnSelectedItemListener(listener: OnItemSelectedListener) {
        layoutManager.addOnItemSelectedListener(listener)
    }

    /**
     *
     */
    fun removeOnItemSelectedListener(listener: OnItemSelectedListener) {
        layoutManager.removeOnItemSelectedListener(listener)
    }

    /**
     * 删除所有的监听器
     */
    fun removeAllOnItemSelectedListener() {
        layoutManager.removeAllOnItemSelectedListener()
    }

    /**
     *
     */
    fun addOnItemFillListener(listener: PickerLayoutManager.OnItemFillListener) {
        layoutManager.addOnItemFillListener(listener)
    }

    /**
     *
     */
    fun removeOnItemFillListener(listener: PickerLayoutManager.OnItemFillListener) {
        layoutManager.removeOnItemFillListener(listener)
    }

    /**
     *
     */
    fun removeAllItemFillListener() {
        layoutManager.removeAllItemFillListener()
    }

    /**
     * 滚动到最后一个item
     */
    fun scrollToEnd() {
        if (adapter == null) return
        this.post {
            this.scrollToPosition(adapter!!.itemCount - 1)
        }
    }

    /**
     * 平滑的滚动到最后一个item
     */
    fun smoothScrollToEnd() {
        if (adapter == null) return
        this.post {
            this.scrollToPosition(adapter!!.itemCount - 1)
        }
    }
}