package revzen.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import revzen.app.api.ApiHandler
import revzen.app.api.FollowersResponse

class SocialViewModel : ViewModel() {
    val socialData: MutableLiveData<FollowersResponse> = MutableLiveData(FollowersResponse(emptyList(),emptyList(),emptyList(),emptyList()))

    fun updatedata(new_data: FollowersResponse) {
        socialData.value = new_data
    }
}