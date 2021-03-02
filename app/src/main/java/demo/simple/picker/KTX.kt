package demo.simple.picker

import android.content.res.Resources

val Float.dp: Float
    get() = Resources.getSystem().displayMetrics.density * this + 0.5f