package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class SetupActivity : AppCompatActivity() {
    private val studyLengths = listOf(30, 45, 60, 90, 120)
    private val breakLengths = listOf(5, 10, 15, 20, 25, 30)
    // RESULT OF API REQUEST STORED IN THESE VARIABLES //
    private val recentStudyTime = 90
    private val recentBreakTime = 10

    private lateinit var studySpinner: Spinner
    private lateinit var breakSpinner: Spinner
    private lateinit var studyButton: Button
    private lateinit var breakButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        val studyStrings = studyLengths.map { t -> timeFormat(t) }
        val breakStrings = breakLengths.map { t -> timeFormat(t) }
        val studyButtonText = "STUDY: " + timeFormat(recentStudyTime)
        val breakButtonText = "BREAK: " + timeFormat(recentBreakTime)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        studySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, studyStrings)
        breakSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, breakStrings)

        studyButton = findViewById(R.id.setupStudyButton)
        breakButton = findViewById(R.id.setupBreakButton)
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
            if (mins > 0) {
                "$hours HOURS $mins MINS"
            } else {
                "$hours HOURS"
            }
        }
    }
}