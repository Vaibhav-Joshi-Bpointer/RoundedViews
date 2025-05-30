package com.bpointer.roundedimageview

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(Corner.TOP_LEFT, Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT)
annotation class Corner {
    companion object {
        const val TOP_LEFT = 0
        const val TOP_RIGHT = 1
        const val BOTTOM_RIGHT = 2
        const val BOTTOM_LEFT = 3
    }
}
