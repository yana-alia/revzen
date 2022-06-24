package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetResponse

class MenuActivity : AppCompatActivity() {
    private lateinit var usernameText: TextView
    private lateinit var friendcodeText: TextView
    private lateinit var apiHandler: ApiHandler
    private lateinit var petImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)
        // Assume it has been passed to the intent
        apiHandler = intent.extras?.getParcelable("handler")!!
        usernameText = findViewById(R.id.menu_username)
        friendcodeText = findViewById(R.id.menu_friendcode)
        usernameText.text = apiHandler.username
        friendcodeText.text = apiHandler.friendcode.toString()

        apiHandler.getPetInfo(this::successGet, this::failGet)
    }

    fun goToSessionSetup(_view: View) {
        startActivity(Intent(this, SetupActivity::class.java).apply {
            putExtra(
                "handler",
                apiHandler
            )
            putExtra("studyList", ArrayList<SessionData>())
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

    fun goToFollowScreen(_view: View) {
        startActivity(Intent(this, FollowActivity::class.java).apply {putExtra(
                "handler",
                apiHandler
            )
        })
    }

    fun goToPetSelect(_view: View) {
        startActivity(Intent(this, PetSelectActivity::class.java).apply {
            putExtra(
                "handler",
                apiHandler
            )
        })
    }

    private fun successGet(info: PetResponse) {
        findViewById<ImageView>(R.id.petImage).setImageResource(info.selectedPet.logoImage)
        findViewById<ImageView>(R.id.petImage).visibility = View.VISIBLE
    }

    private fun failGet(error: ApiError) {
        findViewById<ImageView>(R.id.petImage).visibility = View.INVISIBLE
    }
}