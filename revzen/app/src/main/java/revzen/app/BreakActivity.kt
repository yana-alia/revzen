package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var breakLength = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)

        val extras = getIntent().extras
        if(extras != null) {
            breakLength = extras.get("breakLength") as Int
        }

        timer = findViewById(R.id.breakTimer)
        timer.base = SystemClock.elapsedRealtime() + (breakLength * 60000)
        timer.onChronometerTickListener = this
        timer.start()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
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