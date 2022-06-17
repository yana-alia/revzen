package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import revzen.app.api.ApiHandler

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToLogin(_view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun goToCreateAccount(_view: View) {
        startActivity(Intent(this, CreateAccountActivity::class.java))
    }
}