package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.lang.Integer.max

class SummaryActivity : AppCompatActivity() {
    private var studyList = ArrayList<SessionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!

        var xp = 0
        for (session in studyList) {
            val setTime = session.planned_study_time
            val actualTime = session.study_time
            val maxXp = setTime / 10

            if(actualTime < setTime/2) {
                xp += 0
            } else if (actualTime < setTime) {
                val m = maxXp / (setTime /2)
                xp += m*(actualTime - (setTime/2))
                //y - y1 = m*(x - x1)
                //(x1,y1) = (setTime/2,0) is a point on the line

            } else if (actualTime < setTime + 5) {
                xp += maxXp
                //todo add chance of getting new pet
            } else if (actualTime < setTime + 30) {
                val m = -maxXp/2   //maxXp/2 - maxXp
                xp += m*(actualTime - (setTime+5)) + maxXp
                //y - y1 = m*(x - x1)
                //(x1,y1) = (setTime+5,maxXp) is a point on the line

            } else {
                xp -= maxXp / 2
            }

            xp = max(xp, 0)//limit to at least 0xp
        }

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