package revzen.app.api

import com.google.gson.annotations.SerializedName

data class FollowersResponse(
    @SerializedName("requests") val requests : List<UserDetails>,
    @SerializedName("requested") val requested : List<UserDetails>,
    @SerializedName("following") val following : List<UserDetails>,
    @SerializedName("followers") val followers : List<UserDetails>
)
