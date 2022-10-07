package revzen.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels


class FollowingFragment : Fragment() {
    private val viewModel: SocialViewModel by activityViewModels()
    private lateinit var followingList: ListView
    private lateinit var adapter: SingleButtonRowAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        followingList = view.findViewById(R.id.followingListVIew)
        viewModel.socialData.observe(viewLifecycleOwner) { set ->
            adapter = SingleButtonRowAdapter(
                requireContext(),
                set.followers,
                (activity as FollowActivity)::unfollowUser,
                "unfollow"
            )
            followingList.adapter = adapter
        }
    }
}