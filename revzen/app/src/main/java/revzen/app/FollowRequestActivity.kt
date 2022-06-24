package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.UserDetails
import java.lang.NumberFormatException

class FollowRequestActivity : AppCompatActivity() {
    private lateinit var apiHandler: ApiHandler
    lateinit var loading: ProgressBar
    lateinit var friendcodeText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_request)
        apiHandler = intent.extras?.getParcelable("handler")!!
        loading = findViewById(R.id.send_request_loading)
        friendcodeText = findViewById(R.id.enter_friend_code)
    }

    fun sendRequest(_view: View) {
        try {
            val friendcode = Integer.parseInt(friendcodeText.text.toString())
            loading.visibility = View.VISIBLE
            apiHandler.getUser(friendcode, this::userConfirmed, this::userFailedConfirmation)
        } catch (e: NumberFormatException) {
            AlertDialog.Builder(this).apply {
                setTitle("Error")
                setMessage("Friendcode needs to be a number")
                setPositiveButton("Ok") { _, _ -> }
                create()
                show()
            }
        }
    }

    fun userConfirmed(user: UserDetails) {
        loading.visibility = View.INVISIBLE
        if (user.friendcode == apiHandler.friendcode) {
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
                    apiHandler.manageFollower(user.friendcode, ApiHandler.SocialAction.REQUEST, this@FollowRequestActivity::requestSent, this@FollowRequestActivity::requestFailed)}
                create()
                show()
            }
        }
    }

    fun userFailedConfirmation(error: ApiError) {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(when (error) {
                ApiError.FRIENDCODE_NOT_PRESENT -> "No user has that friendcode"
                ApiError.WRONG_VERSION -> "Application version incorrect"
                else -> "An error occured"
            })
            setPositiveButton("Ok") { _, _ -> }
            create()
            show()
        }
    }

    fun requestSent() {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Success")
            setMessage("Request sent successfully")
            setPositiveButton("Ok") { _, _ -> finish()}
            create()
            show()
        }
    }

    fun requestFailed(error: ApiError) {
        loading.visibility = View.INVISIBLE
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(when (error) {
                ApiError.FRIENDCODE_NOT_PRESENT -> "No user has that friendcode"
                ApiError.WRONG_VERSION -> "Application version incorrect"
                else -> "An error occured"
            })
            setPositiveButton("Ok") { _, _ -> finish()}
            create()
            show()
        }
    }
}