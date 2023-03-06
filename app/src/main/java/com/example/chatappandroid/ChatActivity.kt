package com.example.chatappandroid

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatappandroid.adapter.MessageAdapter
import com.example.chatappandroid.databinding.ActivityChatBinding
import com.example.chatappandroid.model.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessageAdapter? = null
    var messages: ArrayList<Messages>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var storage: FirebaseStorage = FirebaseStorage.getInstance()
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiveUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.background_shape)
            .into(binding!!.profile01)
        binding!!.imageView.setOnClickListener {
            finish()
        }
        receiveUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database.reference.child("Presence").child(receiveUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline") {
                            binding!!.status.visibility = View.GONE
                        } else {
                            binding!!.status.text = status
                            binding!!.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        senderRoom = senderUid + receiveUid
        receiverRoom= receiveUid + senderUid
        adapter=MessageAdapter((this@ChatActivity),messages,senderRoom!!,receiverRoom!!)
        binding!!.recyclerView.layoutManager=LinearLayoutManager(this@ChatActivity)
        binding!!.recyclerView.adapter=adapter
        database.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for(snapshot1 in snapshot.children){
                        val message:Messages?=snapshot1.getValue(Messages::class.java)
                        message!!.messageId=snapshot1.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        binding!!.sendBtn.setOnClickListener{
            val messageTxt:String=binding!!.messageBox.text.toString()
            val date=Date(0)
            val message=Messages(messageTxt,senderUid,date.time)

            binding!!.messageBox.setText("")
            val randomKey=database.reference.push().key
            val lastMsgObj=HashMap<String,Any>()
            lastMsgObj["lastMsg"]=message.message!!
            lastMsgObj["lastMsgTime"]=date.time

            database.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("message")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }
                }

        }
        binding!!.attachment.setOnClickListener{
            val intent= Intent()
            intent.action=Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,25)
        }

        val handler=Handler()
        binding!!.messageBox.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                database.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping,1000)
            }
            var userStoppedTyping = Runnable {
                database.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }

        })
        supportActionBar?.setDisplayShowTitleEnabled(false)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==25){
            if(data!=null){
                if(data.data != null)
                {
                   val selectedImg=data.data
                    val calender= Calendar.getInstance()
                    var refence=storage.reference.child("chats")
                        .child(calender.timeInMillis.toString()+"")
                    dialog!!.show()
                    refence.putFile(selectedImg!!)
                        .addOnCompleteListener{task->
                            dialog!!.dismiss()
                            if(task.isSuccessful){
                                refence.downloadUrl.addOnSuccessListener { uri->
                                    val filePath=uri.toString()
                                    val messageTxt:String=binding!!.messageBox.text.toString()
                                    val date=Date(0)
                                    val message=Messages(messageTxt,senderUid,date.time)
                                    message.message="photo"
                                    message.imageUrl=filePath
                                    binding!!.messageBox.setText("")
                                    val randomKey=database.reference.push().key
                                    val lastMsgObj=HashMap<String,Any>()
                                    lastMsgObj["lastMsg"]=message.message!!
                                    lastMsgObj["lastMsgTime"]=date.time
                                    database.reference.child("chats")
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats")
                                        .child(senderRoom!!)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database!!.reference.child("chats")
                                                .child(receiverRoom!!)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener{  }
                                        }
                                }
                            }

                        }

                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val currentId=FirebaseAuth.getInstance().uid
        database.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }
    override fun onPause() {
        super.onPause()
        val currentId=FirebaseAuth.getInstance().uid
        database.reference.child("Presence")
            .child(currentId!!)
            .setValue("offline")
    }

}