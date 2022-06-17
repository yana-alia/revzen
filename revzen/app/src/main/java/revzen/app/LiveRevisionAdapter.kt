package revzen.app

import revzen.app.api.LiveRevisionResponse


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class LiveRevisionAdapter(private val context: Context,
                     private val dataSource: Array<LiveRevisionResponse>) : BaseAdapter() {

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val liveReviser = getItem(position) as LiveRevisionResponse
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.live_revision_row, parent, false)

        val title: TextView = view.findViewById(R.id.live_user_name)
        val description: TextView = view.findViewById(R.id.live_user_friendcode)

        title.text = "User: ${liveReviser.username} currently revising."
        description.text = "FriendCode: ${liveReviser.friendcode}"

        return view
    }
}