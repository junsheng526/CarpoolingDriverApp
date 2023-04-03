package com.example.carpoolingdriverappv2.notification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.carpoolingdriverappv2.R
import com.example.carpoolingdriverappv2.databinding.ActivityNotificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/myTopic"

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    val TAG = "Notification"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val token:String? = task.result

                    val userId: String = FirebaseAuth.getInstance().currentUser!!.uid
                    FirebaseDatabase.getInstance()
                        .getReference("Tokens")
                        .child(userId)
                        .setValue(token)
                }
            }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)


        binding.btnSend.setOnClickListener{
            val title = "Trip Ended"
            val message = "Please complete the payment now!"
            val recipientToken = binding.etToken.text.toString()
            if(title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()){
                PushNotification(
                    NotificationData(title,message),
                    recipientToken
                ).also {
                    sendNotification(it)
                }
            }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful){
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            }else{
                Log.e(TAG, response.errorBody().toString())
            }
        }catch (e:Exception){
            Log.e(TAG, e.toString())
        }
    }
}
