package revzen.app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.UserDetails

class FollowRequestActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    private lateinit var loading: ProgressBar
    private lateinit var friendCodeText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_request)
        apiHandler = intent.extras?.getParcelable("handler")!!
        loading = findViewById(R.id.sendRequestProgressBar)
        friendCodeText = findViewById(R.id.enterFriendCodeEditText)
    }

    fun sendRequest(_view: View) {
        try {
            val friendCode = Integer.parseInt(friendCodeText.text.toString())
            loading.visibility = View.VISIBLE
            apiHandler.getUser(friendCode, this::userConfirmed, this::userFailedConfirmation)
        } catch (e: NumberFormatException) {
            AlertDialog.Builder(this).apply {
                setTitle("Error")
                setMessage("Friend code needs to be a number")
                setPositiveButton("Ok") { _, _ -> }
                create()
                show()
            }
        }
    }

    private fun userConfirmed(user: UserDetails) {
        loading.visibility = View.INVISIBLE
        if (user.friendcode == apiHandler.friendCode) {
            AlertDialog.Builder(this).apply {
                setTitle("Cannot follow yourself")
                setMessage("Please follow someone else!")
                setPositiveButton("Ok") { _, _ -> }
                create()
                show()
            }
        } else {
            AlertDialog.Builder(this).apply {
                setTitle("Request ${user.username}")
                setMessage("Are you sure you would like to friend ${user.username}?")
                setPositiveButton("Ok") { _, _ ->
                    loading.visibility = View.VISIBLE
                    apiHandler.manageFollower(
                        user.friendcode,
                        ApiHandler.SocialAction.REQUEST,
                        this@FollowRequestActivity::requestSent,
                        this@FollowRequestActivity::requestFailed
                    )
                }
                create()
                show()
            }
        }
    }

    private fun userFailedConfirmation(error: ApiError) {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.FRIENDCODE_NOT_PRESENT -> "No user has that friend code"
                    ApiError.WRONG_VERSION -> "Application version incorrect"
                    else -> "An error occurred"
                }
            )
            setPositiveButton("Ok") { _, _ -> }
            create()
            show()
        }
    }

    private fun requestSent() {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Success")
            setMessage("Request sent successfully")
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }

    private fun requestFailed(error: ApiError) {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(
                when (error) {
                    ApiError.FRIENDCODE_NOT_PRESENT -> "No user has that friend code"
                    ApiError.WRONG_VERSION -> "Application version incorrect"
                    else -> "An error occurred"
                }
            )
            setPositiveButton("Ok") { _, _ -> finish() }
            create()
            show()
        }
    }
}