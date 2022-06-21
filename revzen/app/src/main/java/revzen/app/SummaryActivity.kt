package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class SummaryActivity : AppCompatActivity() {
    private var studyList = ArrayList<Pair<Int,Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        var xp = 0
        val extras = getIntent().extras
        if(extras != null) {
            //studyList = extras.get("studyList") as ArrayList<Pair<Int,Int>>

            println(studyList)
        }

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