package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun goToSessionSetup(_view: View) {
        val i = Intent(this, SetupActivity::class.java)
        i.putExtra("studyList", ArrayList<Pair<Double,Double>>())
        startActivity(i)
        startActivity(Intent(this, SetupActivity::class.java))
    }
}