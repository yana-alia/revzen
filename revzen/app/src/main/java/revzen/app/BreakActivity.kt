package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import revzen.app.api.ApiHandler

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var breakLength = 5
    private val MINSTOMILLIS = 60000
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData
    private var originalTime = 0L
    private val CHANNELID = "BREAK_NOTIFICATION"
    private var studyList = ArrayList<SessionData>()
    private val notificationId = 1
    private var notified = false
    private var builder = NotificationCompat.Builder(this, CHANNELID)
        .setSmallIcon(R.drawable.notif_icon)
        .setContentTitle("Return to your pet!")
        .setContentText("You have to return within 5 minutes or you will break your session!")
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("You have to return within 5 minutes or you will break your session!"))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setVisibility(VISIBILITY_PUBLIC)
        .setAutoCancel(true)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!
        breakLength = intent.extras?.getInt("breakLength")!!


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
            studyList.add(timeTracker)
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
            studyList.add(timeTracker)
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
                studyList.add(timeTracker)
                putExtra("studyList", studyList)
            })
            finish()
        } else if (elapsedMillis < 0) {
            findViewById<TextView>(R.id.breakWarning).visibility = View.VISIBLE
            // reminds user to go back to the app with a notification when break is over
            if (!notified) {
                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(notificationId, builder.build())
                }
                notified = true
            }
        }
    }
}