package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.HistoryResponse

class StudyHistoryActivity : AppCompatActivity() {
    private lateinit var loading: ProgressBar
    private lateinit var studyList: ListView
    private lateinit var apiHandler: ApiHandler
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_history)
        loading = findViewById(R.id.history_loading)
        studyList = findViewById(R.id.revision_history_list)
        apiHandler = intent.extras?.getParcelable("handler")!!

        apiHandler.get_history(this::successGotHistory, this::historyFailure)
    }

    private fun historyFailure(error: ApiError) {
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

    private fun successGotHistory(history: Array<HistoryResponse>) {

        loading.visibility = View.INVISIBLE
        adapter = HistoryAdapter(this, history)
        studyList.adapter = adapter
    }
}