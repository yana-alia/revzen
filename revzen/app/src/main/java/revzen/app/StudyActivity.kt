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
    private val client = OkHttpClient()
    private lateinit var timer: Chronometer
    private val userID = abs(Random.nextInt()) % 1000
    private var elapsedMillis = 0L
    private val MINSTOMILLIS = 60000
    private var studyLength = 60.0 //default values, will be overwritten in onCreate
    private var breakLength = 5.0
    private var studyList = ArrayList<Pair<Int,Long>>()
    private var inSession = true
    private var validLeave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        val extras = getIntent().extras
        if(extras != null) {
            studyLength = extras.get("studyLength") as Double
            breakLength = extras.get("breakLength") as Double
            studyList = extras.get("studyList") as ArrayList<Pair<Int,Long>>
        }

        timer = findViewById(R.id.chronometer)
        timer.base = SystemClock.elapsedRealtime() + (studyLength * MINSTOMILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
        apiStartRevision()
    }

    private fun apiStartRevision() {
        val requestBody =
            FormBody.Builder().add("user_id", userID.toString()).add("version", "0")
                .add("rev_time", "3").build()
        val request =
            Request.Builder().url(BuildConfig.API + "api/revise").post(requestBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {call.cancel()}
            override fun onResponse(call: Call, response: Response) {
            }
        })

    }

    private fun apiEndRevision() {
        val requestBody = FormBody.Builder().add("user_id", userID.toString()).add("version", "0").build()
        val request = Request.Builder().url(BuildConfig.API + "api/stop_revise").post(requestBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {}
        })
    }

    override fun onUserLeaveHint() {
//        apiEndRevision()
        if(validLeave){
            return
        }
        super.onUserLeaveHint()
        timer.stop()
        val i = Intent(this, FailActivity::class.java)
        i.putExtra("reason", "leaveApp")
        i.putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS) as Int, elapsedMillis)))
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L){
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
            i.putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS) as Int, elapsedMillis)))
            startActivity(i)
            finish()
        } else if (elapsedMillis < -20 * MINSTOMILLIS) {
            setWarningView()
        }

    }

    private fun setTimerView() {
        findViewById<TextView>(R.id.studyTitleText).text =
            resources.getString(R.string.session_title)
        findViewById<TextView>(R.id.warningView).text = resources.getString(R.string.warning_title1)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
        findViewById<Button>(R.id.endSessionButton).text = resources.getString(R.string.end_session_button)
    }

    private fun setBreakView() {
        findViewById<TextView>(R.id.studyTitleText).text = resources.getString(R.string.break_title)
        findViewById<TextView>(R.id.warningView).visibility = View.INVISIBLE
        findViewById<Button>(R.id.endSessionButton).text = resources.getString(R.string.break_button)
    }

    private fun setWarningView() {
        findViewById<TextView>(R.id.warningView).text = resources.getString(R.string.warning_title2)
        findViewById<TextView>(R.id.warningView).visibility = View.VISIBLE
    }

    fun goToEndSession(_view: View) {
        apiEndRevision()
        validLeave = true
        //leaving via button is considered valid. Leaving by home button is invalid

        if (inSession) {
            val i = Intent(this, FailActivity::class.java)
            i.putExtra("reason", "giveUp")
            i.putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS) as Int, elapsedMillis)))
            startActivity(i)
        } else {
            val i = Intent(this, BreakActivity::class.java)
            i.putExtra("breakLength", breakLength)
            i.putExtra("studyList", studyList.add(Pair((studyLength * MINSTOMILLIS) as Int, elapsedMillis)))
            startActivity(i)
        }
        finish()
    }
}