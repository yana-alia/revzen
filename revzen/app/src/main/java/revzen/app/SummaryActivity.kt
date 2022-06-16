package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class SummaryActivity : AppCompatActivity() {
    private var studyList = ArrayList<Pair<Int,Long>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val extras = getIntent().extras
        if(extras != null) {
            studyList = extras.get("studyList") as ArrayList<Pair<Int,Long>>
        }

        val xp = 69
        //todo calculate xp from session
        val xpStr = "+" + xp.toString()
        findViewById<TextView>(R.id.summaryXP).text = xpStr

        //api post request to give database xp
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun goToMenu(_view: View) {
        finish()
    }
}