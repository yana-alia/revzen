package revzen.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import revzen.app.api.ApiError
import revzen.app.api.ApiHandler
import revzen.app.api.UserDetails

class FollowActivity : AppCompatActivity() {
    private val viewModel: SocialViewModel by viewModels()
    private lateinit var apiHandler: ApiHandler
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: FollowPageAdapter

    private val handler = Handler()

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            updateFollowData()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTask)
    }

    private fun updateFollowData() {
        apiHandler.getFollows({ viewModel.updateData(it) }, this::getFollowersFailure)
    }

    private fun getFollowersFailure(error: ApiError) {
        handler.removeCallbacksAndMessages(null)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)

        apiHandler = intent.extras?.getParcelable("handler")!!
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.follow_screen_tabs)
        pageAdapter = FollowPageAdapter(this)
        viewPager.adapter = pageAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)?.select()
            }
        })

        handler.post(updateTask)
    }

    private fun manageFollow(
        friendCode: Int,
        requestType: ApiHandler.SocialAction,
        successMessage: String
    ) {
        apiHandler.manageFollower(friendCode, requestType, {
            AlertDialog.Builder(this).apply {
                setTitle("Success")
                setMessage(successMessage)
                setPositiveButton("Ok") { _, _ -> }
                create()
                show()
            }
        }, {
            AlertDialog.Builder(this).apply {
                setTitle("Error")
                setMessage("An error occurred")
                setPositiveButton("Ok") { _, _ -> }
                create()
                show()
            }
        })
    }

    fun rejectUser(user: UserDetails) {
        println("reject here")
        manageFollow(user.friendcode, ApiHandler.SocialAction.REJECT, "Rejected ${user.username}")
    }

    fun acceptUser(user: UserDetails) {
        println("accept here")
        manageFollow(user.friendcode, ApiHandler.SocialAction.ACCEPT, "Accepted ${user.username}")
    }

    fun unfollowUser(user: UserDetails) {
        println("unfollow here")
        manageFollow(
            user.friendcode,
            ApiHandler.SocialAction.UNFOLLOW,
            "Unfollowed ${user.username}"
        )
    }

    fun followRequest(_view: View) {
        startActivity(Intent(this, FollowRequestActivity::class.java).apply {
            putExtra(
                "handler",
                apiHandler
            )
        })
    }
}