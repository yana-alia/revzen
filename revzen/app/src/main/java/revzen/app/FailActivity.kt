package revzen.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.PetStatus
import revzen.app.api.SessionData

class FailActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler

    private lateinit var petImage: ImageView
    private lateinit var failLoadingImage: ProgressBar

    private lateinit var timeTracker: SessionData
    private lateinit var studyList: ArrayList<SessionData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fail)

        studyList = intent.extras?.getParcelableArrayList("studyList")!!
        apiHandler = intent.extras?.getParcelable("handler")!!
        timeTracker = intent.extras?.getParcelable("timeTracker")!!

        failLoadingImage = findViewById(R.id.imageLoadProgressBar)
        petImage = findViewById(R.id.failPetImageView)

        apiHandler.logSession(timeTracker, {}, {})
        apiHandler.getCurrentPet(this::successGet, this::failGet)
    }

    override fun onBackPressed() {}

    fun goToSummary(_view: View) {
        startActivity(Intent(this, SummaryActivity::class.java).apply {
            putExtra("handler", apiHandler)
            putExtra("studyList", studyList)
        })
        finish()
    }

    private fun successGet(info: PetStatus) {
        failLoadingImage.visibility = View.INVISIBLE
        val mainPet = info.petType
        petImage.setImageResource(mainPet.failImage)
        petImage.visibility = View.VISIBLE
    }

    private fun failGet(error: ApiError) {
        failLoadingImage.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.NO_SUCH_USER -> R.string.login_failure_no_such_user
                    ApiError.WRONG_VERSION -> R.string.login_failure_outdated_api
                    else -> R.string.login_failure_unspecified_api_error
                }
            )
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }
}