package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false
    private var paused = false
    private var timerHandler: Handler? = null
    private var t: TimerThread? = null

    inner class TimerBinder : Binder() {
        val isRunning: Boolean get() = this@TimerService.isRunning
        val paused: Boolean get() = this@TimerService.paused

        fun start(startValue: Int) {
            if (!isRunning) {
                t?.interrupt()
                t = TimerThread(startValue)
                t?.start()
            } else {
                pause()
            }
        }

        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        fun stop() {
            t?.interrupt()
            isRunning = false
            paused = false
        }

        fun pause() {
            this@TimerService.pause()
        }
    }

    override fun onBind(intent: Intent): IBinder = TimerBinder()

    fun pause() {
        paused = !paused
    }

    inner class TimerThread(private val startValue: Int) : Thread() {
        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 0) {
                    timerHandler?.sendEmptyMessage(i)
                    while (paused) {
                        sleep(100)
                    }
                    sleep(1000)
                }
            } catch (e: InterruptedException) {
            } finally {
                isRunning = false
                paused = false
            }
        }
    }
}
