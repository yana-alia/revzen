package revzen.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class RequestsFragment : Fragment() {
    private val viewModel: SocialViewModel by activityViewModels()
    private lateinit var adapter: DoubleButtonRowAdapter
    private lateinit var requestsList: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestsList = view.findViewById(R.id.requests_list)
        viewModel.socialData.observe(
            viewLifecycleOwner
        ) { set ->
            adapter = DoubleButtonRowAdapter(
                requireContext(),
                set.requests,
                (activity as FollowActivity)::rejectUser,
                (activity as FollowActivity)::acceptUser,
                "reject",
                "accept"
            )
            requestsList.adapter = adapter
        }
    }
}