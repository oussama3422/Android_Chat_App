package com.example.chatappandroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappandroid.R
import com.example.chatappandroid.databinding.DeleteLayoutBinding
import com.example.chatappandroid.databinding.SendMessageBinding
import com.example.chatappandroid.model.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(
    var context: Context,
    messages: ArrayList<Messages>?,
    senderRoom: String,
    receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    var message: ArrayList<Messages> = ArrayList()
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val senderRoom: String? = null
    var receiveRoom: String? = null

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.send_message, parent, false)
            SentMsgHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.receive_message, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = message[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = message[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.VISIBLE
                Glide.with(context).load(message.imageUrl)
                    .placeholder(R.drawable.background_shape)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                var binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                var dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyOne.setOnClickListener {
                    message.message = "This Message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom!!)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    message.messageId?.let { it2 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(receiveRoom!!)
                            .child("message")
                            .child(it2).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom!!)
                            .child("message")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {dialog.dismiss()}
                dialog.show()
                false
            }

        }else{
            val viewHolder=holder as ReceiveMsgHolder
            if(message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.VISIBLE
                Glide.with(context).load(message.imageUrl)
                    .placeholder(R.drawable.background_shape)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
                    viewHolder.itemView.setOnLongClickListener {
                        val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                        var binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                        var dialog = AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.root)
                            .create()
                        binding.everyOne.setOnClickListener {
                            message.message = "This Message is removed"
                            message.messageId?.let { it1 ->
                                FirebaseDatabase.getInstance().reference
                                    .child("chats")
                                    .child(senderRoom!!)
                                    .child("message")
                                    .child(it1).setValue(message)
                            }
                            message.messageId?.let { it2 ->
                                FirebaseDatabase.getInstance().reference
                                    .child("chats")
                                    .child(receiveRoom!!)
                                    .child("message")
                                    .child(it2).setValue(message)
                            }
                            dialog.dismiss()
                        }
                        binding.delete.setOnClickListener {
                            message.messageId?.let { it1 ->
                                FirebaseDatabase.getInstance().reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("message")
                                    .child(it1).setValue(null)
                            }
                            dialog.dismiss()
                        }
                        binding.cancel.setOnClickListener {dialog.dismiss()}
                        dialog.show()
                        false

        }
        }
    }

    override fun getItemCount(): Int = message.size

}