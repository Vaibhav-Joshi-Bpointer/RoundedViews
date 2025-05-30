package com.bpointer.roundedviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Shader.TileMode
import android.widget.ImageView.ScaleType

class StreamItem @JvmOverloads constructor(
    c: Context, resid: Int, val mLine1: String?, val mLine2: String?, val mScaleType: ScaleType,
    val mTileMode: TileMode? = TileMode.CLAMP
) {
    val mBitmap: Bitmap? = BitmapFactory.decodeResource(c.getResources(), resid)
}
