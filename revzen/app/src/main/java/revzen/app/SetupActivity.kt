package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner

class SetupActivity : AppCompatActivity() {
    private var times = listOf("30 MINUTES", "45 MINUTES", "1 HOUR", "1 HOUR 30 MINUTES", "2 HOURS")
    private lateinit var studySpinner: Spinner
    private lateinit var breakSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        //todo make it so break length has different options
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, times)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        studySpinner.adapter = adapter
        breakSpinner.adapter = adapter
    }

    //do not override onBackPressed() since this page should allow the back
    // button to return to home menu

    fun goToStudySession(_view: View) {
        val i = Intent(this, StudyActivity::class.java)
        i.putExtra("studyLength", 2)
        i.putExtra("breakLength", 1)
        startActivity(i)
        finish()
    }
}