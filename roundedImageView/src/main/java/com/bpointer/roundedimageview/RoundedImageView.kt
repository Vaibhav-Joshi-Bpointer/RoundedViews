package com.bpointer.roundedimageview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import kotlin.math.max

class RoundedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        // Constants for tile mode attributes
        val TILE_MODE_UNDEFINED: Int = -2
        val TILE_MODE_CLAMP: Int = 0
        val TILE_MODE_REPEAT: Int = 1
        val TILE_MODE_MIRROR: Int = 2

        val TAG: String = "RoundedImageView"
        val DEFAULT_RADIUS: Float = 0f
        val DEFAULT_BORDER_WIDTH: Float = 0f
        val DEFAULT_TILE_MODE: TileMode = TileMode.CLAMP
        val SCALE_TYPES: Array<ScaleType?> = arrayOf<ScaleType?>(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )
    }

    private val mCornerRadii: FloatArray? =
        floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)

    private var mBackgroundDrawable: Drawable? = null
    private var mBorderColor: ColorStateList? = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mColorFilter: ColorFilter? = null
    private var mColorMod = false
    private var mDrawable: Drawable? = null
    private var mHasColorFilter = false
    private var mIsOval = false
    private var mMutateBackground = false
    private var mResource = 0
    private var mBackgroundResource = 0
    private var mScaleType: ScaleType? = null
    private var mTileModeX: TileMode? = DEFAULT_TILE_MODE
    private var mTileModeY: TileMode? = DEFAULT_TILE_MODE


    private fun parseTileMode(tileMode: Int): TileMode? {
        return when (tileMode) {
            RoundedImageView.TILE_MODE_CLAMP -> TileMode.CLAMP
            RoundedImageView.TILE_MODE_REPEAT -> TileMode.REPEAT
            RoundedImageView.TILE_MODE_MIRROR -> TileMode.MIRROR
            else -> null
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun getScaleType(): ScaleType? {
        return mScaleType
    }

    override fun setScaleType(scaleType: ScaleType) {
        checkNotNull(scaleType)

        if (mScaleType != scaleType) {
            mScaleType = scaleType

            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )

                else -> super.setScaleType(scaleType)
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }


    override fun setImageBitmap(bm: Bitmap?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(getDrawable())
    }


    private fun resolveResource(): Drawable? {
        val rsrc = getResources()
        if (rsrc == null) {
            return null
        }

        var d: Drawable? = null

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: " + mResource, e)
                // Don't try again.
                mResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    override fun setBackground(background: Drawable?) {
        setBackgroundDrawable(background)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        if (mBackgroundResource != resId) {
            mBackgroundResource = resId
            mBackgroundDrawable = resolveBackgroundResource()
            setBackgroundDrawable(mBackgroundDrawable)
        }
    }

    override fun setBackgroundColor(color: Int) {
        mBackgroundDrawable = ColorDrawable(color)
        setBackgroundDrawable(mBackgroundDrawable)
    }

    private fun resolveBackgroundResource(): Drawable? {
        val rsrc = getResources()
        if (rsrc == null) {
            return null
        }

        var d: Drawable? = null

        if (mBackgroundResource != 0) {
            try {
                d = rsrc.getDrawable(mBackgroundResource)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: " + mBackgroundResource, e)
                // Don't try again.
                mBackgroundResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }


    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable, mScaleType)
    }

    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mMutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable)
            }
            updateAttrs(mBackgroundDrawable, ScaleType.FIT_XY)
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        if (mColorFilter !== cf) {
            mColorFilter = cf
            mHasColorFilter = true
            mColorMod = true
            applyColorMod()
            invalidate()
        }
    }

    private fun applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable!!.mutate()
            if (mHasColorFilter) {
                mDrawable!!.setColorFilter(mColorFilter)
            }
            // TODO: support, eventually...
            //mDrawable.setXfermode(mXfermode);
            //mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private fun updateAttrs(drawable: Drawable?, scaleType: ScaleType?) {
        if (drawable == null) {
            return
        }

        if (drawable is RoundedDrawable) {
            drawable
                .setScaleType(scaleType)
                .setBorderWidth(mBorderWidth)
                .apply {
                    if (mBorderColor != null) {
                        setBorderColor(mBorderColor)
                    }
                }
                .setOval(mIsOval)
                .setTileModeX(mTileModeX ?: TileMode.CLAMP)
                .setTileModeY(mTileModeY ?: TileMode.CLAMP)

            if (mCornerRadii != null) {
                drawable.setCornerRadius(
                    mCornerRadii[Corner.TOP_LEFT],
                    mCornerRadii[Corner.TOP_RIGHT],
                    mCornerRadii[Corner.BOTTOM_RIGHT],
                    mCornerRadii[Corner.BOTTOM_LEFT]
                )
            }

            applyColorMod()
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            val ld = drawable
            var i = 0
            val layers = ld.getNumberOfLayers()
            while (i < layers) {
                updateAttrs(ld.getDrawable(i), scaleType)
                i++
            }
        }
    }

    override fun setBackgroundDrawable(background: Drawable?) {
        mBackgroundDrawable = background
        updateBackgroundDrawableAttrs(true)
        super.setBackgroundDrawable(mBackgroundDrawable)
    }
    fun getCornerRadius(): Float {
        return getMaxCornerRadius()
    }
    fun getMaxCornerRadius(): Float {
        var maxRadius = 0f
        for (r in mCornerRadii!!) {
            maxRadius = max(r.toDouble(), maxRadius.toDouble()).toFloat()
        }
        return maxRadius
    }
    fun getCornerRadius(@Corner corner: Int): Float {
        return mCornerRadii!![corner]
    }
    fun setCornerRadiusDimen(@DimenRes resId: Int) {
        val radius = getResources().getDimension(resId)
        setCornerRadius(radius, radius, radius, radius)
    }
    fun setCornerRadiusDimen(@Corner corner: Int, @DimenRes resId: Int) {
        setCornerRadius(corner, getResources().getDimensionPixelSize(resId).toFloat())
    }
    fun setCornerRadius(radius: Float) {
        setCornerRadius(radius, radius, radius, radius)
    }
    fun setCornerRadius(@Corner corner: Int, radius: Float) {
        if (mCornerRadii!![corner] == radius) {
            return
        }
        mCornerRadii[corner] = radius

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }
    fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        if (mCornerRadii!![Corner.TOP_LEFT] == topLeft && mCornerRadii[Corner.TOP_RIGHT] == topRight && mCornerRadii[Corner.BOTTOM_RIGHT] == bottomRight && mCornerRadii[Corner.BOTTOM_LEFT] == bottomLeft) {
            return
        }

        mCornerRadii[Corner.TOP_LEFT] = topLeft
        mCornerRadii[Corner.TOP_RIGHT] = topRight
        mCornerRadii[Corner.BOTTOM_LEFT] = bottomLeft
        mCornerRadii[Corner.BOTTOM_RIGHT] = bottomRight

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }
    fun getBorderWidth(): Float {
        return mBorderWidth
    }

    fun setBorderWidth(@DimenRes resId: Int) {
        setBorderWidth(getResources().getDimension(resId))
    }

    fun setBorderWidth(width: Float) {
        if (mBorderWidth == width) {
            return
        }

        mBorderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    @ColorInt
    fun getBorderColor(): Int {
        return mBorderColor!!.getDefaultColor()
    }

    fun setBorderColor(@ColorInt color: Int) {
        setBorderColor(ColorStateList.valueOf(color))
    }

    fun getBorderColors(): ColorStateList? {
        return mBorderColor
    }

    fun setBorderColor(colors: ColorStateList?) {
        if (mBorderColor == colors) {
            return
        }

        mBorderColor =
            if (colors != null) colors else ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (mBorderWidth > 0) {
            invalidate()
        }
    }
    fun isOval(): Boolean {
        return mIsOval
    }

    fun setOval(oval: Boolean) {
        mIsOval = oval
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun getTileModeX(): TileMode? {
        return mTileModeX
    }

    fun setTileModeX(tileModeX: TileMode?) {
        if (this.mTileModeX == tileModeX) {
            return
        }

        this.mTileModeX = tileModeX
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun getTileModeY(): TileMode? {
        return mTileModeY
    }

    fun setTileModeY(tileModeY: TileMode?) {
        if (this.mTileModeY == tileModeY) {
            return
        }

        this.mTileModeY = tileModeY
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }
    fun mutatesBackground(): Boolean {
        return mMutateBackground
    }
    fun mutateBackground(mutate: Boolean) {
        if (mMutateBackground == mutate) {
            return
        }

        mMutateBackground = mutate
        updateBackgroundDrawableAttrs(true)
        invalidate()
    }

}