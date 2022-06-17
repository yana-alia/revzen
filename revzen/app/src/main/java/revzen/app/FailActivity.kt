package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import revzen.app.api.ApiHandler

class FailActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var timeTracker: SessionData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fail)
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun goToSummary(_view: View) {
        apiHandler.logSession(timeTracker.planned_study_time, timeTracker.planned_break_time, timeTracker.study_time, timeTracker.break_time, {}, { _ ->})
        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
        })
        finish()
    }
}