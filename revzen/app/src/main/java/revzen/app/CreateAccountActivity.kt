package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import revzen.app.api.ApiError
import revzen.app.api.createUser

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var loading: ProgressBar
    private lateinit var subjectID: EditText
    private lateinit var username: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        subjectID = findViewById(R.id.create_subject_id)
        username = findViewById(R.id.create_username)
        loading = findViewById(R.id.create_account_loading)
    }

    fun attempt_create_account(_view: View) {
        loading.visibility = View.VISIBLE
        val subjectID = Integer.parseInt(subjectID.text.toString()).toLong()
        val username = username.text.toString()
        createUser(
            subjectID,
            username,
            this::successful_account_create,
            this::account_create_failure
        )
    }

    fun successful_account_create() {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Success")
            setMessage("Account created successfully")
            setPositiveButton("Ok") { _, _ -> }
            create()
            show()
        }
    }

    fun account_create_failure(error: ApiError) {
        loading.visibility = View.INVISIBLE
        subjectID.text.clear()
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.USER_ALREADY_EXISTS -> R.string.create_account_user_exists
                    ApiError.WRONG_VERSION -> R.string.login_failure_outdated_api
                    else -> R.string.login_failure_unspecified_api_error
                }
            )
            setPositiveButton("Ok") { _, _ -> finish()}
            create()
            show()
        }
    }
}