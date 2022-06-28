package revzen.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import revzen.app.api.UserDetails

class SingleButtonRowAdapter(
    private val context: Context,
    private val dataSource: List<UserDetails>,
    private val buttonFun: (UserDetails) -> Any,
    private val buttonText: String
) : BaseAdapter() {
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
        val view =
            convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.single_button_row, parent, false)

        val user = getItem(position) as UserDetails

        val title: TextView = view.findViewById(R.id.row_title)
        val description: TextView = view.findViewById(R.id.row_description)
        val icon: ImageView = view.findViewById(R.id.row_icon)
        val button: Button = view.findViewById(R.id.row_button)
        val descText = "friend code: " + user.friendcode

        icon.setImageResource(user.main_pet.logoImage)
        title.text = user.username
        description.text = descText
        button.text = buttonText
        button.setOnClickListener { buttonFun(user) }

        return view
    }
}