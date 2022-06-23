package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.lang.Integer.max
import kotlin.math.roundToInt

class SummaryActivity : AppCompatActivity() {
    private var studyList = ArrayList<SessionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!

        var xp = 0
        for (session in studyList) {
            val setTime = session.planned_study_time //in seconds
            val actualTime = session.study_time //in seconds
            val maxXp = setTime / 10

            if(actualTime < setTime/2) {
                xp += 0
            } else if (actualTime < setTime) {
                val m = maxXp / (setTime /2)
                xp += m*(actualTime - (setTime/2))
                //y - y1 = m*(x - x1)
                //(x1,y1) = (setTime/2,0) is a point on the line

            } else if (actualTime < setTime + 5*60) {
                xp += maxXp
                //todo add chance of getting new pet
            } else if (actualTime < setTime + 30*60) {
                val m = -maxXp/2   //maxXp/2 - maxXp
                xp += m*(actualTime - (setTime+5*60)) + maxXp
                //y - y1 = m*(x - x1)
                //(x1,y1) = (setTime+5*60,maxXp) is a point on the line

            } else {
                xp -= maxXp / 2
            }

            xp = max(xp, 0)//limit to at least 0xp
        }

        val xpStr = "+" + xp.toString()
        findViewById<TextView>(R.id.summaryXP).text = xpStr

        val totalStudy = studyList.sumOf { sessionData -> sessionData.study_time }
        findViewById<TextView>(R.id.summaryTotalStudy).text = timeFormat(totalStudy)
        val totalBreak = studyList.sumOf { sessionData -> sessionData.break_time }
        findViewById<TextView>(R.id.summaryTotalBreak).text = timeFormat(totalBreak)

        if(totalBreak == 0){
            findViewById<TextView>(R.id.summaryRatio).text = "N/A"
        } else {
            val ratio: Double = totalStudy.toDouble() / totalBreak.toDouble()
            val roundRatio: Double = (ratio * 100.0).roundToInt() / 100.0
            findViewById<TextView>(R.id.summaryRatio).text = roundRatio.toString()
        }

        //todo api post request to give database xp
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun goToMenu(_view: View) {
        finish()
    }

    private fun timeFormat(time: Int): String {
        val hours = time / 3600
        val mins = (time % 3600) / 60
        val secs = (time % 3600) % 60

        return if(hours == 0){
            if(mins == 0){
                "$secs seconds"
            } else {
                "$mins minutes $secs seconds"
            }
        } else {
            "$hours hours $mins minutes $secs seconds"
        }
    }
}