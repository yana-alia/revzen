package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class SummaryActivity : AppCompatActivity() {
    private var studyList = ArrayList<Pair<Int,Int>>()
    private val MILLISTOMINS = 60000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        var xp = 0
        val extras = getIntent().extras
        if(extras != null) {
            //studyList = extras.get("studyList") as ArrayList<Pair<Int,Int>>
            //testing
            studyList.add(Pair(120*60000, 120*60000)) //+120 xp
            studyList.add(Pair(30*60000, 60*60000)) //-15 xp

            for (pair in studyList) {
                val setTime = pair.first / MILLISTOMINS
                val actualTime = pair.second / MILLISTOMINS
                val maxXp = setTime

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
            }
            if (xp < 0) {
                xp = 0
            }
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