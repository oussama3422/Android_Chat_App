package com.example.chatappandroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappandroid.R
import com.example.chatappandroid.databinding.SendMessageBinding
import com.example.chatappandroid.model.Message
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(var context: Context,messages:ArrayList<Message>?,senderRoom:String,receiverRoom:String)
       :RecyclerView.Adapter<RecyclerView.ViewHolder?>()
            {
                lateinit var message:ArrayList<Message>
                val ITEM_SENT=1
                val ITEM_RECEIVE=2
                val senderRoom:String?=null
                var receiveRoom:String?=null

                inner class SentMsgHolder(itemView:View):RecyclerView.ViewHolder(itemView){
                    var binding:SendMessageBinding=SendMessageBinding.bind(itemView)
                }
                inner class ReceiveMsgHolder(itemView:View):RecyclerView.ViewHolder(itemView){
                    var binding:SendMessageBinding=SendMessageBinding.bind(itemView)
                }
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): RecyclerView.ViewHolder {
                    return if(viewType==ITEM_SENT){
                        val view:View=LayoutInflater.from(context).inflate(R.layout.send_message,parent,false)
                        SentMsgHolder(view)
                    }else{
                        val view:View=LayoutInflater.from(context).inflate(R.layout.receive_message,parent,false)
                        ReceiveMsgHolder(view)
                    }
                }

                override fun getItemViewType(position: Int): Int {
                    val messages=message[position]
                    return if(FirebaseAuth.getInstance().uid==messages.senderId){
                        ITEM_SENT
                    }else{
                        ITEM_RECEIVE
                    }
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val message=message[position]
                    if(holder.javaClass==SentMsgHolder::class.java){
                        val viewHolder=holder as SentMsgHolder
                        if(message.message.equals("photo")){
                            // continue Coding Here where I stop
                            viewHolder.binding.mLinear.visibility=View.VISIBLE
                        }

                    }
                }

                override fun getItemCount():Int=message.size

            }