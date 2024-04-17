package com.example.fly


import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {
    private lateinit var messageRecyclerview: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var imagesend: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var imageList: ArrayList<MessageImage>
    private lateinit var client:OkHttpClient
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val RC_SELECT_IMG = 2
    private var name :String?=null
    private var receiveruid :String?=null
    private var senderuid  :String?=null
    private var receiverRoom: String? =null
    private var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        imagesend = findViewById(R.id.sendimage)
        storage = FirebaseStorage.getInstance().reference.child("images").child(System.currentTimeMillis().toString())

        database = FirebaseDatabase.getInstance().reference

        client= OkHttpClient()
        // Ensure senderuid is not null before creating chatrooms
        name = intent.getStringExtra("name")
         receiveruid = intent.getStringExtra("uid")
         senderuid = FirebaseAuth.getInstance().currentUser?.uid
         receiverRoom = receiveruid + senderuid
         senderRoom = senderuid + receiveruid
        supportActionBar?.title = name

        messageRecyclerview = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentbutton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        messageRecyclerview.layoutManager = LinearLayoutManager(this)
        messageRecyclerview.adapter = messageAdapter
        messageRecyclerview.addItemDecoration(
            VerticalSpaceItemDecoration(
                resources.getDimension(R.dimen.item_spacing).toInt()
            )
        )
        // Logic for retrieving data from database
        // Adding message to database
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = Message(message, senderuid,null)

            Toast.makeText(this, "Message is travelling", Toast.LENGTH_SHORT).show()
            messageBox.setText("")
            database.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject)
                .addOnSuccessListener {
                    database.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to store message in receiverRoom: $e")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to store message in senderRoom: $e")
                }
            messageBox.setText("")
        }
        database.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        Log.d(TAG, "Retrieved message: $message")
                        messageList.add(message!!)
                    }
                    Log.d(TAG, "Message list size after onDataChange: ${messageList.size}")
                    messageAdapter.notifyDataSetChanged()
                    val lastItemPosition = messageAdapter.getLastItemPosition()
                    messageRecyclerview.smoothScrollToPosition(lastItemPosition)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        Log.d(TAG, "Message list size after onDataChange: ${messageList.size}")
        messageAdapter.notifyDataSetChanged()
        val lastItemPosition = messageAdapter.getLastItemPosition()
        messageRecyclerview.smoothScrollToPosition(lastItemPosition)
        imagesend.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMG)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SELECT_IMG && resultCode ==Activity.RESULT_OK &&data?.data !=null ){
            val selectedUri :Uri?=data.data
            if(selectedUri !=null) {
                 uploadImagetoapi(selectedUri){response->
                     runOnUiThread {

                         Toast.makeText(this@ChatActivity, response, Toast.LENGTH_SHORT).show();
                         Log.d("err", response.length.toString())
                     }
                    if (response.contains( "false")) {
                            Toast.makeText(this@ChatActivity,"Inside uploading",Toast.LENGTH_LONG).show();
                    storage.putFile(selectedUri).addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            val messageObject = Message(null, senderuid, imageUrl)
                            database.child("chats").child(senderRoom!!).child("messages").push()
                                .setValue(messageObject)
                                .addOnSuccessListener {
                                    database.child("chats").child(receiverRoom!!).child("messages")
                                        .push()
                                        .setValue(messageObject)
                                        .addOnFailureListener { e ->

                                            Log.e(
                                                TAG,
                                                "Failed to store message in receiverRoom: $e"
                                            )
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to store message in senderRoom: $e")
                                }
                            messageBox.setText("")
                        }
                        database.child("chats").child(senderRoom!!).child("messages")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    messageList.clear()
                                    for (postSnapshot in snapshot.children) {
                                        val message = postSnapshot.getValue(Message::class.java)
                                        Log.d(TAG, "Retrieved message: $message")
                                        messageList.add(message!!)
                                    }
                                    Log.d(
                                        TAG,
                                        "Message list size after onDataChange: ${messageList.size}"
                                    )
                                    messageAdapter.notifyDataSetChanged()
                                    val lastItemPosition = messageAdapter.getLastItemPosition()
                                    messageRecyclerview.smoothScrollToPosition(lastItemPosition)
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }
                    }else{
                       messagereq()
                       runOnUiThread {
                           Toast.makeText(this,"Behave",Toast.LENGTH_SHORT).show()
                       }

                    }

                }

            }
        }
    }
    private fun uploadImagetoapi(imageUri: Uri, callback: (String) -> Unit) {
        val inputStream = contentResolver.openInputStream(imageUri)
        inputStream?.use { input ->
            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }

            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()
            val request = Request.Builder()
                .url("http://192.168.194.19:3001/analyze")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Log.d("Failed", e.stackTraceToString())
                        Toast.makeText(
                            this@ChatActivity,
                            "${e.stackTraceToString()} Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Invoke callback with empty string on failure
                        callback("")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    var responseBody = ""
                    runOnUiThread {
                        if (response.isSuccessful) {
                            responseBody = response.body!!.string()

                        } else {
                            Toast.makeText(
                                this@ChatActivity,
                                "Failed while sending",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // Invoke callback with response body
                        callback(responseBody)
                    }
                }
            })
        } ?: run {
            runOnUiThread {
                Toast.makeText(this@ChatActivity, " Failed", Toast.LENGTH_SHORT).show()
                // Invoke callback with empty string when input stream is null
                callback("")
            }
        }
    }


    private fun messagereq(){
        client= OkHttpClient()

        val JSON="application/json; charset=utf-8".toMediaType()
        val jsonBody="""
            {
            "phone_number":"+917738874661"
            }
        """.trimIndent()

        val request=Request.Builder()
            .url("http://192.168.194.19:3001/print_phone_number")
            .post(jsonBody.toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ErrorWhile Calling msg api", e.printStackTrace().toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    val responseBody=response.body?.string()
                    Log.d("API Response", responseBody ?: "Empty response body")
                } else {
                    Log.e("API Error", "Failed to make request: ${response.code}")
                }
            }
        })
    }

//    private fun uploadImagetoapi(imageUri: Uri):String {
//         var responsebody:String=""
//        val inputStream = contentResolver.openInputStream(imageUri)
//        inputStream?.use { input ->
//            val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
//            tempFile.outputStream().use { output ->
//                input.copyTo(output)
//            }
//            val mediaType = "image/jpeg".toMediaTypeOrNull()
//            val requestBody=MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("image",tempFile.name,
//                    tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
//                )
//                .build()
//            val request = Request.Builder()
//                .url("http://192.168.175.19:3001/analyze")
//                .post(requestBody)
//                .build()
//
//            client.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    runOnUiThread {
//
//                        Log.d("Failed",e.stackTraceToString())
//                        Toast.makeText(this@ChatActivity,"${e.stackTraceToString()} Failed",Toast.LENGTH_SHORT).show()
//                    }
//                }
//                override fun onResponse(call: Call, response: Response) {
//
//                    runOnUiThread {
//                        if(response.isSuccessful){
////                            Toast.makeText(this@ChatActivity,"$responsebody Done",Toast.LENGTH_SHORT).show()
//                            responsebody= response.body?.string().toString()
//                        }else{
//                            Toast.makeText(this@ChatActivity,"Failed while sending",Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            })
//        } ?: run {
//            runOnUiThread {
//                Toast.makeText(this@ChatActivity," Failed",Toast.LENGTH_SHORT).show()
//            }
//        }
//        return responsebody
//    }
}




