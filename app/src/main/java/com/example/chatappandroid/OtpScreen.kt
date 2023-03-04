package com.example.chatappandroid

import `in`.aabhasjindal.otptextview.OTPListener
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.chatappandroid.databinding.ActivityOtpScreenBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpScreen : AppCompatActivity() {
    var binding: ActivityOtpScreenBinding?=null
    var verficationId:String?=null
    var auth: FirebaseAuth =FirebaseAuth.getInstance()
    var dialog:ProgressDialog?=null
    var context:Context=this
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_otp_screen)
        binding= ActivityOtpScreenBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog= ProgressDialog(this)
        dialog!!.setMessage("Sending OTP...")
        dialog!!.setCancelable(false)
        dialog!!.show()
        supportActionBar?.hide()
        val phoneNumber=intent.getStringExtra("phoneNumber")
        binding!!.phoneLabel.text="Verify $phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(
                object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        dialog= ProgressDialog(this@OtpScreen)
                        dialog!!.setMessage("the Code Message Correct ✅")
                        dialog!!.setCancelable(false)
                        dialog!!.show()
                        supportActionBar?.hide()
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        dialog= ProgressDialog(this@OtpScreen)
                        dialog!!.setMessage("the Code Message Not Correct ⛔")
                        dialog!!.setCancelable(false)
                        dialog!!.show()
                        supportActionBar?.hide()
                    }

                    override fun onCodeSent(verifyId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken)
                        dialog!!.dismiss()
                        verficationId=verifyId
                        val imm=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                        binding!!.otpView.requestFocus()
                    }
                    }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding!!.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // fired when user types something in the OTP BOX
            }

            override fun onOTPComplete(otp: String) {
                // fired when user has entered the OTP fully.
                val credential= PhoneAuthProvider.getCredential(verficationId!!,otp)
                auth.signInWithCredential(credential).addOnCompleteListener {
                        task->
                    if(task.isSuccessful){
                        val intent= Intent(context,SetupProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()

                    }else{
                        Toast.makeText(context,"Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }
}