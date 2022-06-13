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
        val studyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, studyLengths)
        val breakAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, breakLengths)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        studySpinner.adapter = studyAdapter
        breakSpinner.adapter = breakAdapter
    }

    //do not override onBackPressed() since this page should allow the back
    // button to return to home menu

    fun goToStudySession(_view: View) {
        val i = Intent(this, StudyActivity::class.java)
        i.putExtra("studyLength",  studySpinner.selectedItem.toString().toDouble())
        i.putExtra("breakLength", breakSpinner.selectedItem.toString().toDouble())
        startActivity(i)
        finish()
    }
}