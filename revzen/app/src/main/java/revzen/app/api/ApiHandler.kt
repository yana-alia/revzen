package revzen.app.api

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import com.google.gson.Gson
import okhttp3.*
import okio.IOException
import revzen.app.Pet

@Parcelize
class ApiHandler(
    private val subject_id: Long,
    val username: String,
    val friendcode: Int
) : Parcelable {

    // Creates a new request, with the subject_id and client version added as post request fields by default.
    private fun buildRequest(
        method: String,
        post_fields: List<Pair<String, Any>>,
        callback: Callback
    ) {
        // Inefficient, but OkHttpClient is not Parcelizable, and regardless would be destroyed and remade.
        buildRequest(OkHttpClient(), subject_id, method, post_fields, callback)
    }

    fun getHistory(
        on_success: (Array<HistoryResponse>) -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("get_history", emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val history = Gson().fromJson(
                            response.body.string(),
                            Array<HistoryResponse>::class.java
                        )
                        handler.post { on_success(history) }
                    }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }

            }
        })
    }

    fun logSession(
        planned_study_time: Int,
        planned_break_time: Int,
        study_time: Int,
        break_time: Int,
        on_success: () -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest(
            "log_session",
            listOf(
                Pair("planned_study_time", planned_study_time),
                Pair("planned_break_time", planned_break_time),
                Pair("study_time", study_time),
                Pair("break_time", break_time)
            ),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.post { on_failure(ApiError.API_FAILURE) }
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.code) {
                        200 -> handler.post { on_success() }
                        404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                        422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                        else -> handler.post { on_failure(ApiError.API_FAILURE) }
                    }
                }
            })
    }

    fun startLiveRevision(on_success: () -> Any, on_failure: (ApiError) -> Any) {
        apiEmptyPost("start_revising", on_success, on_failure)
    }

    fun stopLiveRevision(on_success: () -> Any, on_failure: (ApiError) -> Any) {
        apiEmptyPost("stop_revising", on_success, on_failure)
    }

    private fun apiEmptyPost(method: String, on_success: () -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest(method, emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> handler.post { on_success() }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        })
    }

    fun getLiveRevision(
        on_success: (Array<LiveRevisionResponse>) -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("get_revising", emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val liveUsers = Gson().fromJson(
                            response.body.string(),
                            Array<LiveRevisionResponse>::class.java
                        )
                        handler.post {
                            on_success(liveUsers)
                        }
                    }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        })
    }

    enum class SocialAction(val apicode: String) {
        REQUEST("request"),
        ACCEPT("accept"),
        REJECT("reject"),
        UNFOLLOW("unfollow"),
    }

    fun manageFollower(
        friend_code: Int,
        action: SocialAction,
        on_success: () -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest(
            "manage_follows",
            listOf(Pair("friend_code", friend_code.toString()), Pair("action", action.apicode)),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.post { on_failure(ApiError.API_FAILURE) }
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.code) {
                        200 -> handler.post {
                            println("success")
                            on_success()
                        }
                        410 -> handler.post { on_failure(ApiError.FRIENDCODE_NOT_PRESENT) }
                        404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                        400 -> handler.post { on_failure(ApiError.SELF_FRIEND) }
                        409 -> handler.post { on_failure(ApiError.CONFLICTING_DATA) }
                        else -> handler.post { on_failure(ApiError.API_FAILURE) }
                    }
                }
            })
    }

    fun getFollows(on_success: (FollowersResponse) -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("get_follows", emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val followers = Gson().fromJson(
                            response.body.string(),
                            FollowersResponse::class.java
                        )
                        handler.post {
                            on_success(followers)
                        }
                    }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        })
    }

    fun getUser(friend_code: Int, on_success: (UserDetails) -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest(
            "get_user",
            listOf(Pair("friendcode", friend_code.toString())),
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.post { on_failure(ApiError.API_FAILURE) }
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.code) {
                        200 -> {
                            val user = Gson().fromJson(
                                response.body.string(),
                                UserDetails::class.java
                            )
                            handler.post {
                                on_success(user)
                            }
                        }
                        410 -> handler.post { on_failure(ApiError.FRIENDCODE_NOT_PRESENT) }
                        404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                        else -> handler.post { on_failure(ApiError.API_FAILURE) }
                    }
                }
            })
    }

    //todo stub for testing, implement properly
    fun getMainPet(
        on_success: (Pet) -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            on_success(Pet.SHIBA)
        }
    }
}