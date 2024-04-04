package com.example.fly

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class ImageMessageAdapter(private val context: Context,private val data: ArrayList<MessageImage> ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEMRECEIVED = 1
    private val ITEMSENT = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType==ITEMRECEIVED){
            val view=LayoutInflater.from(context).inflate(R.layout.receivedimage,parent,false)
            ReceivedImageViewHolder(view)
        }else{
            val view =LayoutInflater.from(context).inflate(R.layout.sentimage,parent,false)
            SentImageViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage =data[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid==currentMessage.senderId){
            ITEMSENT
        }else{
            ITEMRECEIVED
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage=data[position]
        if(holder.javaClass==SentImageViewHolder::class.java){
            val viewHolder =holder as SentImageViewHolder
            if(currentMessage.image!=null){
                Glide.with(context)
                    .load(currentMessage.image)
                    .into(viewHolder.sentImageMessage)
            }else {
                // If the message does not contain an image URL, hide the ImageView
                viewHolder.sentImageMessage.visibility = View.GONE
            }

        }else{
            val viewHolder =holder as ReceivedImageViewHolder
            if(currentMessage.image!=null){
                Glide.with(context)
                    .load(currentMessage.image)
                    .into(viewHolder.receivedImagemessage)
            }else{
                viewHolder.receivedImagemessage.visibility=View.GONE
            }
        }
    }


    class SentImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val sentImageMessage=itemView.findViewById<ImageView>(R.id.sendimage)
    }

    class ReceivedImageViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val receivedImagemessage=itemView.findViewById<ImageView>(R.id.receivedimg)
    }
}