package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder

class TimerService : Service() {

    var isRunning = false
    var isPaused = false
    private var timerHandler: Handler? = null
    private var timerThread: Thread? = null

    // This is what the Activity will use to talk to the Service
    inner class TimerBinder : Binder() {
        fun getService() = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun setHandler(handler: Handler) {
        timerHandler = handler
    }

    fun start(startValue: Int) {
        if (!isRunning) {
            isRunning = true
            isPaused = false
            
            // Start a background thread to count down
            timerThread = Thread {
                try {
                    for (i in startValue downTo 0) {
                        // Update the UI
                        timerHandler?.sendEmptyMessage(i)
                        
                        // If paused, wait here
                        while (isPaused) {
                            Thread.sleep(100)
                        }
                        
                        // Wait one second
                        Thread.sleep(1000)
                    }
                } catch (e: InterruptedException) {
                    // This happens when we call stop()
                } finally {
                    isRunning = false
                    isPaused = false
                }
            }
            timerThread?.start()
        }
    }

    fun pause() {
        if (isRunning) {
            isPaused = !isPaused
        }
    }

    fun stop() {
        timerThread?.interrupt()
        isRunning = false
        isPaused = false
    }
}
