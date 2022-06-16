package revzen.app.api
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import com.google.gson.Gson
import okhttp3.*
import okio.IOException
import revzen.app.BuildConfig

@Parcelize
public class ApiHandler(
    private val subject_id: Long,
    val username: String,
    val friendcode: Int
) : Parcelable {

    // Creates a new request, with the subject_id and client version added as post request fields by default.
    private fun build_request(
        method: String,
        post_fields: List<Pair<String, Any>>,
        callback: Callback
    ) {
        // Inefficient, but OkHttpClient is not Parcelizable, and regardless would be destroyed and remade.
        build_request(OkHttpClient(), subject_id, method, post_fields, callback)
    }

    public fun get_history(
        on_success: (Array<HistoryResponse>) -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler: Handler = Handler(Looper.getMainLooper())
        build_request("get_history", emptyList(), object : Callback {
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
                        handler.post {on_success(history)}
                    }
                    404 -> handler.post {on_failure(ApiError.NO_SUCH_USER) }
                    422 -> handler.post {on_failure(ApiError.WRONG_VERSION) }
                    else -> handler.post {on_failure(ApiError.API_FAILURE) }
                }

            }
        })
    }

    public fun log_session(
        planned_study_time: Int,
        planned_break_time: Int,
        study_time: Int,
        break_time: Int,
        on_success: () -> Any,
        on_failure: (ApiError) -> Any
    ) {
        val handler: Handler = Handler(Looper.getMainLooper())
        build_request(
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
}