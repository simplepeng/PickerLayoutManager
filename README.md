# PickerLayoutManager

[![](https://jitpack.io/v/simplepeng/PickerLayoutManager.svg)](https://jitpack.io/#simplepeng/PickerLayoutManager)

一个基于自定义LayoutManager的PickerView，扩展自由度极高。

![gif_picker.gif](https://i.loli.net/2020/09/07/RBNdOF64fsCuj5U.gif)

## 导入依赖

在根目录的`build.gradle`中添加`jitpack`的仓库

```groovy
allprojects {
    repositories {
    maven { url 'https://www.jitpack.io' }
  }
}
```

在`app`的`build.gradle`添加依赖

```groovy
dependencies {
    implementation 'com.github.simplepeng:PickerLayoutManager:v1.0.2'
}
```

## 如何使用

直接将`PickerLayoutManager`设置给`RecyclerView`即可，`Adapter`完全自定义化。

```kotlin
val pickerLayoutManager = PickerLayoutManager(PickerLayoutManager.VERTICAL)
recyclerView.layoutManager = pickerLayoutManager
recyclerView.adapter = Adapter()
```

`PickerLayoutManager`支持的构造参数属性

* orientation：摆放子View的方向，默认为VERTICAL
* visibleCount：显示多少个子View，默认为3，切只支持设置奇数
* isLoop：是否支持无限滚动，默认为false
* scaleX：x轴缩放的比例，默认为1.0f
* scaleY：y轴缩放的比例，默认为1.0f
* alpha：未选中item的透明度，默认为1.0f

## 监听选中

```kotlin
pickerLayoutManager.addOnItemSelectedListener { position ->
    toast(position.toString()) 
}
```

## 监听选中和取消选中时itemView被填充的回调

```kotlin
pickerLayoutManager.addOnItemFillListener(object : PickerLayoutManager.OnItemFillListener {
    override fun onItemSelected(itemView: View, position: Int) {
        val tvItem = itemView.findViewById<TextView>(R.id.tv_item)
        tvItem.setTextColor(Color.RED)
    }

    override fun onItemUnSelected(itemView: View, position: Int) {
        val tvItem = itemView.findViewById<TextView>(R.id.tv_item)
        tvItem.setTextColor(Color.BLUE)
    }
})       
```

这个方法超级有用，可以实现选中和取消选中itemView的自定义化，下面的几个扩展View都使用到了这个方法回调。

## 设置分割线

分割线基于`ItemDecoration`实现，给RecyclerView添加`PickerItemDecoration`即可。

```kotlin
recyclerView.addItemDecoration(PickerItemDecoration())
```

`PickerItemDecoration`支持`color`，`size`，`margin`等构造参数。

## 基于PickerLayoutManager实现的扩展View

### TextPickerView

![gif_text_picker.gif](https://i.loli.net/2020/09/07/pUMW5o4TKBGlY9R.gif)

这个实现相对简单，就自定义了一个item layout为TextView的Adapter而已，再做了一点自定义属性的封装。

#### 支持的属性和方法

**`注意：`在调用自定义属性的方法后，必须重新调用`resetLayoutManager()`方法才会起作用。**

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
| ~~scrollToEnd~~     | scrollToEnd() | 是否滚动到底部 |

#### 如何使用

在布局中添加TextPickerView

```xml
<me.simple.picker.widget.TextPickerView
    android:id="@+id/textPickerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

设置数据源

```kotlin
val items = mutableListOf<String>()
    for (index in 0 until 100) {
    items.add(index.toString())
}

textPickerView.setData(items)
```

### DataPickerView和TimePickerView

![gif_date_time_picker.gif](https://i.loli.net/2020/09/07/vREh2qdQi1HpgYj.gif)

#### 支持的属性和方法

同上面的`TextPickerView`

#### 如何使用

```xml
<me.simple.picker.datepicker.DatePickerView
   android:id="@+id/datePickerView"
   android:layout_width="match_parent"
   android:layout_height="wrap_content"
   app:isLoop="true"
   app:layout_constraintStart_toStartOf="parent"
   app:layout_constraintTop_toTopOf="parent"
   />

<me.simple.picker.timepicker.TimePickerView
   android:id="@+id/timePickerView"
   android:layout_width="match_parent"
   android:layout_height="wrap_content"
   android:layout_marginTop="30dp"
   app:layout_constraintStart_toStartOf="parent"
   app:layout_constraintTop_toBottomOf="@id/tvDate" />
```

#### 设置数据源

```kotlin
//默认从1949-1-1到当前这天
datePickerView.setDateInterval()
//滚动到当前日期，不可与`scrollToEnd`同时使用
datePickerView.scrollToCurrentDate()
datePickerView.selectedTodayItem()

//默认从0点到24点
timerPickerView.setTimeInterval()
```

#### 监听选中

```kotlin
datePickerView.setOnDateSelectedListener { year, month, day ->
	tvDate.text = "$year-$month-$day"
}

timePickerView.setOnTimeSelectedListener { hour, minute ->
	tvTime.text = "$hour:$minute"
}
```

## 版本迭代

* v1.0.2：`DatePickerView`增加`scrollTodayItem`，`scrollEndItem`等方法，丰富api调用

* v1.0.1：修复`itemCount=1`且`isLoop=true`闪退的bug，`DatePickerView`增加`scrollToCurrentDate`的方法。

* v1.0.0：首次上传