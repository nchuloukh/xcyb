package com.lkh.circularrectprogresslayout.utils

import androidx.core.content.res.ResourcesCompat
import com.lkh.circularrectprogresslayout.consts.Global



fun getDimenKt(res: Int): Float {
    val dimenValue = Global.application.resources?.getDimension(res)
    return dimenValue ?: 0f
}

fun getColorKt(res: Int): Int {
    return ResourcesCompat.getColor(Global.application.resources, res, null)
}
