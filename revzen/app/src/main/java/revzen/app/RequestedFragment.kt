package revzen.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class RequestedFragment : Fragment() {
    private val viewModel: SocialViewModel by activityViewModels()
    private lateinit var requestedList: ListView
    private lateinit var adapter: SingleButtonRowAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_requested, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestedList = view.findViewById(R.id.requestedListView)
        viewModel.socialData.observe(viewLifecycleOwner) { set ->
            adapter = SingleButtonRowAdapter(
                requireContext(),
                set.requested,
                (activity as FollowActivity)::unfollowUser,
                "cancel"
            )
            requestedList.adapter = adapter
        }
    }
}