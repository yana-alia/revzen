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
    private lateinit var userCode: EditText
    private lateinit var username: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        userCode = findViewById(R.id.createUserCodeEditText)
        username = findViewById(R.id.createUsernameEditText)
        loading = findViewById(R.id.createAccountProgressBar)
    }

    fun attemptAccountCreation(_view: View) {
        loading.visibility = View.VISIBLE
        val subjectID = Integer.parseInt(userCode.text.toString()).toLong()
        val username = username.text.toString()
        createUser(
            subjectID,
            username,
            this::successfulAccountCreate,
            this::failedAccountCreate
        )
    }

    private fun successfulAccountCreate() {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Success")
            setMessage("Account created successfully")
            setPositiveButton("Ok") { _, _ -> }
            create()
            show()
        }
    }

    private fun failedAccountCreate(error: ApiError) {
        loading.visibility = View.INVISIBLE
        userCode.text.clear()
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.USER_ALREADY_EXISTS -> R.string.user_exists_text
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