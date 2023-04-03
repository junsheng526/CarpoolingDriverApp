package com.example.carpoolingdriverappv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.carpoolingdriverappv2.data.ExistingTripsRecordModel
import com.example.carpoolingdriverappv2.databinding.ActivityViewExistingTripBinding
import com.example.carpoolingdriverappv2.databinding.ActivityViewTripBinding
import com.example.carpoolingdriverappv2.util.ExistingTripAdapter
import com.example.carpoolingdriverappv2.util.TripAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ViewExistingTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewExistingTripBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var existingTripArrayList: ArrayList<ExistingTripsRecordModel>

    //adapter
    private lateinit var existingTripAdapter: ExistingTripAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewExistingTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadTrips()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //search
        binding.searchEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try{
                    existingTripAdapter.filter.filter(s)
                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

    }

    private fun convertStringtoDateTime(tripDateTime: String): LocalDateTime? {
        //convert format from String to dateTime
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        return LocalDateTime.parse(tripDateTime, formatter)
    }

    private fun loadTrips() {
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

                //can set as button later on
                //filter by ascending or descending
                existingTripArrayList.sortByDescending { convertStringtoDateTime(it.tripDateTime) }

                //setup adapter
                existingTripAdapter = ExistingTripAdapter(this@ViewExistingTripActivity, existingTripArrayList)
                //set adapter to recycle view
                binding.tripRv.adapter = existingTripAdapter


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}