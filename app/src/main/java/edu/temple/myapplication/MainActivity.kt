package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(timerHandler)
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    private val timerHandler = Handler(Looper.getMainLooper()) {
        findViewById<TextView>(R.id.textView).text = it.what.toString()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        bindService(Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE)

        startButton.setOnClickListener {
            if (isConnected) {
                if (!timerBinder.isRunning) timerBinder.start(100) else timerBinder.pause()
                if (timerBinder.paused) startButton.text = "Unpause" else startButton.text = "Pause"
            }
        }

        stopButton.setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
                startButton.text = "Start"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isConnected) {
            unbindService(serviceConnection)
            isConnected = false
        }
    }
}
