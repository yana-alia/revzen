package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView

class SetupActivity : AppCompatActivity() {
    private val studyLengths = listOf(30, 45, 60, 90, 120)
    private val breakLengths = listOf(5, 10, 15, 20, 25, 30)

    private var recentStudyTime = 150
    private var recentBreakTime = 10

    private lateinit var studySpinner: Spinner
    private lateinit var breakSpinner: Spinner
    private lateinit var recentView: TextView
    private lateinit var studyButton: Button
    private lateinit var breakButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        val studyStrings = studyLengths.map { t -> timeFormat(t) }
        val breakStrings = breakLengths.map { t -> timeFormat(t) }
        val hasRecentHistory = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        recentView = findViewById(R.id.recentView)
        studyButton = findViewById(R.id.setupStudyButton)
        breakButton = findViewById(R.id.setupBreakButton)
        studySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, studyStrings)
        breakSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, breakStrings)

        if (hasRecentHistory) {
            //recentStudyTime = API_GET
            //recentBreakTime = API_GET
            studyButton.visibility = View.VISIBLE
            breakButton.visibility = View.VISIBLE
            recentView.visibility = View.VISIBLE
        } else {
            studyButton.visibility = View.GONE
            breakButton.visibility = View.GONE
            recentView.visibility = View.INVISIBLE
        }

        val studyButtonText = "STUDY:    " + timeFormat(recentStudyTime)
        val breakButtonText = "BREAK:  " + timeFormat(recentBreakTime)
        studyButton.text = studyButtonText
        breakButton.text = breakButtonText
    }

    fun startSessionWithSpinners(_view: View) {
        startSession(
            studyLengths[studySpinner.selectedItemId.toInt()],
            breakLengths[breakSpinner.selectedItemId.toInt()]
        )
    }

    fun startSessionWithRecent(_view: View) {
        startSession(recentStudyTime, recentBreakTime)
    }

    private fun startSession(studyTime: Int, breakTime: Int) {
        val i = Intent(this, StudyActivity::class.java)
        i.putExtra("studyLength", studyTime.toDouble())
        i.putExtra("breakLength", breakTime.toDouble())
        startActivity(i)
        finish()
    }

    private fun timeFormat(time: Int): String {
        val hours = time / 60
        val mins = time % 60
        return if (hours < 1) {
            "$mins MINS"
        } else {
            var hourRep = "HOURS"
            if (hours == 1) {
                hourRep = "HOUR"
            }
            if (mins > 0) {
                "$hours $hourRep $mins MINS"
            } else {
                "$hours $hourRep"
            }
        }
    }
}