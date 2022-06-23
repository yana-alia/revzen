package revzen.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
    lateinit var pageAdapter: FollowPageAdapter

    private val handler = Handler()

    private val updateTask: Runnable = object : Runnable {
        override fun run() {
            updateFollowData()
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateFollowData() {
        apiHandler.getFollows({ viewModel.updatedata(it) }, this::getFollowersFailure)
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

    fun rejectUser(user: UserDetails) {
        println("reject user")
    }
    fun acceptUser(user: UserDetails) {
        println("accept user")
    }
    fun followUser(user: UserDetails) {
        println("follow user")
    }
    fun unfollowUser(user: UserDetails) {
        println("Unfollowed user call here")
    }
    fun newFriend() {
        println("new_friend")
    }
}