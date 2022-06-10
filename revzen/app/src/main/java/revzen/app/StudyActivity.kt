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
    private var minutes = 1
    private var inSession = true
    private var validLeave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        timer = findViewById(R.id.chronometer)
        timer.base = SystemClock.elapsedRealtime() + (minutes * 60000)
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
        startActivity(Intent(this, FailActivity::class.java))
        finish()
    }

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
        apiEndRevision()
        validLeave = true
        //leaving via button is considered valid. Leaving by home button is invalid

        if (inSession) {
            startActivity(Intent(this, FailActivity::class.java))
        } else {
            startActivity(Intent(this, BreakActivity::class.java))
        }
        finish()
    }
}