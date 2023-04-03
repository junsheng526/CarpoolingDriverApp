package com.example.carpoolingdriverappv2.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.CreateTripActivity
import com.example.carpoolingdriverappv2.LoginActivity
import com.example.carpoolingdriverappv2.R
import com.example.carpoolingdriverappv2.ViewTripActivity
import com.example.carpoolingdriverappv2.databinding.FragmentCarpoolBinding
import com.example.carpoolingdriverappv2.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Carpool : Fragment() {

    private lateinit var binding: FragmentCarpoolBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCarpoolBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        firebaseAuth = FirebaseAuth.getInstance()

//        checkUser()
//        loadUserInfo()


        binding.createTripBtn.setOnClickListener {
            val intent = Intent(activity, CreateTripActivity::class.java)
            activity?.startActivity(intent)
        }
        binding.viewTripBtn.setOnClickListener {
            val intent = Intent(activity, ViewTripActivity::class.java)
            activity?.startActivity(intent)
        }
        return binding.root
    }

//    private fun loadUserInfo() {
//        val ref = FirebaseDatabase.getInstance().getReference("users")
//        ref.child(firebaseAuth.uid!!)
//            .addValueEventListener(object: ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    //get user info
//                    val email = "${snapshot.child("email").value}"
//                    val name = "${snapshot.child("name").value}"
//                    val phone = "${snapshot.child("phone").value}"
//                    val studentId = "${snapshot.child("studentId").value}"
//                    val profileImage = "${snapshot.child("profileImage").value}"
//
//                    //set data
//                    binding.profileNameTv.text = name
//                    binding.studentIdTv.text = studentId
//
//                    try {
//                        Glide.with(this@Carpool)
//                            .load(profileImage)
//                            .placeholder(R.drawable.ic_baseline_person_24)
//                            .into(binding.imgProfile)
//                    }catch (e: Exception){
//
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//    }

//    private fun checkUser() {
//        //check user is logged in or not
//
//        val firebaseUser = firebaseAuth.currentUser
//        if (firebaseUser != null){
//            //user not null, user is logged in, get user info
//            val email = firebaseUser.email
//            //set to text view
//            binding.emailTv.text = email
//
//        }
//        else{
//            //user is null, user is not logged in
////            startActivity(Intent(this, LoginActivity::class.java))
////            finish()
//            val intent = Intent(activity, LoginActivity::class.java)
//            activity?.startActivity(intent)
//            activity?.finish()
//        }
//    }

}