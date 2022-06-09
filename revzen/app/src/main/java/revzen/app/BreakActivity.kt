package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var minutes = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)

        timer = findViewById(R.id.breakTimer)
        timer.base = SystemClock.elapsedRealtime() + (minutes * 60000)
        timer.onChronometerTickListener = this
        timer.start()
    }

    fun keepStudying(_view : View) {
        startActivity(Intent(this, SetupActivity::class.java))
        finish()
    }

    fun endSession(_view : View) {
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }

    override fun onChronometerTick(chronometer: Chronometer?) {
        //SHOW WARNING
    }
}