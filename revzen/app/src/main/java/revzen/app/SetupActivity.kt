package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner

class SetupActivity : AppCompatActivity() {
    private var studyLengths = listOf(30, 45, 60, 90, 120)
    private var breakLengths = listOf(5, 10, 15, 20, 25, 30)
    private lateinit var studySpinner: Spinner
    private lateinit var breakSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        val studyStrings = studyLengths.map { t -> timeFormat(t) }
        val breakStrings = breakLengths.map { t -> timeFormat(t) }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        studySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, studyStrings)
        breakSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, breakStrings)
    }

    //do not override onBackPressed() since this page should allow the back
    // button to return to home menu

    fun goToStudySession(_view: View) {
        val i = Intent(this, StudyActivity::class.java)
        i.putExtra("studyLength", studyLengths[studySpinner.selectedItemId.toInt()].toDouble())
        i.putExtra("breakLength", breakLengths[breakSpinner.selectedItemId.toInt()].toDouble())
        startActivity(i)
        finish()
    }

    private fun timeFormat(time: Int): String {
        val hours = time / 60
        val mins = time % 60
        return if (hours < 1) {
            "$mins MINUTES"
        } else {
            if (mins > 0) {
                "$hours HOURS $mins MINUTES"
            } else {
                "$hours HOURS"
            }
        }
    }
}