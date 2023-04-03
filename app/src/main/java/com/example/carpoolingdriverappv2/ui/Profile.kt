package com.example.carpoolingdriverappv2.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.*
import com.example.carpoolingdriverappv2.R
import com.example.carpoolingdriverappv2.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Profile : Fragment() {

    //View Binding
    private lateinit var binding: FragmentProfileBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    //Realtime Database
    private lateinit var database : FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //load user info for display
        loadUserInfo()

        //check user login or logout
        checkUser()

        //edit profile button
        binding.edtProfile.setOnClickListener {
//            val profileEdit = ProfileEdit()
//            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
//            transaction.replace(R.id.frame_layout, profileEdit)
//            transaction.commit()

            val intent = Intent(activity, EditProfileActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.verifyMyKad.setOnClickListener {
            val intent = Intent(activity, VerifyDocumentActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.testMapBtn.setOnClickListener {
//            val mapsFragment = MapsFragment()
//            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
//            transaction.replace(R.id.frame_layout, mapsFragment)
//            transaction.commit()
            val intent = Intent(activity, MapsActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.reportBtn.setOnClickListener {
            val intent = Intent(activity, ReportActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.fragmentActivity.setOnClickListener {
            val intent = Intent(activity, ViewExistingTripActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.faqBtn.setOnClickListener {
            val intent = Intent(activity, MessageActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.carInfo.setOnClickListener {
            val intent = Intent(activity, VerifyCarInfoActivity::class.java)
            activity?.startActivity(intent)
        }

        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            clearToken(FirebaseAuth.getInstance().currentUser!!.uid)
            firebaseAuth.signOut()
            checkUser()
        }

        binding.setting.setOnClickListener {
            val intent = Intent(activity, SettingActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.walletRL.setOnClickListener {
            val intent = Intent(activity, WalletActivity::class.java)
            activity?.startActivity(intent)
        }

        return binding.root
    }

    private fun clearToken(userId:String) {
        FirebaseDatabase.getInstance().getReference("Tokens").child(userId)
            .removeValue()
    }


    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phone = "${snapshot.child("phone").value}"
                    val studentId = "${snapshot.child("studentId").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val walletBal = "${snapshot.child("walletBal").value}"
//                    val rating = "${snapshot.child("rating").value}"

                    //set data
                    binding.profileNameTv.text = name
                    binding.studentIdTv.text = studentId
                    binding.walletTv.text = "RM%.2f".format(walletBal.toDouble())
//                    binding.ratingTv.text = "%.1f/5.0".format(rating.toDouble())

                    //set image
                    try {
                        Glide.with(this@Profile)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .into(binding.imgProfile)
                    }catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        ref.child(firebaseAuth.uid!!).child("rating")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val rating = "${snapshot.child("average").value}"

                    binding.ratingTv.text = "%.1f/5.0".format(rating.toDouble())

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun checkUser() {
        //check user is logged in or not

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            //user not null, user is logged in, get user info
            val email = firebaseUser.email
            //set to text view
            binding.emailTv.text = email

        }
        else{
            //user is null, user is not logged in
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            clearToken(FirebaseAuth.getInstance().currentUser!!.uid)
            val intent = Intent(activity, LoginActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }
    }


}