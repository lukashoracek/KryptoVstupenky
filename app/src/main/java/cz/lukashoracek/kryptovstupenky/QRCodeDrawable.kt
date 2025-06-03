// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class QRCodeDrawable(var qrBoundingRect: Rect) : Drawable() {
    private val boundingRectanglePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 10F
        alpha = 255
    }

    private val rectangeFillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
        alpha = 100
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(qrBoundingRect, boundingRectanglePaint)
        canvas.drawRect(qrBoundingRect, rectangeFillPaint)
    }

    override fun setAlpha(alpha: Int) {
        boundingRectanglePaint.alpha = alpha
        rectangeFillPaint.alpha = (alpha - 155).coerceAtLeast(0)
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        boundingRectanglePaint.colorFilter = colorFilter
        rectangeFillPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}