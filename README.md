# PickerLayoutManager

一个基于自定义LayoutManager的PickerView，扩展自由度极高。



## 导入依赖

```gr

```

## 如何使用

直接将`PickerLayoutManager`设置给`RecyclerView`即可，`Adapter`完全自定义化。

```kotlin
val pickerLayoutManager = PickerLayoutManager(PickerLayoutManager.VERTICAL)
recyclerView.layoutManager = pickerLayoutManager
recyclerView.adapter = Adapter()
```

`PickerLayoutManager`支持的构造属性

* orientation：摆放子View的方向，默认为VERTICAL
* visibleCount：显示多少个子View，默认为3，切只支持设置奇数
* isLoop：是否支持无限滚动，默认为false
* scaleX：x轴缩放的比例，默认为1.0f
* scaleY：y轴缩放的比例，默认为1.0f
* alpha：未选中item的透明度，默认为1.0f

## 基于PickerLayoutManager实现的扩展View

### DataPickerView和TimePickerView



#### 支持的属性和方法

| 属性        | 方法 | 注释 |
| ------------------- | ------------- | ---- |
| visibleCount        | setVisibleCount | 显示多少个子View |
| isLoop              | setIsLoop | 是否支持无限滚动 |
| scaleX              | setItemScaleX | x轴缩放的比例 |
| scaleY              | setItemScaleY | y轴缩放的比例 |
| alpha               | setItemAlpha | 未选中item的透明度 |
| dividerVisible      | setDividerVisible | 分割线是否可见 |
| dividerColor        | setDividerColor | 分割线的颜色 |
| dividerSize         | setDividerSize | 分割线的大小 |
| dividerMargin       | setDividerMargin | 分割线的边距 |
| selectedTextColor   | setSelectedTextColor | 文字选中的颜色 |
| unSelectedTextColor | setUnSelectedTextColor | 文字未选中的颜色 |
| selectedTextSize    | setSelectedTextSize | 文字选中的大小 |
| unSelectedTextSize  | setUnSelectedTextSize | 文字未必选中的大小 |
| selectedIsBold      | setSelectedIsBold | 文字选中是否加粗 |
| scrollToEnd         | scrollToEnd() | 是否滚动到底部 |

#### 如何使用

直接加入布局文件即可

```xml
    <me.simple.picker.datepicker.DatePickerView
        android:id="@+id/datePickerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:isLoop="true"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:scrollToEnd="true" />

    <me.simple.picker.timepicker.TimePickerView
        android:id="@+id/timePickerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="30dp"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/tvDate" />


```

