package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView

class FailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fail)

        //API call to set health variable
        val health = 2
        val healthBar = findViewById<ImageView>(R.id.imageView2)
        val image = when (health){
            3 -> R.drawable.heart3
            2 -> R.drawable.heart2
            1 -> R.drawable.heart1
            0 -> R.drawable.heart0
            else -> R.drawable.heart3
        }
        healthBar.setImageResource(image);
    }

    override fun onBackPressed() {
        //disable back button by preventing call to super.onBackPressed()
        return
    }

    fun goToSummary(_view: View) {
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }
}