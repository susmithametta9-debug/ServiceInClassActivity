package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerService: TimerService? = null
    private var isConnected = false

    // This handles the message from the Service to update the UI
    private val timerHandler = Handler(Looper.getMainLooper()) {
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = it.what.toString()
        
        // If it hits 0, update the buttons back to "Start"
        if (it.what == 0) {
            findViewById<Button>(R.id.startButton).text = "Start"
            invalidateOptionsMenu()
        }
        true
    }

    // This connects our Activity to the Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            timerService?.setHandler(timerHandler)
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        // Bind to the service when the activity starts
        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        startButton.setOnClickListener {
            if (isConnected) {
                val service = timerService!!
                if (!service.isRunning) {
                    service.start(100)
                    startButton.text = "Pause"
                } else {
                    service.pause()
                    startButton.text = if (service.isPaused) "Unpause" else "Pause"
                }
                invalidateOptionsMenu()
            }
        }

        stopButton.setOnClickListener {
            if (isConnected) {
                timerService?.stop()
                startButton.text = "Start"
                findViewById<TextView>(R.id.textView).text = "0"
                invalidateOptionsMenu()
            }
        }
    }

    // Create the menu items (Start and Stop)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.timer_menu, menu)
        return true
    }

    // Update menu text (Pause/Unpause) before it shows
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val startItem = menu?.findItem(R.id.action_start)
        timerService?.let {
            if (it.isRunning) {
                startItem?.title = if (it.isPaused) "Unpause" else "Pause"
            } else {
                startItem?.title = "Start"
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    // Handle clicks on the menu items
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_start -> {
                findViewById<Button>(R.id.startButton).performClick()
            }
            R.id.action_stop -> {
                findViewById<Button>(R.id.stopButton).performClick()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isConnected) {
            unbindService(serviceConnection)
        }
    }
}
