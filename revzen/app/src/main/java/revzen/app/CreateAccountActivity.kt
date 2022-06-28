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
import revzen.app.api.createUser
import revzen.app.api.loginUser
import java.lang.NumberFormatException

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var loading: ProgressBar
    private lateinit var userCode: EditText
    private lateinit var username: EditText

    private var subjectID = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        userCode = findViewById(R.id.createUserCodeEditText)
        username = findViewById(R.id.createUsernameEditText)
        loading = findViewById(R.id.createAccountProgressBar)
    }

    fun attemptAccountCreation(_view: View) {
        try {
            loading.visibility = View.VISIBLE
            subjectID = Integer.parseInt(userCode.text.toString()).toLong()
            val username = username.text.toString()
            createUser(
                subjectID,
                username,
                this::successfulAccountCreate,
                this::failedAccountCreate
            )
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

    private fun successfulAccountCreate() {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Success")
            setMessage("Account created successfully")
            setPositiveButton("Ok") { _, _ ->
                loading.visibility = View.VISIBLE
                loginUser(
                    subjectID,
                    this@CreateAccountActivity::successfulLogin,
                    this@CreateAccountActivity::loginFailure
                )
            }
            create()
            show()
        }

    }

    private fun loginFailure(error: ApiError) {
        loading.visibility = View.INVISIBLE
        userCode.text.clear()
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

    private fun successfulLogin(handler: ApiHandler) {
        loading.visibility = View.INVISIBLE
        startActivity(Intent(this, MenuActivity::class.java).apply { putExtra("handler", handler) })
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