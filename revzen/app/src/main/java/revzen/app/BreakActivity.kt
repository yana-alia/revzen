package revzen.app

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetStatus
import revzen.app.api.SessionData

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private lateinit var breakWarning: TextView
    private lateinit var petImage: ImageView

    private lateinit var loading: ProgressBar

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

        apiHandler.getCurrentPet(this::successGet, this::failGet)

        //service that builds a notification when break timer is up
        startService(Intent(this, BgBreakService::class.java).apply {
            putExtra("breakLength", timeTracker.planned_break_time)
        })

        timer = findViewById(R.id.breakTimer)
        breakWarning = findViewById(R.id.breakWarning)
        petImage = findViewById(R.id.petView)

        loading = findViewById(R.id.logSessionLoading)

        originalTime = SystemClock.elapsedRealtime()
        timer.base = originalTime + (timeTracker.planned_break_time * SECS_TO_MILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    private fun getElapsedTime(): Int =
        ((SystemClock.elapsedRealtime() - originalTime) / 1000).toInt()

    private fun updateTimeTracker() {
        timeTracker.break_time += getElapsedTime()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun keepStudying(_view: View) {
        timer.stop()
        updateTimeTracker()

        stopService(Intent(this, BgBreakService::class.java))
        apiHandler.logSession(timeTracker, this::logSessionSuccess, this::failureLogSession)
    }

    fun endSessionStart(_view: View) {
        timer.stop()
        updateTimeTracker()
        apiHandler.logSession(timeTracker, {}, this::failureLogSession)
        stopService(Intent(this, BgBreakService::class.java))

        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            studyList.add(timeTracker)
            putExtra("studyList", studyList)
        })
        finish()

    }

    private fun logSessionSuccess() {
        loading.visibility = View.INVISIBLE
        startActivity(Intent(this, SetupActivity::class.java).apply {
            putExtra("handler", apiHandler)
            studyList.add(timeTracker)
            putExtra("studyList", studyList)
        })
        finish()
    }

    private fun failureLogSession(error: ApiError) {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.NO_SUCH_USER -> R.string.login_failure_no_such_user
                    ApiError.WRONG_VERSION -> R.string.login_failure_outdated_api
                    else -> R.string.login_failure_unspecified_api_error
                }
            )
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L) {
            chronometer.base -= (1000)
        } else if (elapsedMillis < BREAK_FAIL_THRESHOLD) {
            startActivity(Intent(this, FailActivity::class.java).apply {
                putExtra("reason", "giveUp")
                putExtra("handler", apiHandler)
                putExtra("timeTracker", timeTracker)
                studyList.add(timeTracker)
                putExtra("studyList", studyList)
            })
            finish()
        } else if (elapsedMillis < 0) {
            breakWarning.visibility = View.VISIBLE
        }
    }

    private fun successGet(info: PetStatus) {
        petImage.setImageResource(info.petType.breakImage)
        petImage.visibility = View.VISIBLE
    }

    //todo improve
    private fun failGet(_error: ApiError) {
        apiHandler.stopLiveRevision({}, { })
        finish()
    }
}