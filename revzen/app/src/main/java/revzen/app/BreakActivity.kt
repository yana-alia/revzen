package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.TextView
import revzen.app.api.ApiHandler

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var breakLength = 5.0
    private val MINSTOMILLIS = 60000
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var originalTime = 0L
    private var studyList = ArrayList<SessionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        //todo refactor get extra
        val extras = intent.extras
        if(extras != null) {
            breakLength = extras.getDouble("breakLength")
        }


        timer = findViewById(R.id.breakTimer)
        originalTime = SystemClock.elapsedRealtime()
        timer.base = originalTime + (breakLength * MINSTOMILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    private fun getElapsedTime() : Int = ((SystemClock.elapsedRealtime() - originalTime) / 1000).toInt()

    private fun updateTimeTracker() {
        timeTracker.break_time += getElapsedTime()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun keepStudying(_view : View) {
        timer.stop()
        updateTimeTracker()

        // Currently do not care about failures to push data
        apiHandler.logSession(timeTracker.planned_study_time, timeTracker.planned_break_time, timeTracker.study_time, timeTracker.break_time, {}, { _ ->})

        startActivity(Intent(this, SetupActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("studyList", studyList)
        })
        finish()
    }

    fun endSession(_view : View) {

        apiHandler.stopLiveRevision({}, {_ -> })

        timer.stop()
        updateTimeTracker()

        // Currently do not care about failures to push data
        apiHandler.logSession(timeTracker.planned_study_time, timeTracker.planned_break_time, timeTracker.study_time, timeTracker.break_time, {}, { _ ->})

        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("studyList", studyList)
        })
        finish()
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L){
            chronometer.base -= (1000)
        } else if (elapsedMillis < -5 * MINSTOMILLIS) {
            startActivity(Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "giveUp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                putExtra("studyList", studyList)
            })
            finish()
        } else if (elapsedMillis < 0) {
            findViewById<TextView>(R.id.breakWarning).visibility = View.VISIBLE
        }
    }
}