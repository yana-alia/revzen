package revzen.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.inflate
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.timepicker.TimeFormat
import revzen.app.api.HistoryResponse
import java.util.zip.Inflater

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

    private fun timeFormat(time: Int): String {
        val hours = time / 60
        val mins = time % 60
        return if (hours < 1) {
            "$mins MINS"
        } else {
            var hourRep = "HOURS"
            if (hours == 1) {
                hourRep = "HOUR"
            }
            if (mins > 0) {
                "$hours $hourRep $mins MINS"
            } else {
                "$hours $hourRep"
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val session = getItem(position) as HistoryResponse
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.history_row, parent, false)

        val title: TextView = view.findViewById(R.id.session_title)
        val description: TextView = view.findViewById(R.id.session_description)
        val icon: ImageView = view.findViewById(R.id.session_icon)

        title.text = "Studied for ${timeFormat(session.study_time / 60)}, with ${timeFormat(session.break_time / 60)} break."
        description.text = "Planned to study for ${timeFormat(session.planned_study_time / 60)} with ${timeFormat(session.planned_break_time / 60)} break."

        if (session.study_time >= session.planned_study_time) {
            icon.setImageResource(R.drawable.petsession)
        } else {
            icon.setImageResource(R.drawable.petbroken)
        }

        return view
    }
}