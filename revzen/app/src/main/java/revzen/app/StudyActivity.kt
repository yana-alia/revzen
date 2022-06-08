package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.view.View
import android.widget.TextView

class StudyActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener  {
    private lateinit var timer : Chronometer
    private var minutes = 1
    private var state = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        timer = findViewById(R.id.chronometer)
        timer.base = SystemClock.elapsedRealtime() + (minutes * 60000)
        timer.onChronometerTickListener = this
        timer.start()
    }

    fun endSessionManually(view: View) {
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if ((elapsedMillis <= 0) && state) {
            setBreakView()
            state = false
        } else if ((elapsedMillis <= 0) && state) {
            setTimerView()
            state = true
        }
    }

    private fun setBreakView() {
        findViewById<TextView>(R.id.studyTitleText).text = resources.getString(R.string.break_title)
    }

    private fun setTimerView() {
        findViewById<TextView>(R.id.studyTitleText).text = resources.getString(R.string.session_title)
    }
}