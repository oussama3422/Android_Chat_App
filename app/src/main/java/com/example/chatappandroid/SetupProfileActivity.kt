package com.example.chatappandroid

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatappandroid.databinding.ActivitySetupProfileBinding
import com.example.chatappandroid.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    var binding:ActivitySetupProfileBinding?=null
    var auth:FirebaseAuth=FirebaseAuth.getInstance()
    var storage:FirebaseStorage=FirebaseStorage.getInstance()
    var database:FirebaseDatabase=FirebaseDatabase.getInstance()
    var selectedImage: Uri?=null
    var dialog: ProgressDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog= ProgressDialog(this)
        dialog!!.setMessage("Uploading Profile..")
        dialog!!.setCancelable(false)
        supportActionBar?.hide()
        binding!!.imgeView.setOnClickListener{
            val intent= Intent()
            intent.action=Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,45)
        }
        binding!!.ProfileBtn.setOnClickListener {
            val name:String=binding!!.userNameProfile.text.toString()
            if(name.isEmpty()){
                binding!!.userNameProfile.setError("Please type a name")
            }
            dialog!!.show()
            if(selectedImage!=null){
                val ref=storage.reference.child("Profile").child(auth.uid!!)
                ref.putFile(selectedImage!!).addOnCompleteListener{task->
                    if(task.isSuccessful){
                        ref.downloadUrl.addOnCompleteListener {uri->
                            val image=uri.toString()
                            val uid=auth.uid
                            val phone=auth.currentUser!!.phoneNumber
                            val userName:String=binding!!.userNameProfile.text.toString()
                            val user= User(uid,userName,phone,image)
                            database.reference.child("users").child(uid!!).setValue(user).addOnCompleteListener {
                                dialog!!.dismiss()
                                val intent=Intent(this@SetupProfileActivity,MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    else{
                        val uid=auth.uid
                        val phone=auth.currentUser!!.phoneNumber
                        val userName:String=binding!!.userNameProfile.text.toString()
                        val user= User(uid,userName,phone,profileImage = "No Image")

                        database.reference.child("users").child(uid!!).setValue(user).addOnCompleteListener {
                            dialog!!.dismiss()
                            val intent=Intent(this@SetupProfileActivity,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            if(data.data!=null){
                val uri=data.data //filePath
                val storage=FirebaseStorage.getInstance()
                val time=Date().time
                val ref=storage.reference.child("Profile").child(time.toString() + "")
                ref.putFile(uri!!).addOnCompleteListener{task->
                    if(task.isSuccessful){
                        ref.downloadUrl.addOnCompleteListener { uri->
                            val filePath=uri.toString()
                            val obj=HashMap<String,Any>()
                            obj["image"]=filePath
                            database.reference.child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener {

                            }
                        }
                    }
                }
                binding!!.imgeView.setImageURI(data.data)
                selectedImage=data.data
            }
        }
    }
}