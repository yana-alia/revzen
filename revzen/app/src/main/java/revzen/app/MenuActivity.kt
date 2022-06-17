package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import revzen.app.api.ApiHandler

class MenuActivity : AppCompatActivity() {
    private lateinit var usernameText: TextView
    private lateinit var friendcodeText: TextView
    private lateinit var apiHandler: ApiHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)
        // Assume it has been passed to the intent
        apiHandler = intent.extras?.getParcelable("handler")!!
        usernameText = findViewById(R.id.menu_username)
        friendcodeText = findViewById(R.id.menu_friendcode)
        usernameText.text = apiHandler.username
        friendcodeText.text = apiHandler.friendcode.toString()
    }

    fun goToSessionSetup(_view: View) {
        startActivity(Intent(this, SetupActivity::class.java).apply {
            putExtra(
                "handler",
                apiHandler
            )
        })
    }

    fun goToRevisionHistory(_view: View) {
        startActivity(Intent(this, StudyHistoryActivity::class.java).apply {
            putExtra(
                "handler",
                apiHandler
            )
        })
    }

    fun goToLiveRevision(_view: View) {
        startActivity(Intent(this, LiveRevisionActivity::class.java).apply {
            putExtra(
                "handler",
                apiHandler
            )
        })
    }
}