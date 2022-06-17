package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.LiveRevisionResponse

class LiveRevisionActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var studyList: ListView
    private lateinit var adapter: LiveRevisionAdapter
    private val handler = Handler()

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            pollLiveRevisers()
            handler.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_revision)
        apiHandler = intent.extras?.getParcelable("handler")!!
        studyList = findViewById(R.id.live_revisers_list)
        pollLiveRevisers()
    }

    private fun pollLiveRevisers() {
        apiHandler.getLiveRevision(this::successGetRevisers, this::getRevisersFailure)
    }

    private fun successGetRevisers(revisers: Array<LiveRevisionResponse>) {
        adapter = LiveRevisionAdapter(this, revisers)
        studyList.adapter = adapter
    }

    private fun getRevisersFailure(error: ApiError) {
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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTask)
    }
}