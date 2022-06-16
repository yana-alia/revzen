package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.TextView

class BreakActivity : AppCompatActivity(), Chronometer.OnChronometerTickListener {
    private lateinit var timer: Chronometer
    private var breakLength = 5.0 //default values, will be overwritten in onCreate
    private val MINSTOMILLIS = 60000
    private var studyList = ArrayList<Pair<Int,Long>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_break)

        val extras = getIntent().extras
        if(extras != null) {
            breakLength = extras.get("breakLength") as Double
            studyList = extras.get("studyList") as ArrayList<Pair<Int,Long>>
        }

        timer = findViewById(R.id.breakTimer)
        timer.base = SystemClock.elapsedRealtime() + (breakLength * MINSTOMILLIS).toLong()
        timer.onChronometerTickListener = this
        timer.start()
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun keepStudying(_view : View) {
        val i = Intent(this, SetupActivity::class.java)
        i.putExtra("studyList", studyList)
        startActivity(i)
        finish()
    }

    fun endSession(_view : View) {
        val i = Intent(this, SummaryActivity::class.java)
        i.putExtra("studyList", studyList)
        startActivity(i)
        finish()
    }

    override fun onChronometerTick(chronometer: Chronometer) {
        val elapsedMillis = chronometer.base - SystemClock.elapsedRealtime()
        if (elapsedMillis == 0L){
            chronometer.base -= (1000)
        } else if (elapsedMillis < -5 * MINSTOMILLIS) {
            val i = Intent(this, FailActivity::class.java)
            i.putExtra("reason", "breakTimeout")
            startActivity(i)
            finish()
        } else if (elapsedMillis < 0) {
            findViewById<TextView>(R.id.breakWarning).visibility = View.VISIBLE
        }
    }
}