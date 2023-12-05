package com.dyharlan.coffee_8

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.SurfaceView

class Chip8SurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs){
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if(!resources.getBoolean(R.bool.is_landscape)){
            val width = 2
            val height = 1
            val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
            val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
            val calculatedHeight = originalWidth * height / width
            val finalWidth: Int
            val finalHeight: Int
            if (calculatedHeight > originalHeight) {
                finalWidth = originalHeight * width / height
                finalHeight = originalHeight
            } else {
                finalWidth = originalWidth
                finalHeight = calculatedHeight
            }
            val measureWidth = MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY)
            val measureHeight = MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
            super.onMeasure(measureWidth, measureHeight)
        }

    }
}