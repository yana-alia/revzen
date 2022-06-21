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
import revzen.app.api.ApiHandler
import java.io.IOException
import java.lang.Math.abs
import kotlin.random.Random

class StudyActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var studyLength = 60.0
    private var breakLength = 10.0
    private var studyList = ArrayList<Pair<Int,Int>>()
    private var inSession = true
    private var validLeave = false
    private var originalTime = 0L
    private val MINSTOMILLIS = 60000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val extras = intent.extras
        if (extras != null) {
            studyLength = extras.getDouble("studyLength")
            breakLength = extras.getDouble("breakLength")
            //studyList = extras.get("studyList") as ArrayList<Pair<Int,Int>>
        }

        // updating getting the api handler
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        timer = findViewById(R.id.chronometer)
        originalTime = SystemClock.elapsedRealtime()
        timer.base = originalTime + (studyLength * MINSTOMILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    private fun getElapsedTime(): Int =
        ((SystemClock.elapsedRealtime() - originalTime) / 1000).toInt()

    private fun updateTimeTracker() {
        timeTracker.study_time += getElapsedTime()
    }

    override fun onUserLeaveHint() {
        if (!validLeave) {
            super.onUserLeaveHint()
            timer.stop()
            updateTimeTracker()

            startActivity(Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "leaveApp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS).toInt(), getElapsedTime())))
            })

            finish()
        }
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
        } else if (elapsedMillis < -30 * MINSTOMILLIS) {
            timer.stop()
            val i = Intent(this, FailActivity::class.java)
            i.putExtra("reason", "studyTimeout")
            i.putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS).toInt(), getElapsedTime())))
            startActivity(i)
            finish()
        } else if (elapsedMillis < -20 * MINSTOMILLIS) {
            setWarningView()
        }
    }

    private fun setBreakView() {
        findViewById<TextView>(R.id.studyTitleText).text = resources.getString(R.string.break_title)
        findViewById<TextView>(R.id.warningView).visibility = View.INVISIBLE
        findViewById<Button>(R.id.endSessionButton).text =
            resources.getString(R.string.break_button)
    }

    private fun setTimerView() {
        findViewById<TextView>(R.id.studyTitleText).text =
            resources.getString(R.string.session_title)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
        findViewById<Button>(R.id.endSessionButton).text =
            resources.getString(R.string.end_session_button)
    }

    private fun setWarningView() {
        findViewById<TextView>(R.id.warningView).text = resources.getString(R.string.warning_title2)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
    }

    fun goToEndSession(_view: View) {
        validLeave = true
        timer.stop()
        updateTimeTracker()

        startActivity(if (inSession) {
            Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "giveUp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS).toInt(), getElapsedTime())))
            }
        } else {
            Intent(this, BreakActivity::class.java).apply {
                putExtra("breakLength", breakLength)
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS).toInt(), getElapsedTime())))
            }
        })
        finish()
    }
}