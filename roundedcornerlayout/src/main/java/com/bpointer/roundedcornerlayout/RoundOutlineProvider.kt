package com.bpointer.roundedcornerlayout

import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.ViewOutlineProvider

internal class RoundOutlineProvider(private val cornersHolder: CornersHolder) :
        ViewOutlineProvider() {

    constructor(cornerRadius: Float) :
            this(CornersHolder(cornerRadius, cornerRadius, cornerRadius, cornerRadius))

    override fun getOutline(view: View, outline: Outline) {
        val rectF = RectF(0f, 0f, view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
        val path = Path()
        path.addRoundRectWithRoundCorners(rectF, cornersHolder)
        path.close()
        outline.setConvexPath(path)
    }
}
