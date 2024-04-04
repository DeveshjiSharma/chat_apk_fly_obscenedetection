package com.example.fly

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.fly.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding
    private lateinit var mauth:FirebaseAuth
    private lateinit var database:DatabaseReference
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        mauth = FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance().getReference()
        FirebaseApp.initializeApp(this)

        binding.signupButton.setOnClickListener {
            val name = binding.signupName.text.toString()
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else {
                signUp(name, email, password)
            }
        }


        binding.loginbutton.setOnClickListener {
            val intent=Intent(this,Login_Activity::class.java)
            finish()
            startActivity(intent)
        }
    }
    private fun signUp(name: String, email: String, password: String) {
        mauth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("Worked","But not adding")
                    val user = mauth.currentUser
                    if (user != null) {
                        val userdetails=User(name,email,user.uid)
                        database.child("Users").child(user.uid).setValue(userdetails)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Data stored successfully", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "Data stored successfully")
                                } else {
                                    // Data storage failed
                                    Toast.makeText(this, "Data storage failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    Log.w(TAG, "Data storage failed", task.exception)
                                }
                            }


                        val intent = Intent(this, Login_Activity::class.java)
                        finish()
                        startActivity(intent)
                    }
                } else {

                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


}