package com.example.fly

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.fly.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login_Activity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var mauth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mauth= FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
           val email= binding.loginEmail.text.toString()
            val password=binding.loginPassword.text.toString()
            if( email.isEmpty()|| password.isEmpty()){
                Toast.makeText(this,"Please fill all details", Toast.LENGTH_SHORT).show()
            }else{
                login(email,password)
            }
        }
        binding.signupButton.setOnClickListener {
            val intent= Intent(this,SignUpActivity::class.java)
            finish()
            startActivity(intent)
        }



    }
    private fun login(email:String,password:String){
     mauth.signInWithEmailAndPassword(email,password)
         .addOnCompleteListener(this){task ->
             if(task.isSuccessful){
                 Log.d(TAG,"Signed in")
                 val intent=Intent(this,Home_Activity::class.java)
                 finish()
                 startActivity(intent)
             } else {
                 // If sign in fails, display a message to the user.
                 Log.w(TAG, "signInWithEmail:failure", task.exception)
                 Toast.makeText(
                     baseContext,
                     "Authentication failed.",
                     Toast.LENGTH_SHORT,
                 ).show()

             }

         }

    }
}