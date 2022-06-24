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

class DoubleButtonRowAdapter(
    private val context: Context,
    private val dataSource: List<UserDetails>,
    private val buttonOneFun: (UserDetails) -> Any,
    private val buttonTwoFun: (UserDetails) -> Any,
    private val buttonOneText: String,
    private val buttonTwoText: String
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
            convertView ?: LayoutInflater.from(context).inflate(R.layout.two_button_row, parent, false)

        val user = getItem(position) as UserDetails

        val title: TextView = view.findViewById(R.id.d_row_title)
        val description: TextView = view.findViewById(R.id.d_row_description)
        val icon: ImageView = view.findViewById(R.id.d_row_icon)
        val buttonOne: Button = view.findViewById(R.id.d_row_button_one)
        val buttonTwo: Button = view.findViewById(R.id.d_row_button_two)

        icon.setImageResource(R.drawable.petstudy_shiba)
        title.text = user.username
        description.text = "friendcode: " + user.friendcode
        buttonOne.text = buttonOneText
        buttonTwo.text = buttonTwoText
        buttonOne.setOnClickListener {buttonOneFun(user)}
        buttonTwo.setOnClickListener {buttonTwoFun(user)}

        return view
    }
}