package com.example.carpoolingdriverappv2.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.*
import com.example.carpoolingdriverappv2.data.ExistingTripsRecordModel
import com.example.carpoolingdriverappv2.databinding.ActivityViewTripBinding
import com.example.carpoolingdriverappv2.databinding.FragmentHomeBinding
import com.example.carpoolingdriverappv2.databinding.FragmentProfileBinding
import com.example.carpoolingdriverappv2.util.TripAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold trips
    private lateinit var tripArrayList: ArrayList<TripModel>

    private lateinit var existingTripArrayList: ArrayList<ExistingTripsRecordModel>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadUserInfo()
        loadTrips()
        loadExistingTrips()

        binding.upcomingTripBtn.setOnClickListener{
            val intent = Intent(activity, ViewTripActivity::class.java)
            activity?.startActivity(intent)
        }

        binding.existingTripBtn.setOnClickListener {
            val intent = Intent(activity, ViewExistingTripActivity::class.java)
            activity?.startActivity(intent)
        }


        return binding.root
    }

    private fun loadTrips() {
        //init arraylist
        tripArrayList = ArrayList()
        //get all trips
        //val ref = FirebaseDatabase.getInstance().getReference("Trips")
        //only get the trip which created by the current user

        val ref = FirebaseDatabase.getInstance().getReference("Trips")
            .orderByChild("uid").equalTo(firebaseAuth.currentUser?.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                tripArrayList.clear()
                for (ds in snapshot.children){
                    //get data as model
                    val model = ds.getValue(TripModel::class.java)
                    //add to array list
                    tripArrayList.add(model!!)
                }

                if (tripArrayList.isNotEmpty()){
                    //sort the arraylist by ascending order so the nearest time will be arraylist[0]
                    tripArrayList.sortBy { convertStringtoDateTime(it.tripDateTime) }

                    val model = tripArrayList[0]
                    val id = model.id
                    val tripDate = model.tripDate
                    val tripTime = model.tripTime
                    val pickup = model.pickup
                    val dropoff = model.dropoff

                    binding.pickupTv.text = pickup
                    binding.dropoffTv.text = dropoff
                    binding.tripDateTv.text = tripDate
                    binding.tripTimeTv.text = tripTime
                    binding.tripIdTv.text = id
                }
                else{
                    binding.pickupTv.text = "No Trip Yet"
                    binding.dropoffTv.text = "No Trip Yet"
                    binding.tripDateTv.text = "-"
                    binding.tripTimeTv.text = "-"
                    binding.tripIdTv.text = "No Trip Yet"
                }



            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun convertStringtoDateTime(tripDateTime: String): LocalDateTime? {
        //convert format from String to dateTime
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        return LocalDateTime.parse(tripDateTime, formatter)
    }

    private fun loadExistingTrips() {
        //init arraylist
        existingTripArrayList = ArrayList()
        //get all trips
        //val ref = FirebaseDatabase.getInstance().getReference("Trips")
        //only get the trip which created by the current user
        val ref = FirebaseDatabase.getInstance().getReference("ExistingTripsRecord")
            .orderByChild("uid").equalTo(firebaseAuth.currentUser?.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                existingTripArrayList.clear()
                for (ds in snapshot.children){
                    //get data as model
                    val model = ds.getValue(ExistingTripsRecordModel::class.java)
                    //add to array list
                    existingTripArrayList.add(model!!)
                }

                if (existingTripArrayList.isNotEmpty()){
                    //sort the arraylist by ascending order so the nearest time will be arraylist[0]
                    existingTripArrayList.sortByDescending { convertStringtoDateTime(it.tripDateTime) }

                    val model = existingTripArrayList[0]
                    val id = model.id
                    val tripDate = model.tripDate
                    val tripTime = model.tripTime
                    val pickup = model.pickup
                    val dropoff = model.dropoff

                    binding.existingPickup.text = pickup
                    binding.existingDropoff.text = dropoff
                    binding.existingTripDate.text = tripDate
                    binding.existingTripTime.text = tripTime
                    binding.existingTripId.text = id
                }
                else{
                    binding.existingPickup.text = "No Trip Yet"
                    binding.existingDropoff.text = "No Trip Yet"
                    binding.existingTripDate.text = "-"
                    binding.existingTripTime.text = "-"
                    binding.existingTripId.text = "No Trip Yet"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
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

                    //set data
                    binding.profileNameTv.text = name
                    try {
                        Glide.with(this@Home)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .into(binding.imgProfile)
                    }catch (e: Exception){

                    }
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
//            val email = firebaseUser.email
            //set to text view
//            binding.emailTv.text = email

        }
        else{
            //user is null, user is not logged in
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
            val intent = Intent(activity, LoginActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }
    }

}