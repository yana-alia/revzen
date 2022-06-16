package revzen.app.api

import com.google.gson.Gson
import okhttp3.*
import okio.IOException
import revzen.app.BuildConfig

public class ApiHandler(
    private val client: OkHttpClient,
    private val subject_id: Long,
    val username: String,
    val friendcode: Int
) {

    // Creates a new request, with the subject_id and client version added as post request fields by default.
    private fun build_request(
        method: String,
        post_fields: List<Pair<String, Any>>,
        callback: Callback
    ) {
        build_request(client, subject_id, method, post_fields, callback)
    }

    public fun get_history(
        on_success: (Array<HistoryResponse>) -> Any,
        on_failure: (ApiError) -> Any
    ) {
        build_request("get_history", emptyList(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                on_failure(ApiError.API_FAILURE)
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> on_success(
                        Gson().fromJson(
                            response.body.string(),
                            Array<HistoryResponse>::class.java
                        )
                    )
                    404 -> on_failure(ApiError.NO_SUCH_USER)
                    422 -> on_failure(ApiError.WRONG_VERSION)
                    else -> on_failure(ApiError.API_FAILURE)
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
                    on_failure(ApiError.API_FAILURE)
                }

                override fun onResponse(call: Call, response: Response) {
                    when (response.code) {
                        200 -> on_success()
                        404 -> on_failure(ApiError.NO_SUCH_USER)
                        422 -> on_failure(ApiError.WRONG_VERSION)
                        else -> on_failure(ApiError.API_FAILURE)
                    }
                }
            })
    }
}