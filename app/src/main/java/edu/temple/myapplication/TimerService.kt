package edu.temple.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder

class TimerService : Service() {

    var isRunning = false
    var isPaused = false
    var currentValue = 0
    private var timerHandler: Handler? = null
    private var timerThread: Thread? = null

    private val preferences by lazy {
        getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
    }

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
            currentValue = startValue
            
            timerThread = Thread {
                try {
                    while (currentValue >= 0) {
                        timerHandler?.sendEmptyMessage(currentValue)
                        while (isPaused) {
                            Thread.sleep(100)
                        }
                        Thread.sleep(1000)
                        currentValue--
                    }
                    preferences.edit().remove("pausedValue").apply()
                } catch (e: InterruptedException) {
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
            if (isPaused) {
                preferences.edit().putInt("pausedValue", currentValue).apply()
            } else {
                preferences.edit().remove("pausedValue").apply()
            }
        }
    }

    fun stop() {
        timerThread?.interrupt()
        isRunning = false
        isPaused = false
        currentValue = 0
        preferences.edit().remove("pausedValue").apply()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (!isPaused) {
            preferences.edit().remove("pausedValue").apply()
        }
        return super.onUnbind(intent)
    }
}
