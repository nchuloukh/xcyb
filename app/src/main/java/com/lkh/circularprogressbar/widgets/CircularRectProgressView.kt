package com.lkh.circularrectprogresslayout.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.lkh.circularprogressbar.R
import com.lkh.circularrectprogresslayout.utils.getColorKt
import com.lkh.circularrectprogresslayout.utils.getDimenKt


/**
 * 矩形进度条
 * Author: wfj
 * Date: 2023/4/1 10:13
 */

class CircularRectProgressView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributes, defStyleAttr) {

    private var ringStrokeWidth = getDimenKt(R.dimen.dp_5) // 矩形环宽度
    private var halfRingWidth = ringStrokeWidth / 2
    private val maskPadding = 0 // 遮罩内边距
    private var maskColor = getColorKt(R.color.color_80_000000) // 遮罩颜色
    private var textColor = Color.WHITE // 文字颜色
    private var textSize = getDimenKt(R.dimen.dp_36) // 文字大小
    private var cornerRadius = getDimenKt(R.dimen.dp_12) // 圆角半径

    private var progress = 0 // 进度值

    private val outerRect = RectF() // 画矩形环所用的矩形
    private val maskInnerRect = RectF() // 画遮罩矩形环所用的矩形
    private val topLeftArc = RectF() // 左上角圆弧
    private val topRightArc = RectF() // 右上角圆弧
    private val bottomRightArc = RectF() // 右下角圆弧
    private val bottomLeftArc = RectF() // 左下角圆弧
    private val paint = Paint() // 文字画笔
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG) // 遮罩画笔
    private val maskPath = Path()
    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    private var bgColor =  Color.WHITE
    private var progressColor =  Color.WHITE
    private var shaderStartColor =  Color.WHITE
    private var shaderEndColor =  Color.WHITE

    private var isUseShader = false
    private var max = 100f

    // 一条rect width占总进度
    private var widthProgress = 0.21f

    // 圆角占比
    private var cornerProgress = 0.02f

    // 一条rect height占总进度
    private var heightProgress = 0.25f

    private var canDraw = true
    private val progressPath = Path()

    init {

        val a = context.obtainStyledAttributes(attributes, R.styleable.CircularRectProgressView)
        ringStrokeWidth = a.getDimension(
            R.styleable.CircularRectProgressView_crp_ring_stroke_width,
            getDimenKt(R.dimen.dp_5)
        )
        maskColor = a.getColor(
            R.styleable.CircularRectProgressView_crp_mask_color,
            getColorKt(R.color.color_80_000000)
        )
        textColor =
            a.getColor(R.styleable.CircularRectProgressView_crp_text_color, Color.WHITE)
        textSize = a.getDimension(
            R.styleable.CircularRectProgressView_crp_text_size,
            getDimenKt(R.dimen.dp_36)
        )
        cornerRadius = a.getDimension(
            R.styleable.CircularRectProgressView_crp_corner_radius,
            getDimenKt(R.dimen.dp_12)
        )
        progress = a.getInteger(R.styleable.CircularRectProgressView_crp_current_progress, 0)
        bgColor = a.getColor(R.styleable.CircularRectProgressView_crp_bg_color, Color.parseColor("#54575D"))
        progressColor = a.getColor(R.styleable.CircularRectProgressView_crp_progress_color, Color.parseColor("#D9D9D9"))
        isUseShader = a.getBoolean(R.styleable.CircularRectProgressView_crp_is_shader, false)
        shaderStartColor = a.getColor(R.styleable.CircularRectProgressView_crp_start_color, Color.parseColor("#03E8FD"))
        shaderEndColor = a.getColor(R.styleable.CircularRectProgressView_crp_end_color, Color.parseColor("#FF55CE"))
        a.recycle()
        halfRingWidth = ringStrokeWidth / 2

        paint.isAntiAlias = true // 抗锯齿

        rectPaint.color = bgColor
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = ringStrokeWidth
        rectPaint.isAntiAlias = true // 抗锯齿

        maskPaint.color = maskColor

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = ringStrokeWidth
        progressPaint.isAntiAlias = true // 抗锯齿
        progressPaint.strokeCap = Paint.Cap.SQUARE


    }

    fun updateSizeProgress(wProgress: Float, hProgress: Float) {
        this.canDraw = true
        this.widthProgress = wProgress
        this.heightProgress = hProgress
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int
        var height: Int

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else {
            width = suggestedMinimumWidth + paddingLeft + paddingRight
            if (widthMode == MeasureSpec.AT_MOST) {
                width = width.coerceAtMost(widthSize)
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize
        } else {
            height = suggestedMinimumHeight + paddingTop + paddingBottom
            if (heightMode == MeasureSpec.AT_MOST) {
                height = height.coerceAtMost(heightSize)
            }
        }
        outerRect.set(
            halfRingWidth,
            halfRingWidth,
            width.toFloat() - halfRingWidth,
            height.toFloat() - halfRingWidth
        )
        if(isUseShader) {
            val startX = outerRect.left
            val startY = outerRect.top
            val endX = outerRect.right
            val endY = outerRect.bottom
            val colors =
                intArrayOf(shaderStartColor,shaderEndColor) // 渐变色的两种颜色
            val positions = floatArrayOf(0f, 1f) // 渐变色的分界点（范围从0-1）
            val shader: Shader =
                LinearGradient(startX, startY, endX, endY, colors, positions, Shader.TileMode.CLAMP)
            progressPaint.shader = shader
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!canDraw) {
            return
        }
        val centerX = width / 2f // 矩形环中心X坐标
        val centerY = height / 2f // 矩形环中心Y坐标
        outerRect.set(
            halfRingWidth,
            halfRingWidth,
            width.toFloat() - halfRingWidth,
            height.toFloat() - halfRingWidth
        )
        maskInnerRect.set(
            halfRingWidth + maskPadding,
            halfRingWidth + maskPadding,
            width.toFloat() - halfRingWidth - maskPadding,
            height.toFloat() - halfRingWidth - maskPadding
        )

        maskPath.reset()
        maskPath.addRoundRect(maskInnerRect, cornerRadius, cornerRadius, Path.Direction.CW)

        canvas?.drawPath(maskPath, maskPaint)
        canvas?.drawRoundRect(outerRect, cornerRadius, cornerRadius, rectPaint)

        // =========================== 绘制进度 ===========================
        topLeftArc.set(
            outerRect.left,
            outerRect.top,
            outerRect.left + cornerRadius * 2,
            outerRect.top + cornerRadius * 2
        )
        topRightArc.set(
            outerRect.right - cornerRadius * 2,
            outerRect.top,
            outerRect.right,
            outerRect.top + cornerRadius * 2
        )
        bottomRightArc.set(
            outerRect.right - cornerRadius * 2,
            outerRect.bottom - cornerRadius * 2,
            outerRect.right,
            outerRect.bottom
        )
        bottomLeftArc.set(
            outerRect.left,
            outerRect.bottom - cornerRadius * 2,
            outerRect.left + cornerRadius * 2,
            outerRect.bottom
        )

        if (progress > 0) {
            val tempProgress = progress / max
            // 计算进度条路径
            progressPath.reset()
            val startX = outerRect.left + cornerRadius
            val startY = outerRect.top
            progressPath.moveTo(startX, startY)
            val tempRectWidth = outerRect.width() - cornerRadius * 2 + ringStrokeWidth
            val tempRectHeight = outerRect.height() - cornerRadius * 2 + ringStrokeWidth
            if (tempProgress < widthProgress) {
                progressPath.lineTo(
                    startX + (tempRectWidth * (tempProgress / widthProgress)),
                    outerRect.top
                )
            } else {
                progressPath.lineTo(outerRect.right - cornerRadius, startY)
                if (tempProgress < (widthProgress + cornerProgress)) {
                    // 画右上角圆角
                    val topRightSweep = 90f * ((tempProgress - widthProgress) / cornerProgress)
                    progressPath.arcTo(topRightArc, -90f, topRightSweep)
                } else {
                    // 画右上角圆角
                    progressPath.arcTo(topRightArc, -90f, 90f)
                    if (tempProgress < (widthProgress + heightProgress + cornerProgress)) {
                        // 右边宽
                        progressPath.lineTo(
                            outerRect.right,
                            outerRect.top + cornerRadius + (tempRectHeight * ((tempProgress - widthProgress - cornerProgress) / heightProgress))
                        )
                    } else {
                        progressPath.lineTo(outerRect.right, outerRect.bottom - cornerRadius)
                        if (tempProgress < (widthProgress + heightProgress + cornerProgress * 2)) {
                            val bottomRightSweep =
                                90f * ((tempProgress - (widthProgress + heightProgress + cornerProgress)) / cornerProgress)
                            // 画右下角圆角
                            progressPath.arcTo(bottomRightArc, 0f, bottomRightSweep)
                        } else {
                            // 画右下角圆角
                            progressPath.arcTo(bottomRightArc, 0f, 90f)
                            if (tempProgress < (widthProgress * 2 + heightProgress + cornerProgress * 2)) {
                                progressPath.lineTo(
                                    outerRect.right - cornerRadius - (tempRectWidth * ((tempProgress - (widthProgress + heightProgress + cornerProgress * 2)) / widthProgress)),
                                    outerRect.bottom
                                )
                            } else {
                                progressPath.lineTo(
                                    cornerRadius,
                                    outerRect.bottom
                                )
                                if (tempProgress < (widthProgress * 2 + heightProgress + cornerProgress * 3)) {
                                    // 左下角
                                    val bottomLeftSweep =
                                        90f * ((tempProgress - (widthProgress * 2 + heightProgress + cornerProgress * 2)) / cornerProgress)
                                    progressPath.arcTo(bottomLeftArc, 90f, bottomLeftSweep)
                                } else {
                                    // 左下角
                                    progressPath.arcTo(bottomLeftArc, 90f, 90f)
                                    if (tempProgress < (widthProgress * 2 + heightProgress * 2 + cornerProgress * 3)) {
                                        progressPath.lineTo(
                                            outerRect.left,
                                            outerRect.bottom - cornerRadius - (tempRectHeight * ((tempProgress - (widthProgress * 2 + heightProgress + cornerProgress * 3)) / heightProgress))
                                        )
                                    } else {
                                        progressPath.lineTo(
                                            outerRect.left,
                                            cornerRadius
                                        )
                                        // 99 当作100处理， 不然有时候99到100那段没有画，就隐藏了
                                        if (progress <= 98 && tempProgress < (widthProgress * 2 + heightProgress * 2 + cornerProgress * 4)) {
                                            val topLeftSweep =
                                                90f * ((tempProgress - (widthProgress * 2 + heightProgress * 2 + cornerProgress * 3)) / cornerProgress)
                                            progressPath.arcTo(topLeftArc, 180f, topLeftSweep)
                                        } else {
                                            progressPath.arcTo(topLeftArc, 180f, 90f)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 绘制进度条
            canvas?.drawPath(progressPath, progressPaint)
        }
        // =========================== 绘制进度 ===========================

        // 画文字
        paint.color = textColor
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        val text = "$progress%"
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        canvas?.drawText(text, centerX, centerY + textBounds.height() / 2f, paint)
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }

    fun getStrokeWidth() = ringStrokeWidth
}
