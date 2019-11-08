package com.xyz.swipelayoutkotlin

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import kotlinx.android.synthetic.main.swipe_button.view.*


class SwipeLayout : FrameLayout {

    private var gestureDetector: GestureDetector? = null
    private var animator: ValueAnimator? = null
    private var flingAnimation: FlingAnimation? = null
    private var anim: Animation? = null
    private var triggered = false
    var vibe = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.swipe_button, this, true)
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                animateShakeButton()
                return true
            }

            override fun onScroll(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
                cancelAnimations()
                setDragProgress(motionEvent1.x)
                return true
            }

            override fun onFling(downEvent: MotionEvent, moveEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (velocityX < 0) {
                    return false
                }
                cancelAnimations()

                val max = width.toFloat()
                val startValue = moveEvent.x
                if (startValue > max) return false
                vibe.vibrate(100)
                flingAnimation = FlingAnimation(FloatValueHolder(startValue))
                flingAnimation!!.setStartVelocity(velocityX)
                        .setMaxValue(max)
                        .setFriction(FLING_FRICTION)
                        .addUpdateListener { animation, value, velocity -> setDragProgress(value) }
                        .addEndListener { animation, canceled, value, velocity -> onDragFinished(value) }
                        .start()
                return true
            }
        })

        gestureDetector!!.setIsLongpressEnabled(false)

        //TODO duration needs to change change later
        blinkAnimation(ivOne, 10, 500)
        blinkAnimation(ivTwo, 30, 500)
        blinkAnimation(ivThree, 50, 500)


        blinkAnimation(ivArrow13, 65, 500)
        blinkAnimation(ivArrow12, 60, 500)
        blinkAnimation(ivArrow11, 55, 500)
        blinkAnimation(ivArrow10, 50, 500)
        blinkAnimation(ivArrow9, 45, 500)
        blinkAnimation(ivArrow8, 40, 500)
        blinkAnimation(ivArrow7, 35, 500)
        blinkAnimation(ivArrow6, 30, 500)
        blinkAnimation(ivArrow5, 25, 500)
        blinkAnimation(ivArrow4, 20, 500)
        blinkAnimation(ivArrow3, 15, 500)
        blinkAnimation(ivArrow2, 10, 500)
        blinkAnimation(ivArrow1, 5, 500)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (triggered) {
            return true
        }
        if (gestureDetector!!.onTouchEvent(event)) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_UP -> onDragFinished(event.x)
        }

        return true
    }

    private fun setDragProgress(x: Float) {
        val translation = calculateTranslation(x)
        // setPadding(translation, 0, -translation, 0)
        if (!triggered) {
            llOverlayLayout!!.alpha = x / width

            val width = Math.min(x - llOverlayLayout!!.x - translation.toFloat(), viewBackground!!.width.toFloat()).toInt()
            llOverlayLayout!!.layoutParams.width = width
            llOverlayLayout!!.requestLayout()
            llTextContainer!!.alpha = 1 - llOverlayLayout!!.alpha
            if (width > 140) {
                ivMove.layoutParams.width = Math.min(x - llOverlayLayout!!.x - translation.toFloat(), viewBackground!!.width.toFloat()).toInt()
                ivMove.requestLayout()
            } else {
                llOverlayLayout.alpha = 0f
                ivMove.layoutParams.width = 140
                ivMove.requestLayout()
            }
        } else {
            Log.e("dragging back  ", " true ")
            llOverlayLayout.alpha = 1f
            llOverlayLayout.layoutParams.width = viewBackground!!.width
            llOverlayLayout.requestLayout()
            llTextContainer!!.alpha = 0f
        }
    }

    private fun calculateTranslation(x: Float): Int {
        return x.toInt() / 25
    }

    private fun cancelAnimations() {
        if (animator != null) {
            animator!!.cancel()
        }
        if (flingAnimation != null) {
            flingAnimation!!.cancel()
        }
    }

    private fun onDragFinished(finalX: Float) {
        if (finalX > THRESHOLD_FRACTION * width) {
            animateToEnd(finalX)
        } else {
            animateToStart()
        }
    }

    private fun animateToStart() {
        cancelAnimations()
        animator = ValueAnimator.ofFloat(llOverlayLayout!!.width.toFloat(), 0f)
        animator!!.addUpdateListener { valueAnimator -> setDragProgress(valueAnimator.animatedValue as Float) }
        animator!!.duration = ANIMATE_TO_START_DURATION.toLong()
        animator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (triggered) {
                    // completeSubject.onNext(null)
                }
            }
        })
        animator!!.start()
    }

    private fun animateToEnd(currentValue: Float) {
        cancelAnimations()
        var rightEdge = viewBackground!!.width + viewBackground!!.x
        rightEdge += calculateTranslation(rightEdge).toFloat()
        animator = ValueAnimator.ofFloat(currentValue, rightEdge)
        animator!!.addUpdateListener { valueAnimator -> setDragProgress(valueAnimator.animatedValue as Float) }
        animator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                triggered = true
                //ivCheck!!.animate().alpha(1f).duration = ANIMATE_TO_START_DURATION.toLong()
                animateToStart()
                resetToDefaults()
            }
        })
        animator!!.duration = ANIMATE_TO_END_DURATION.toLong()
        animator!!.start()
    }


    private fun resetToDefaults() {
        triggered = false
        /*ivMove!!.layoutParams.width = 40
        ivMove!!.requestLayout()
        layoutOverlay!!.alpha = 0f
        layoutOverlay!!.layoutParams.width = 0
        layoutOverlay!!.requestLayout()
        layoutText!!.alpha = 1f*/
    }


    private fun animateShakeButton() {
        cancelAnimations()
        vibe.vibrate(100)
        var rightEdge = viewBackground!!.width + viewBackground!!.x
        rightEdge += calculateTranslation(rightEdge).toFloat()
        animator = ValueAnimator.ofFloat(0f, rightEdge, 0f, rightEdge / 2, 0f, rightEdge / 4, 0f)
        animator!!.interpolator = AccelerateDecelerateInterpolator()
        animator!!.addUpdateListener { valueAnimator ->
            val translation = calculateTranslation(valueAnimator.animatedValue as Float)
            setPadding(translation, 0, -translation, 0)
        }
        animator!!.duration = ANIMATE_SHAKE_DURATION.toLong()
        animator!!.start()
    }

    private fun blinkAnimation(view: View, offset: Long, duration: Long) {
        anim = AlphaAnimation(0.0f, 1.0f)
        anim!!.startOffset = offset
        anim!!.duration = duration //You can manage the time of the blink with this parameter
        anim!!.repeatMode = Animation.REVERSE
        anim!!.repeatCount = Animation.INFINITE
        view.startAnimation(anim)
    }

    companion object {
        val THRESHOLD_FRACTION = .85
        val ANIMATE_TO_START_DURATION = 100
        val ANIMATE_TO_END_DURATION = 75
        val ANIMATE_SHAKE_DURATION = 300
        val FLING_FRICTION = .85f
    }
}
