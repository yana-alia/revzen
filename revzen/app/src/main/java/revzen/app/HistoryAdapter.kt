package revzen.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import revzen.app.api.HistoryResponse

class HistoryAdapter(private val context: Context,
                     private val dataSource: Array<HistoryResponse>) : BaseAdapter() {

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun timeFormat(seconds: Int): String {
        val secs = seconds % 60
        val mins = (seconds / 60) % 60
        val hours = seconds / (60 * 60)
        return String.format("%02d:%02d:%02d", hours, mins, secs)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val session = getItem(position) as HistoryResponse
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.history_row, parent, false)

        val title: TextView = view.findViewById(R.id.session_title)
        val description: TextView = view.findViewById(R.id.session_description)
        val icon: ImageView = view.findViewById(R.id.session_icon)

        title.text = "Studied for ${timeFormat(session.study_time)}, with ${timeFormat(session.break_time)} break."
        description.text = "Planned to study for ${timeFormat(session.planned_study_time)} with ${timeFormat(session.planned_break_time)} break."

        if (session.study_time >= session.planned_study_time) {
            icon.setImageResource(R.drawable.petsession)
        } else {
            icon.setImageResource(R.drawable.petbroken)
        }

        return view
    }
}