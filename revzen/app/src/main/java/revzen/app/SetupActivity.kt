package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.HistoryResponse
import revzen.app.api.SessionData

class SetupActivity : AppCompatActivity() {
    // Lengths in seconds
    private val studyLengths = listOf(5, 10, 30, 60, 300, 600, 900, 1800, 3600, 5400, 7200)
    private val breakLengths = listOf(5, 10, 30, 60, 300, 600, 900, 1200, 1500, 1800)

    private var studyList = ArrayList<SessionData>()

    private lateinit var apiHandler: ApiHandler

    private var recentStudy = 0
    private var recentBreak = 0

    private lateinit var studySpinner: Spinner
    private lateinit var breakSpinner: Spinner
    private lateinit var recentView: TextView
    private lateinit var studyButton: Button
    private lateinit var breakButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val studyStrings = studyLengths.map { timeFormat(it) }
        val breakStrings = breakLengths.map { timeFormat(it) }

        studyList = intent.extras?.getParcelableArrayList("studyList")!!

        // Get API handler
        apiHandler = intent.extras?.getParcelable("handler")!!

        apiHandler.getHistory(this::successGotHistory, this::historyFailure)

        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        recentView = findViewById(R.id.recentTitleTextView)
        studyButton = findViewById(R.id.setupStudyButton)
        breakButton = findViewById(R.id.setupBreakButton)
        studySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, studyStrings)
        breakSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, breakStrings)
    }

    fun startSessionWithSpinners(_view: View) {
        startSession(
            studyLengths[studySpinner.selectedItemId.toInt()],
            breakLengths[breakSpinner.selectedItemId.toInt()]
        )
    }

    fun startSessionWithRecent(_view: View) {
        startSession(recentStudy, recentBreak)
    }

    private fun startSession(studyTime: Int, breakTime: Int) {
        apiHandler.startLiveRevision({}, { })

        startActivity(Intent(this, StudyActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("timeTracker", SessionData(0, 0, studyTime, breakTime))
            putExtra("breakLength", breakTime)
            putExtra("studyLength", studyTime)
            putExtra("studyList", studyList)
        })
        finish()
    }

    private fun successGotHistory(history: Array<HistoryResponse>) {
        if (history.isNotEmpty()) {
            recentStudy = history[0].planned_study_time
            recentBreak = history[0].planned_break_time

            val studyText = "Study: ${timeFormat(recentStudy)}"
            val breakText = "Break: ${timeFormat(recentBreak)}"
            studyButton.text = studyText
            breakButton.text = breakText

            studyButton.visibility = View.VISIBLE
            breakButton.visibility = View.VISIBLE
            recentView.visibility = View.VISIBLE
        }
    }

    private fun historyFailure(error: ApiError) {
        AlertDialog.Builder(this).apply {
            setTitle("Error Retrieving Study History")
            setMessage(
                when (error) {
                    ApiError.NO_SUCH_USER -> R.string.login_failure_no_such_user
                    ApiError.WRONG_VERSION -> R.string.login_failure_outdated_api
                    else -> R.string.login_failure_unspecified_api_error
                }
            )
            setPositiveButton("Ok") { _, _ -> }
            create()
            show()
        }
    }
}