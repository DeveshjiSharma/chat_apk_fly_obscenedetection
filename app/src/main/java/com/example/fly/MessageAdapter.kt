package com.example.fly

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEMRECEIVED = 1
    private val ITEMSENT = 2
    private val IMAGESENT = 3
    private val IMAGERECEIVED = 4


    fun getLastItemPosition():Int{
        return if(messageList.isNotEmpty()){
            messageList.size-1
        }else{
            0
        }
    }
    private var currentPosition: Int = -1

    private fun setCurrentPosition(position: Int) {
        currentPosition = position
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ITEMRECEIVED -> {
                // Inflate received layout
                val view = LayoutInflater.from(context).inflate(R.layout.received, parent, false)
                ReceiveViewHolder(view)
            }
            IMAGESENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.sentimage, parent, false)
                SentImageViewHolder(view)
            }
            IMAGERECEIVED -> {
                val view = LayoutInflater.from(context).inflate(R.layout.receivedimage, parent, false)
                ReceivedImageViewHolder(view)
            }
            else -> {
                // Inflate sent layout
                val view = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
                SentViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId && currentMessage.message!=null) {
            ITEMSENT
        } else if(FirebaseAuth.getInstance().currentUser?.uid != currentMessage.senderId && currentMessage.message!=null){
            ITEMRECEIVED
        }else if(FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderId && currentMessage.imageUrl!=null){
            IMAGESENT
        }else{
            IMAGERECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        setCurrentPosition(position)
        if (holder.javaClass== SentViewHolder::class.java) {
            // For sending
            val viewHolder=holder as SentViewHolder
            viewHolder.sentMessage.text = currentMessage.message
        }else if(holder.javaClass==SentImageViewHolder::class.java){
            val viewHolder=holder as SentImageViewHolder
            Glide.with(context)
                .load(currentMessage.imageUrl)
                .into(viewHolder.sentImage)
        } else if(holder.javaClass==ReceivedImageViewHolder::class.java){
            val viewHolder=holder as ReceivedImageViewHolder
            Glide.with(context)
                .load(currentMessage.imageUrl)
                .into(viewHolder.receivedImage)
        }else {
            // For receiving
            val viewHolder=holder as ReceiveViewHolder
            viewHolder.receivedMessage.text = currentMessage.message
        }


        }
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.text_sent_message)!!
    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage = itemView.findViewById<TextView>(R.id.text_received_message)!!
    }

    class SentImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val sentImage=itemView.findViewById<ImageView>(R.id.sentimg)!!
    }

    class ReceivedImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val receivedImage=itemView.findViewById<ImageView>(R.id.receivedimg)!!
    }
}