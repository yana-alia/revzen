package revzen.app.api

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import okhttp3.*
import revzen.app.BuildConfig
import java.io.IOException

fun buildRequest(
    client: OkHttpClient,
    subject_id: Long,
    method: String,
    post_fields: List<Pair<String, Any>>,
    callback: Callback
) {
    val requestBody = FormBody.Builder().add("user_id", subject_id.toString())
        .add("version", BuildConfig.VERSION_CODE.toString()).apply {
            for ((key, value) in post_fields) {
                add(key, value.toString())
            }
        }.build()
    val request = Request.Builder().url(BuildConfig.API + "api/" + method).post(requestBody).build()
    client.newCall(request).enqueue(callback)
}

fun loginUser(subject_id: Long, on_success: (ApiHandler) -> Any, on_failure: (ApiError) -> Any) {
    val client = OkHttpClient()
    val handler = Handler(Looper.getMainLooper())
    buildRequest(client, subject_id, "login", emptyList(), object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            handler.post { on_failure(ApiError.API_FAILURE) }
        }

        override fun onResponse(call: Call, response: Response) {
            when (response.code) {
                200 -> {
                    val loginResponse =
                        Gson().fromJson(response.body.string(), UserDetails::class.java)
                    handler.post {
                        on_success(
                            ApiHandler(
                                subject_id,
                                loginResponse.username,
                                loginResponse.friendcode
                            )
                        )
                    }
                }
                404 -> handler.post { on_failure(ApiError.NO_SUCH_USER) }
                422 -> handler.post { on_failure(ApiError.WRONG_VERSION) }
                else -> handler.post { on_failure(ApiError.API_FAILURE) }
            }
        }
    })
}

fun createUser(
    subject_id: Long,
    username: String,
    on_success: () -> Any,
    on_failure: (ApiError) -> Any
) {
    val client = OkHttpClient()
    val handler = Handler(Looper.getMainLooper())
    buildRequest(
        client,
        subject_id,
        "create",
        listOf(Pair("user_name", username)),
        object : Callback {
            override fun onFailure(call: Call, e: okio.IOException) {
                handler.post { on_failure(ApiError.API_FAILURE) }
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> handler.post { on_success() }
                    409 -> handler.post { on_failure(ApiError.USER_ALREADY_EXISTS) }
                    else -> handler.post { on_failure(ApiError.API_FAILURE) }
                }
            }
        }
    )
}
