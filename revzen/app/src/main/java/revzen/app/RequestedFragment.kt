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
        requestedList = view.findViewById(R.id.requested_list)
        viewModel.socialData.observe(viewLifecycleOwner, Observer {set ->
            adapter = SingleButtonRowAdapter(
                requireContext(),
                set.requested,
                (activity as FollowActivity)::unfollowUser,
                "cancel request"
            )
            requestedList.adapter = adapter
        } )
    }
}