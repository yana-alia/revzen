package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
    }

    fun goToMenu(view: View) {
        finish()
    }
}