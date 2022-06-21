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

class SetupActivity : AppCompatActivity() {
    private val studyLengths = listOf(1, 30, 45, 60, 90, 120)
    private val breakLengths = listOf(1, 5, 10, 15, 20, 25, 30)

    private var studyList = ArrayList<Pair<Int,Int>>()

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

        val studyStrings = studyLengths.map { t -> timeFormat(t) }
        val breakStrings = breakLengths.map { t -> timeFormat(t) }

        //todo refactor getting studyList
        val extras = getIntent().extras
        if(extras != null) {
            //studyList = extras.get("studyList") as ArrayList<Pair<Int,Int>>
        }

        // Get the api handler
        apiHandler = intent.extras?.getParcelable("handler")!!

        apiHandler.getHistory(this::successGotHistory, this::historyFailure)

        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        recentView = findViewById(R.id.recentView)
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

        apiHandler.startLiveRevision({}, {_ -> })

        startActivity(Intent(this, StudyActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("timeTracker", SessionData(0, 0, studyTime * 60, breakTime * 60))
            putExtra("breakLength", breakTime.toDouble())
            putExtra("studyLength", studyTime.toDouble())
            putExtra("studyList", studyList)
        })
        finish()
    }

    private fun timeFormat(time: Int): String {
        val hours = time / 60
        val mins = time % 60
        return if (hours < 1) {
            "$mins minutes"
        } else {
            var hourRep = "hours"
            if (hours == 1) {
                hourRep = "hour"
            }
            if (mins > 0) {
                "$hours $hourRep $mins minutes"
            } else {
                "$hours $hourRep"
            }
        }
    }

    private fun successGotHistory(history: Array<HistoryResponse>) {

        if (history.isNotEmpty()) {
            recentStudy = history[0].planned_study_time / 60
            recentBreak = history[0].planned_break_time / 60

            studyButton.text = "Study: ${timeFormat(recentStudy)}"
            breakButton.text = "Break: ${timeFormat(recentBreak)}"

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