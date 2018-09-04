package com.anwesh.uiprojects.bouncedownballview

/**
 * Created by anweshmishra on 05/09/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Context
import android.graphics.Color

val nodes : Int = 5

fun Canvas.drawBDBNode(i : Int, scale : Float, paint : Paint) {
    paint.color = Color.parseColor("#0277BD")
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val xGap : Float = (w * 0.9f) / (nodes + 1)
    val r : Float = xGap / 4
    val origX : Float = 0.05f * w + xGap * i + xGap / 2
    val origY : Float = r
    val dy : Float = h - r
    val dx1 : Float = w/2
    val dx2 : Float = w - r
    val y : Float = origY + (dy - origY) * scale
    val x : Float = origX + (dx1 - origX) * sc1 + (dx2 - dx1) * sc2
    save()
    translate(x, y)
    drawCircle(x, y, r, paint)
    restore()

}

class BounceDownBallView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BDBNode(var i : Int, val state : State = State()) {
        private var next : BDBNode? = null
        private var prev : BDBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BDBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBDBNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BDBNode {
            var curr : BDBNode? = this.prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BounceDownBall(var i : Int) {

        private var root : BDBNode = BDBNode(0)
        private var curr : BDBNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BounceDownBallView) {

        private val animator : Animator = Animator(view)
        private val bdb : BounceDownBall = BounceDownBall(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            bdb.draw(canvas, paint)
            animator.animate {
                bdb.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bdb.startUpdating {
                animator.start()
            }
        }
    }
}