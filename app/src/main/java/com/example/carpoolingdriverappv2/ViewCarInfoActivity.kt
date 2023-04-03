package com.example.carpoolingdriverappv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.databinding.ActivityCreateTripBinding
import com.example.carpoolingdriverappv2.databinding.ActivityViewCarInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewCarInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewCarInfoBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


        loadCarInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.editCarBtn.setOnClickListener {
            startActivity(Intent(this, EditCarInfoActivity::class.java))
//            finish()
        }
        binding.testCarGrant.setOnClickListener {
            startActivity(Intent(this, VerifyCarInfoActivity::class.java))
        }

    }

    private fun loadCarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Cars")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val carModel = "${snapshot.child("carModel").value}"
                    val carColor = "${snapshot.child("carColor").value}"
                    val carPlate = "${snapshot.child("carNoPlate").value}"

                    //set data
                    binding.carModelTv.text = carModel
                    binding.carColorTv.text = carColor
                    binding.carNoPlateTv.text = carPlate


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}