package com.example.fly


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UserAdapter(private val context: Context, private val userList:ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
          val txtname= itemView.findViewById<TextView>(R.id.txtname)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
         val view:View=LayoutInflater.from(context).inflate(R.layout.user_layout,parent,false)
        return  UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser=userList[position]
        holder.txtname.text=currentUser.name
        holder.itemView.setOnClickListener{
            val intent= Intent(context,ChatActivity::class.java)
            intent.putExtra("name",currentUser.name)
            intent.putExtra("uid", currentUser.uid)

            context.startActivity(intent)
        }
    }
}