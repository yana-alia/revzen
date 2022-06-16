package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.view.View
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import java.io.IOException
import java.lang.Math.abs
import kotlin.random.Random

class StudyActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var studyLength = 60.0
    private var breakLength = 15.0
    private var inSession = true
    private var validLeave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val extras = intent.extras
        if(extras != null) {
            studyLength = extras.get("studyLength") as Double
            breakLength = extras.get("breakLength") as Double
        }

        timer = findViewById(R.id.chronometer)
        timer.base = SystemClock.elapsedRealtime() + (studyLength * 60000).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    override fun onUserLeaveHint() {
        if(validLeave){
            return
        }
        super.onUserLeaveHint()
        timer.stop()
        val i = Intent(this, FailActivity::class.java)
        i.putExtra("reason", "leaveApp")
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L) {
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
        validLeave = true
        //leaving via button is considered valid. Leaving by home button is invalid

        if (inSession) {
            val i = Intent(this, FailActivity::class.java)
            i.putExtra("reason", "giveUp")
            startActivity(i)
        } else {
            val i = Intent(this, BreakActivity::class.java)
            i.putExtra("breakLength", breakLength)
            startActivity(i)
        }
        finish()
    }
}