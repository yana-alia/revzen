package revzen.app.api

import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import com.google.gson.Gson
import okhttp3.*
import okio.IOException
import kotlin.reflect.KFunction1

@Parcelize
class ApiHandler(
    private val subject_id: Long,
    val username: String,
    val friendCode: Int
) : Parcelable {

    // Adapter to make use of the existing buildRequest function
    private fun buildRequest(
        method: String,
        post_fields: List<Pair<String, Any>>,
        callback: Callback
    ) {
        buildRequest(OkHttpClient(), subject_id, method, post_fields, callback)
    }

    // Make an empty post request (common pattern)
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

    fun changePet(new_pet: Pet, on_success: () -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("change_pet", listOf(Pair("pet_type", new_pet.ordinal)), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post {on_failure(ApiError.API_FAILURE)}
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> handler.post { on_success() }
                    409 -> handler.post {on_failure(ApiError.PET_UNAVAILABLE)}
                    404 -> handler.post {on_failure(ApiError.NO_SUCH_USER)}
                    422 -> handler.post {on_failure(ApiError.WRONG_VERSION)}
                    else -> handler.post {on_failure(ApiError.API_FAILURE)}
                }
            }
        })
    }

    fun getCurrentPet(on_success: (PetStatus) -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("get_current_pet", emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post {on_failure(ApiError.API_FAILURE)}
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val petStatus = Gson().fromJson(response.body.string(), PetStatus::class.java)
                        handler.post {on_success(petStatus)}
                    }
                    422 -> handler.post{on_failure(ApiError.WRONG_VERSION)}
                    else ->handler.post{on_failure(ApiError.API_FAILURE)}
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

    fun getPetInfo(on_success: (PetsResponse) -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("get_pet_info", emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val petResponse = Gson().fromJson(response.body.string(), PetsResponse::class.java)
                        handler.post {on_success(petResponse)}
                    }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        })
    }

    fun getRevising(
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
                        422 -> handler.post { on_failure(ApiError.WRONG_VERSION)}
                        else -> handler.post { on_failure(ApiError.API_FAILURE) }
                    }
                }
            })
    }

    fun givePet(new_pet: Pet, on_success: (GiveResult) -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("give_pet", listOf(Pair("pet_type", new_pet.ordinal)), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val giveResponse = Gson().fromJson(response.body.string(), GiveResult::class.java)
                        handler.post { on_success(giveResponse) }
                    }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        })
    }

    data class Reward (
        val xpGained: Int,
        val healthChange: Int,
        val total_study_time: Int,
        val total_break_time: Int
    )

    fun giveReward(reward: Reward, on_success: (RewardResponse) -> Any, on_failure: (ApiError) -> Any) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest("give_reward", listOf(Pair("gained_xp", reward.xpGained), Pair("health_change", reward.healthChange)), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val reward = Gson().fromJson(response.body.string(), RewardResponse::class.java)
                        handler.post {on_success(reward)}
                    }
                    404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        })
    }

    fun logSession(
        study : SessionData,
        on_success: () -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler = Handler(Looper.getMainLooper())
        buildRequest(
            "log_session",
            listOf(
                Pair("planned_study_time", study.planned_study_time),
                Pair("planned_break_time", study.planned_break_time),
                Pair("study_time", study.study_time),
                Pair("break_time", study.break_time),
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
                        200 -> handler.post { on_success() }
                        410 -> handler.post { on_failure(ApiError.FRIENDCODE_NOT_PRESENT) }
                        404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                        400 -> handler.post { on_failure(ApiError.SELF_FRIEND) }
                        409 -> handler.post { on_failure(ApiError.CONFLICTING_DATA) }
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
}