package com.example.fly

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fly.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Home_Activity : AppCompatActivity() {
    private lateinit var binding:ActivityHomeBinding
    private lateinit var userRecyclerView:RecyclerView
    private lateinit var adapter:UserAdapter
    private lateinit var userList:ArrayList<User>
    private lateinit var mauth:FirebaseAuth
    private lateinit var database:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mauth=FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance().reference
        userList=ArrayList()
        adapter=UserAdapter(this,userList)
        userRecyclerView=findViewById(R.id.userRecyclerView)

        userRecyclerView.layoutManager=LinearLayoutManager(this)
        userRecyclerView.adapter=adapter

        database.child("Users").addValueEventListener(object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
              for(postSnapshot in snapshot.children){
                  val currentUser =postSnapshot.getValue(User::class.java)
                  if(mauth.currentUser?.uid!=currentUser?.uid){
                      userList.add(currentUser!!)
                  }

              }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            mauth.signOut()
            val intent= Intent(this,SignUpActivity::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true

    }
}