package com.example.chatappandroid

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatappandroid.adapter.UserAdapter
import com.example.chatappandroid.databinding.ActivityMainBinding
import com.example.chatappandroid.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    var binding:ActivityMainBinding?=null
    var database:FirebaseDatabase=FirebaseDatabase.getInstance()
    var users:ArrayList<User>?=null
    var usersAdapter:UserAdapter?=null
    var dialog:ProgressDialog?=null
    var user:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog= ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        users=ArrayList<User>()
        usersAdapter= UserAdapter(this@MainActivity,users!!)
        val layoutManager=GridLayoutManager(this@MainActivity,2)
        binding!!.mRec.layoutManager=layoutManager
        database.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot)
                   {
                       user = snapshot.getValue(User::class.java)
                   }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        binding!!.mRec.adapter = usersAdapter
        database.reference.child("users").addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                 users!!.clear()
                for(snapshot1 in snapshot.children){
                    val user:User?=snapshot1.getValue(User::class.java)
                    if(!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                }
                usersAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val currentId=FirebaseAuth.getInstance().uid
        database.reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId=FirebaseAuth.getInstance().uid
        database.reference.child("presence").child(currentId!!).setValue("Offline")
    }
}