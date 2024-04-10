package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var loadingProgress = 0f
    private var buttonBackgroundColor = Color.BLACK
    private var buttonProgressBarColor = Color.BLUE
    private var buttonProgressCircleColor = Color.WHITE
    private var buttonTextColor = Color.WHITE
    lateinit var valueAnimator: ValueAnimator
    private var textContent = context.getString(R.string.button_download_text)
    private var rectBackground = RectF()
    private var rectProgressbar = RectF()
    private var textBounds = Rect()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                valueAnimator.start()
            }
            ButtonState.Loading -> {
                textContent = context.getString(R.string.button_loading_text)
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                textContent = context.getString(R.string.button_complete_text)
            }
            ButtonState.Waiting -> {
                valueAnimator.cancel()
                textContent = context.getString(R.string.button_download_text)
            }
        }
        invalidate()
    }

    fun startProgressAnimation() {
        buttonState = ButtonState.Clicked
        this.isEnabled = true
    }

    fun cancelProgressAnimation() {
        buttonState = ButtonState.Waiting
        loadingProgress = 0f
        this.isEnabled = true
    }

    fun endProgressAnimation() {
        buttonState = ButtonState.Completed
        loadingProgress = 0f
        this.isEnabled = true
    }

    private fun disableViewDuringAnimation(view: View, animator: ValueAnimator) {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                view.isEnabled = false
                buttonState = ButtonState.Loading
            }

            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                view.isEnabled = true
                buttonState = ButtonState.Completed
            }
        })
    }

    init {
        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        disableViewDuringAnimation(this, valueAnimator)
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.duration = 2500

        valueAnimator.addUpdateListener { animator ->
            loadingProgress = animator.animatedValue as Float
            invalidate()
        }

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, Color.BLACK)
            buttonProgressBarColor = getColor(R.styleable.LoadingButton_progressBarColor, Color.BLUE)
            buttonProgressCircleColor = getColor(R.styleable.LoadingButton_progressCircleColor, Color.WHITE)
            buttonTextColor = getColor(R.styleable.LoadingButton_progressCircleColor, Color.WHITE)
        }

    }

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = buttonBackgroundColor
        style = Paint.Style.FILL
    }

    private val paintProgressCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = buttonProgressCircleColor
        style = Paint.Style.FILL
    }

    private val paintProgressBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = buttonProgressBarColor
        style = Paint.Style.FILL
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 48f
        color = buttonTextColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

         drawBackground(canvas)

        if (buttonState == ButtonState.Loading || buttonState == ButtonState.Completed)
            drawProgressBar(canvas)

        drawText(canvas)

        if (buttonState == ButtonState.Loading)
            drawProgressCircle(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        rectBackground.set(0f, 0f, widthSize.toFloat(), heightSize.toFloat())
        canvas.drawRect(rectBackground, paintBackground)
    }

    private fun drawProgressCircle(canvas: Canvas) {
        paintText.getTextBounds(textContent, 0, textContent.length, textBounds)
        val r = textBounds.height().toFloat()
        val textW = paintText.measureText(textContent)
        val xoff = (textW + widthSize + r) / 2
        val yoff = heightSize / 2f - r / 2f
        canvas.save()
        canvas.translate(xoff, yoff)
        canvas.drawArc(0f, 0f, r, r, 0f, 360f * loadingProgress, true, paintProgressCircle)
        canvas.restore()
    }

    private fun drawProgressBar(canvas: Canvas) {
        rectProgressbar.set(0f, 0f, widthSize * loadingProgress, heightSize.toFloat())
        canvas.drawRect(rectProgressbar, paintProgressBar)
    }
    
    private fun drawText(canvas: Canvas) {
        canvas.drawText(
            textContent,
            widthSize/2f,
            heightSize/2f + paintText.textSize/4f,
            paintText
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}