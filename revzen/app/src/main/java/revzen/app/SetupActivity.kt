package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner

class SetupActivity : AppCompatActivity() {
    private var times = listOf("30 MINUTES", "1 HOUR")
    private lateinit var studySpinner: Spinner
    private lateinit var breakSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, times)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        studySpinner = findViewById(R.id.studyLengthSpinner)
        breakSpinner = findViewById(R.id.breakLengthSpinner)
        studySpinner.adapter = adapter
        breakSpinner.adapter = adapter
    }

    fun goToStudySession(view: View) {
        startActivity(Intent(this, StudyActivity::class.java))
    }
}