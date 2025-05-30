package com.bpointer.roundedimageview

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView.ScaleType
import androidx.annotation.ColorInt
import kotlin.math.max
import kotlin.math.min

class RoundedDrawable(private var mBitmap: Bitmap) : Drawable() {

    private var mBounds = RectF()
    private var mDrawableRect = RectF()
    private var mBitmapRect = RectF()
//    private var mBitmap: Bitmap? = null
    private var mBitmapPaint: Paint? = null
    private var mBitmapWidth = 0
    private var mBitmapHeight = 0
    private var mBorderRect = RectF()
    private var mBorderPaint: Paint? = null
    private var mShaderMatrix = Matrix()
    private var mSquareCornersRect = RectF()

    private var mTileModeX = TileMode.CLAMP
    private var mTileModeY = TileMode.CLAMP
    private var mRebuildShader = true

    private var mCornerRadius = 0f

    // [ topLeft, topRight, bottomLeft, bottomRight ]
    private val mCornersRounded = booleanArrayOf(true, true, true, true)

    private var mOval = false
    private var mBorderWidth = 0f
    private var mBorderColor = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
    private var mScaleType: ScaleType? = ScaleType.FIT_CENTER

    init {
        mBitmap = mBitmap

        mBitmapWidth = mBitmap.getWidth()
        mBitmapHeight = mBitmap.getHeight()
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapPaint = Paint()
        mBitmapPaint?.setStyle(Paint.Style.FILL)
        mBitmapPaint?.setAntiAlias(true)

        mBorderPaint = Paint()
        mBorderPaint?.setStyle(Paint.Style.STROKE)
        mBorderPaint?.setAntiAlias(true)
        mBorderPaint?.setColor(
            mBorderColor.getColorForState(
                getState(),
                DEFAULT_BORDER_COLOR
            )
        )
        mBorderPaint?.setStrokeWidth(mBorderWidth)
    }
/*    @SuppressLint("NotConstructor")
    fun RoundedDrawable(bitmap: Bitmap) {
        mBitmap = bitmap

        mBitmapWidth = bitmap.getWidth()
        mBitmapHeight = bitmap.getHeight()
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapPaint = Paint()
        mBitmapPaint?.setStyle(Paint.Style.FILL)
        mBitmapPaint?.setAntiAlias(true)

        mBorderPaint = Paint()
        mBorderPaint?.setStyle(Paint.Style.STROKE)
        mBorderPaint?.setAntiAlias(true)
        mBorderPaint?.setColor(
            mBorderColor.getColorForState(
                getState(),
                DEFAULT_BORDER_COLOR
            )
        )
        mBorderPaint?.setStrokeWidth(mBorderWidth)
    }*/

    fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
        return (if (bitmap != null) {
            RoundedDrawable(bitmap)
        } else {
            null
        }) as RoundedDrawable?
    }

    fun fromDrawable(drawable: Drawable?): Drawable? {
        if (drawable == null) return null

        return when (drawable) {
            is RoundedDrawable -> drawable

            is LayerDrawable -> {
                val cs = drawable.mutate().constantState
                val ld = (cs?.newDrawable() ?: drawable).mutate() as LayerDrawable

                for (i in 0 until ld.numberOfLayers) {
                    val d = ld.getDrawable(i)
                    ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d) ?: d)
                }
                ld
            }

            else -> {
                val bm = drawableToBitmap(drawable)
                if (bm != null) RoundedDrawable(bm) else drawable
            }
        } as Drawable?
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.getBitmap()
        }

        var bitmap: Bitmap?
        val width = max(drawable.getIntrinsicWidth().toDouble(), 2.0).toInt()
        val height = max(drawable.getIntrinsicHeight().toDouble(), 2.0).toInt()
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            drawable.draw(canvas)
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.w(TAG, "Failed to create bitmap from drawable!")
            bitmap = null
        }

        return bitmap
    }

    fun getSourceBitmap(): Bitmap {
        return mBitmap!!
    }

    override fun isStateful(): Boolean {
        return mBorderColor.isStateful()
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = mBorderColor.getColorForState(state, 0)
        if (mBorderPaint!!.getColor() != newColor) {
            mBorderPaint!!.setColor(newColor)
            return true
        } else {
            return super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float

        when (mScaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)

                mShaderMatrix.reset()
                mShaderMatrix.setTranslate(
                    ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f).toInt().toFloat(),
                    ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat()
                )
            }

            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)

                mShaderMatrix.reset()

                dx = 0f
                dy = 0f

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + mBorderWidth / 2,
                    (dy + 0.5f).toInt() + mBorderWidth / 2
                )
            }

            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()

                if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    scale = 1.0f
                } else {
                    scale = min(
                        (mBounds.width() / mBitmapWidth.toFloat()).toDouble(),
                        (mBounds.height() / mBitmapHeight.toFloat()).toDouble()
                    ).toFloat()
                }

                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)

                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }

        mDrawableRect.set(mBorderRect)
        mRebuildShader = true
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mBounds.set(bounds)

        updateShaderMatrix()
    }


    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            val bitmapShader = BitmapShader(mBitmap!!, mTileModeX, mTileModeY)
            if (mTileModeX == TileMode.CLAMP && mTileModeY == TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint!!.setShader(bitmapShader)
            mRebuildShader = false
        }

        if (mOval) {
            if (mBorderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint!!)
                canvas.drawOval(mBorderRect, mBorderPaint!!)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint!!)
            }
        } else {
            if (any(mCornersRounded)) {
                val radius = mCornerRadius
                if (mBorderWidth > 0) {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint!!)
                    canvas.drawRoundRect(mBorderRect, radius, radius, mBorderPaint!!)
                    redrawBitmapForSquareCorners(canvas)
                    redrawBorderForSquareCorners(canvas)
                } else {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint!!)
                    redrawBitmapForSquareCorners(canvas)
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint!!)
                if (mBorderWidth > 0) {
                    canvas.drawRect(mBorderRect, mBorderPaint!!)
                }
            }
        }
    }

    private fun redrawBitmapForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }

        if (mCornerRadius == 0f) {
            return  // no round corners
        }

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = mCornerRadius

        if (!mCornersRounded[Corner.TOP_LEFT]) {
            mSquareCornersRect.set(left, top, left + radius, top + radius)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint!!)
        }

        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            mSquareCornersRect.set(right - radius, top, right, radius)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint!!)
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            mSquareCornersRect.set(right - radius, bottom - radius, right, bottom)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint!!)
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            mSquareCornersRect.set(left, bottom - radius, left + radius, bottom)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint!!)
        }
    }

    private fun redrawBorderForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }

        if (mCornerRadius == 0f) {
            return  // no round corners
        }

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = mCornerRadius
        val offset = mBorderWidth / 2

        if (!mCornersRounded[Corner.TOP_LEFT]) {
            canvas.drawLine(left - offset, top, left + radius, top, mBorderPaint!!)
            canvas.drawLine(left, top - offset, left, top + radius, mBorderPaint!!)
        }

        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            canvas.drawLine(right - radius - offset, top, right, top, mBorderPaint!!)
            canvas.drawLine(right, top - offset, right, top + radius, mBorderPaint!!)
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            canvas.drawLine(right - radius - offset, bottom, right + offset, bottom, mBorderPaint!!)
            canvas.drawLine(right, bottom - radius, right, bottom, mBorderPaint!!)
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            canvas.drawLine(left - offset, bottom, left + radius, bottom, mBorderPaint!!)
            canvas.drawLine(left, bottom - radius, left, bottom, mBorderPaint!!)
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getAlpha(): Int {
        return mBitmapPaint!!.getAlpha()
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint!!.setAlpha(alpha)
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return mBitmapPaint!!.getColorFilter()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint!!.setColorFilter(cf)
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint!!.setDither(dither)
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint!!.setFilterBitmap(filter)
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    /**
     * @return the corner radius.
     */
    fun getCornerRadius(): Float {
        return mCornerRadius
    }

    /**
     * @param corner the specific corner to get radius of.
     * @return the corner radius of the specified corner.
     */
    fun getCornerRadius(@Corner corner: Int): Float {
        return if (mCornersRounded[corner]) mCornerRadius else 0f
    }

    /**
     * Sets all corners to the specified radius.
     *
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(radius: Float): RoundedDrawable {
        setCornerRadius(radius, radius, radius, radius)
        return this
    }

    /**
     * Sets the corner radius of one specific corner.
     *
     * @param corner the corner.
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(@Corner corner: Int, radius: Float): RoundedDrawable {
        require(!(radius != 0f && mCornerRadius != 0f && mCornerRadius != radius)) { "Multiple nonzero corner radii not yet supported." }

        if (radius == 0f) {
            if (only(corner, mCornersRounded)) {
                mCornerRadius = 0f
            }
            mCornersRounded[corner] = false
        } else {
            if (mCornerRadius == 0f) {
                mCornerRadius = radius
            }
            mCornersRounded[corner] = true
        }

        return this
    }

    /**
     * Sets the corner radii of all the corners.
     *
     * @param topLeft top left corner radius.
     * @param topRight top right corner radius
     * @param bottomRight bototm right corner radius.
     * @param bottomLeft bottom left corner radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(
        topLeft: Float, topRight: Float, bottomRight: Float,
        bottomLeft: Float
    ): RoundedDrawable {
        val radiusSet: MutableSet<Float?> = HashSet<Float?>(4)
        radiusSet.add(topLeft)
        radiusSet.add(topRight)
        radiusSet.add(bottomRight)
        radiusSet.add(bottomLeft)

        radiusSet.remove(0f)

        require(radiusSet.size <= 1) { "Multiple nonzero corner radii not yet supported." }

        if (!radiusSet.isEmpty()) {
            val radius: Float = radiusSet.iterator().next()!!
            require(!(java.lang.Float.isInfinite(radius) || java.lang.Float.isNaN(radius) || radius < 0)) { "Invalid radius value: " + radius }
            mCornerRadius = radius
        } else {
            mCornerRadius = 0f
        }

        mCornersRounded[Corner.TOP_LEFT] = topLeft > 0
        mCornersRounded[Corner.TOP_RIGHT] = topRight > 0
        mCornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0
        mCornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0
        return this
    }

    fun getBorderWidth(): Float {
        return mBorderWidth
    }

    fun setBorderWidth(width: Float): RoundedDrawable {
        mBorderWidth = width
        mBorderPaint!!.setStrokeWidth(mBorderWidth)
        return this
    }

    fun getBorderColor(): Int {
        return mBorderColor.getDefaultColor()
    }

    fun setBorderColor(@ColorInt color: Int): RoundedDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    fun getBorderColors(): ColorStateList {
        return mBorderColor
    }

    fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        mBorderColor = if (colors != null) colors else ColorStateList.valueOf(0)
        mBorderPaint!!.setColor(
            mBorderColor.getColorForState(
                getState(),
                DEFAULT_BORDER_COLOR
            )
        )
        return this
    }

    fun isOval(): Boolean {
        return mOval
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        mOval = oval
        return this
    }

    fun getScaleType(): ScaleType {
        return mScaleType!!
    }

    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) {
            scaleType = ScaleType.FIT_CENTER
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    fun getTileModeX(): TileMode {
        return mTileModeX
    }

    fun setTileModeX(tileModeX: TileMode): RoundedDrawable {
        if (mTileModeX != tileModeX) {
            mTileModeX = tileModeX
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun getTileModeY(): TileMode {
        return mTileModeY
    }

    fun setTileModeY(tileModeY: TileMode): RoundedDrawable {
        if (mTileModeY != tileModeY) {
            mTileModeY = tileModeY
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    private fun only(index: Int, booleans: BooleanArray): Boolean {
        var i = 0
        val len = booleans.size
        while (i < len) {
            if (booleans[i] != (i == index)) {
                return false
            }
            i++
        }
        return true
    }

    private fun any(booleans: BooleanArray): Boolean {
        for (b in booleans) {
            if (b) {
                return true
            }
        }
        return false
    }

    private fun all(booleans: BooleanArray): Boolean {
        for (b in booleans) {
            if (b) {
                return false
            }
        }
        return true
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {
        val TAG: String = "RoundedDrawable"
        val DEFAULT_BORDER_COLOR: Int = Color.BLACK

        fun fromDrawable(drawable: Drawable?): Drawable? {
            drawable ?: return null

            return when (drawable) {
                is RoundedDrawable -> drawable

                is LayerDrawable -> {
                    val cs = drawable.mutate().constantState
                    val ld = (cs?.newDrawable() ?: drawable).mutate() as LayerDrawable

                    for (i in 0 until ld.numberOfLayers) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d) ?: d)
                    }
                    ld
                }

                else -> {
                    val bm = drawableToBitmap(drawable)
                    if (bm != null) RoundedDrawable(bm) else drawable
                }
            }
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) return drawable.bitmap

            return try {
                val width = max(drawable.intrinsicWidth, 2)
                val height = max(drawable.intrinsicHeight, 2)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
            return (if (bitmap != null) {
                RoundedDrawable(bitmap)
            } else {
                null
            }) as RoundedDrawable?
        }

    }

}