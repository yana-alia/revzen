package revzen.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer


class FollowingFragment : Fragment() {
    private val viewModel: SocialViewModel by activityViewModels()
    private lateinit var followingList: ListView
    private lateinit var adapter: SingleButtonRowAdapter
    private lateinit var newFriendButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newFriendButton = view.findViewById(R.id.new_friend)
        newFriendButton.setOnClickListener { (activity as FollowActivity)::newFriend }
        followingList = view.findViewById(R.id.following_list)
        viewModel.socialData.observe(viewLifecycleOwner, Observer { set ->
            adapter = SingleButtonRowAdapter(
                requireContext(),
                set.following,
                (activity as FollowActivity)::unfollowUser,
                "unfollow"
            )
            followingList.adapter = adapter
        })
    }
}