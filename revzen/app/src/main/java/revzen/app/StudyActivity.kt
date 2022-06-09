package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.view.View
import android.widget.Button
import android.widget.TextView

class StudyActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var minutes = 1
    private var inSession = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        timer = findViewById(R.id.chronometer)
        timer.base = SystemClock.elapsedRealtime() + (minutes * 60000)
        timer.onChronometerTickListener = this
        timer.start()
    }

//    override fun onUserLeaveHint() {
//        super.onUserLeaveHint()
//        timer.stop()
//        startActivity(Intent(this, SummaryActivity::class.java))
//        finish()
//    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis.equals(-0)){
            chronometer.base -= (1000)
        }
        if ((elapsedMillis <= 0) && inSession) {
            setBreakView()
            inSession = false
        } else if ((elapsedMillis > 0) && !inSession) {
            setTimerView()
            inSession = true
        }
    }

    private fun setBreakView() {
        findViewById<TextView>(R.id.studyTitleText).text = resources.getString(R.string.break_title)
        findViewById<TextView>(R.id.warningView).visibility = View.INVISIBLE
        findViewById<Button>(R.id.endSessionButton).text = resources.getString(R.string.break_button)
    }

    private fun setTimerView() {
        findViewById<TextView>(R.id.studyTitleText).text =
            resources.getString(R.string.session_title)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
        findViewById<Button>(R.id.endSessionButton).text = resources.getString(R.string.end_session_button)
    }

    fun goToEndSession(_view: View) {
        if (inSession) {
            startActivity(Intent(this, SummaryActivity::class.java))
        } else {
            startActivity(Intent(this, BreakActivity::class.java))
        }
        finish()
    }
}