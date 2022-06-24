package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.loginUser
import java.lang.NumberFormatException

class LoginActivity : AppCompatActivity() {
    lateinit var loading: ProgressBar
    lateinit var subjectID: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        subjectID = findViewById(R.id.subject_id)
        loading = findViewById(R.id.login_loading)
    }

    fun attempt_login(_view: View) {
        try {
            val subjectID = Integer.parseInt(subjectID.text.toString()).toLong()
            loading.visibility = View.VISIBLE
            loginUser(subjectID, this::successful_login, this::login_failure)
        } catch (e: NumberFormatException) {
            AlertDialog.Builder(this).apply {
                setTitle("Error")
                setMessage("Subject ID needs to be a number")
                setPositiveButton("Ok") { _, _ -> finish() }
                create()
                show()
            }
        }
    }

    fun successful_login(handler: ApiHandler) {
        loading.visibility = View.INVISIBLE
        startActivity(Intent(this, MenuActivity::class.java).apply { putExtra("handler", handler) })
    }

    fun login_failure(error: ApiError) {
        loading.visibility = View.INVISIBLE
        subjectID.text.clear()
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