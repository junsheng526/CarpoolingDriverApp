package com.example.carpoolingdriverappv2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.data.LocationInfo
import com.example.carpoolingdriverappv2.databinding.ActivityEditTripBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.protobuf.Value
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EditTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTripBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "TRIP_EDIT_TAG"
    }

    private var tripId = ""

    private lateinit var progressDialog: ProgressDialog

    //date
    private lateinit var tvDatePicker : TextView
    private lateinit var btnDatePicker : Button

    //time
    private lateinit var tvTime : TextView
    private lateinit var btnTimePicker : Button

    private lateinit var locationInfoArrayList: ArrayList<String>


    private val locations = arrayOf(
        LocationInfo("","TARUMT GATE 2", 3.213657, 101.726999),
        LocationInfo("","TARUMT MAIN GATE", 3.215021, 101.728418),
        LocationInfo("","TARUMT HOSTEL", 3.217382, 101.732491),
        LocationInfo("","PV128", 3.200647, 101.717896),
        LocationInfo("","PV16", 3.201927, 101.717246),
        LocationInfo("","PV15", 3.202295, 101.717021),
        LocationInfo("","PV10", 3.204056, 101.714990),
        LocationInfo("","PV12", 3.206772, 101.717333),
        LocationInfo("","PRIMA SETAPAK", 3.197373, 101.710913)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dropdown list location
//        val location = resources.getStringArray(R.array.location)
//        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, location)
//        binding.pickupLocation.setAdapter(arrayAdapter)
//        binding.dropoffLocation.setAdapter(arrayAdapter)

        //dropdown list seats
        val seat = resources.getStringArray(R.array.seats)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, seat)
        binding.noOfSeatsTv.setAdapter(adapter)

        firebaseAuth = FirebaseAuth.getInstance()

        //date and time picker
        tvDatePicker = findViewById(R.id.tripDate)
        btnDatePicker = findViewById(R.id.btnDatePicker)

        tvTime = findViewById(R.id.tripTime)
        btnTimePicker = findViewById(R.id.btnTimePicker)

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(myCalendar)
        }

        // bring forward the data
        tripId = intent.getStringExtra("tripId")!!
        pickup = intent.getStringExtra("pickup")!!
        dropoff = intent.getStringExtra("dropoff")!!
        tripDate = intent.getStringExtra("tripDate")!!
        tripTime = intent.getStringExtra("tripTime")!!
        noOfSeats = intent.getStringExtra("noOfSeats")!!
        fares = intent.getStringExtra("fares")!!


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadTripInfo()
        loadLocationInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.cancelBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

        binding.btnDatePicker.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnTimePicker.setOnClickListener {
            val currentTime = Calendar.getInstance()

            val timePicker = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                currentTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                currentTime.set(Calendar.MINUTE, minute)
                updateLabelTime(currentTime)
            }

            TimePickerDialog(this, timePicker, currentTime.get(Calendar.HOUR_OF_DAY), myCalendar.get(
                Calendar.MINUTE), true).show()
        }
    }

    private fun loadTripInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Trips")
        ref.child(tripId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get trip info
                    val id = "${snapshot.child("id").value}"
                    val tripDate = "${snapshot.child("tripDate").value}"
                    val tripTime = "${snapshot.child("tripTime").value}"
                    val fares = "${snapshot.child("fares").value}"


                    //set data
                    binding.tripIdTv.text = id
                    binding.tripDate.text = tripDate
                    binding.tripTime.text = tripTime
                    binding.faresTv.text = fares

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun updateLabelTime(currentTime: Calendar) {
        val stf = SimpleDateFormat("HH:mm")
        tvTime.text = stf.format(currentTime.time)
    }

    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
        tvDatePicker.text = sdf.format(myCalendar.time)
    }

    private var pickup = ""
    private var dropoff = ""
    private var tripDate = ""
    private var tripTime = ""
    private var noOfSeats = ""
    private var tripDateTime = ""
    private var fares = ""

    private fun validateData() {
        pickup = binding.pickupLocation.text.toString().trim()
        dropoff = binding.dropoffLocation.text.toString().trim()
        tripTime = binding.tripTime.text.toString().trim()
        tripDate = binding.tripDate.text.toString().trim()
        noOfSeats = binding.noOfSeatsTv.text.toString().trim()
        tripDateTime = "$tripDate, $tripTime"

        val faresBef = calculateFares(pickup, dropoff)
        binding.faresTv.text = String.format("RM%.2f",faresBef)

        if (tripTime == "HH:mm" || tripDate == "dd MMM yyyy"){
            if (tripDate == "dd MMM yyyy"){
                Toast.makeText(this, "Please select trip date", Toast.LENGTH_SHORT).show()
            }
            else if (tripTime == "HH:mm"){
                Toast.makeText(this, "Please select trip time", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            validateTripDateTime(tripDateTime){ isValid ->
                if (isValid){
                    if (pickup.isEmpty() || dropoff.isEmpty() || noOfSeats.isEmpty()){
                        if (pickup.isEmpty()){
                            Toast.makeText(this, "Please select pickup point", Toast.LENGTH_SHORT).show()
                        }
                        else if (dropoff.isEmpty()){
                            Toast.makeText(this, "Please select dropoff point", Toast.LENGTH_SHORT).show()
                        }
                        else if (noOfSeats.isEmpty()){
                            Toast.makeText(this, "Please select no of empty seats", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else if (pickup == dropoff){
                        Toast.makeText(this, "Pickup and dropoff point cannot be same", Toast.LENGTH_SHORT).show()
                    }
                    else if (checkTripsPast(tripDateTime)){
                        Toast.makeText(this, "Trip date and time cannot before the current time", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        updateTrip()
                    }
                }
                else{
                    Toast.makeText(this, "Trip Time cannot same within 2 hours", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }

    private fun convertStringtoDateTime(tripDateTime: String): LocalDateTime? {
        //convert format from String to dateTime
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        return LocalDateTime.parse(tripDateTime, formatter)
    }

    private fun validateTripDateTime(tripDateTime: String, onResult: (Boolean) -> Unit) {
        // Retrieve all trips created by the current user from the database
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("Trips")
            .orderByChild("uid").equalTo(uid)
        val tripList = mutableListOf<LocalDateTime>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val tripDateTimeStr = ds.child("tripDateTime").value as String
                    val tripDateTime = convertStringtoDateTime(tripDateTimeStr)
                    tripList.add(tripDateTime!!)
                }
                // Convert input trip datetime to LocalDateTime object
                val inputTripDateTime = convertStringtoDateTime(tripDateTime)

                // Check for clashes with existing trips within 2 hours
                for (trip in tripList) {
                    val timeDifference = Duration.between(trip, inputTripDateTime).abs()
                    if (timeDifference.toHours() <= 2) {
                        onResult(false)
                        return
                    }
                }

                // If there are no clashes, return true
                onResult(true)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(false)
            }
        })
    }

//    private fun validateData() {
//        pickup = binding.pickupLocation.text.toString().trim()
//        dropoff = binding.dropoffLocation.text.toString().trim()
//        tripDate = binding.tripDate.text.toString().trim()
//        tripTime = binding.tripTime.text.toString().trim()
//        noOfSeats = binding.noOfSeatsTv.text.toString().trim()
//        tripDateTime = "$tripDate, $tripTime"
//        val faresBef = calculateFares(pickup, dropoff)
//        binding.faresTv.text = String.format("RM%.2f",faresBef)
//
//        if (pickup.isEmpty() || dropoff.isEmpty() || tripTime == "Time" || tripDate == "Date" || noOfSeats.isEmpty()){
//            if (pickup.isEmpty()){
//                Toast.makeText(this, "Please select pickup point", Toast.LENGTH_SHORT).show()
//            }
//            else if (dropoff.isEmpty()){
//                Toast.makeText(this, "Please select dropoff point", Toast.LENGTH_SHORT).show()
//            }
//            else if (tripDate == "Date"){
//                Toast.makeText(this, "Please select trip date", Toast.LENGTH_SHORT).show()
//            }
//            else if (tripTime == "Time"){
//                Toast.makeText(this, "Please select trip time", Toast.LENGTH_SHORT).show()
//            }
//            else if (noOfSeats.isEmpty()){
//                Toast.makeText(this, "Please select no of empty seats", Toast.LENGTH_SHORT).show()
//            }
//        }
//        else if (pickup == dropoff){
//            Toast.makeText(this, "Pickup and dropoff point cannot be same", Toast.LENGTH_SHORT).show()
//        }
//        else if (checkTripsPast(tripDateTime)){
//            Toast.makeText(this, "Trip date and time cannot before the current time", Toast.LENGTH_SHORT).show()
//        }
//
//
//        else{
//            updateTrip()
//        }
//    }

    private fun checkTripsPast(tripDateTime: String): Boolean{

        //check current date
        val now = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

        val dateTimeTrip = LocalDateTime.parse(tripDateTime, formatter)

        //return true if the trip has expired
        return dateTimeTrip.isBefore(now)
    }

    private fun updateTrip() {
        Log.d(TAG, "Starting updating trip info...")

        progressDialog.setMessage("Updating trip info")
        progressDialog.show()

        val faresBef = calculateFares(pickup, dropoff)
        val fares = String.format("%.2f",faresBef)

        val hashMap = HashMap<String, Any>()
        hashMap["pickup"] = "$pickup"
        hashMap["dropoff"] = "$dropoff"
        hashMap["tripDate"] = "$tripDate"
        hashMap["tripTime"] = "$tripTime"
        hashMap["noOfSeats"] = "$noOfSeats"
        hashMap["fares"] = fares

        val ref = FirebaseDatabase.getInstance().getReference("Trips")
        ref.child(tripId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "Update successfully")
                Toast.makeText(this,"Update successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //Calculate fares by distance
    private fun calculateFares(pickup: String, dropoff: String): Double{
        var lat1 = 0.0
        var lon1 = 0.0
        var lat2 = 0.0
        var lon2 = 0.0

        for (i in locations.indices) {
            val location = locations[i]
            if (location.name == pickup){
                lat1 = location.latitude
                lon1 = location.longitude
            }
            if (location.name == dropoff){
                lat2 = location.latitude
                lon2 = location.longitude
            }
        }

        val FARES_RATE = 1.5

        val distance = calculateDistance(lat1, lon1, lat2, lon2)

        return FARES_RATE * distance

    }

    private fun loadLocationInfo(){
        //init arraylist
        locationInfoArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("LocationInfo")
        ref.child("id")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                locationInfoArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.child("name").getValue(String::class.java)
                    //add to array list
                    locationInfoArrayList.add(model!!)
                }

                val arrayAdapter = ArrayAdapter(this@EditTripActivity, R.layout.dropdown_item, locationInfoArrayList)
                binding.pickupLocation.setAdapter(arrayAdapter)
                binding.dropoffLocation.setAdapter(arrayAdapter)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    //Calculate distance by two point (Lat, Lng)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth's radius in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }


}