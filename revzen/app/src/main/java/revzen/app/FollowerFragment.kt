package revzen.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer

class FollowerFragment : Fragment() {
    private val viewModel: SocialViewModel by activityViewModels()
    private lateinit var adapter: SingleButtonRowAdapter
    private lateinit var followerList: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        followerList = view.findViewById(R.id.follower_list)
        viewModel.socialData.observe(viewLifecycleOwner, Observer { set ->
            adapter = SingleButtonRowAdapter(
                requireContext(),
                set.followers,
                (activity as FollowActivity)::rejectUser,
                "reject"
            )
            followerList.adapter = adapter
        })
    }
}