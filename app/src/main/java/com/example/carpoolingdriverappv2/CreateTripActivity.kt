package com.example.carpoolingdriverappv2

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.carpoolingdriverappv2.data.LocationInfo
import com.example.carpoolingdriverappv2.databinding.ActivityCreateTripBinding
import com.example.carpoolingdriverappv2.util.TripAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_create_trip.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CreateTripActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityCreateTripBinding

    //firebase auth
    private lateinit var firebaseAuth:FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    private lateinit var tripArrayList: ArrayList<TripModel>

    //date
    private lateinit var tvDatePicker : TextView
    private lateinit var btnDatePicker : Button

    //time
    private lateinit var tvTime : TextView
    private lateinit var btnTimePicker : Button

    private lateinit var locationInfoArrayList: ArrayList<String>

    private var frequency = ""
    private var customFrequency = ""
//    private val once = 1
//    private val weekly = 7
//    private val monthly = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dropdown list seats
        val seat = resources.getStringArray(R.array.seats)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, seat)
        binding.noOfSeatsTv.setAdapter(adapter)

//        val frequencySpinner: Spinner = findViewById(R.id.frequency_spinner)
//        val frequencyOptions = arrayOf("Once", "Weekly", "Monthly")
//        val frequencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencyOptions)
//        frequencySpinner.adapter = frequencyAdapter


//        frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                if (position == 0) {
//                    //If weekly or monthly can enter frequency
//                    customFrequencyEditText.visibility = View.GONE
//                    frequency = frequencySpinner.selectedItem.toString()
//                    customFrequency = customFrequencyEditText.text.toString()
//                } else {
//                    //If repeat once then no need frequency
//                    customFrequencyEditText.visibility = View.VISIBLE
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                frequency = "Once"
//                customFrequency = "1"
//            }
//        }

        //date and time picker
        tvDatePicker = findViewById(R.id.tripDate)
        btnDatePicker = findViewById(R.id.btnDatePicker)

        tvTime = findViewById(R.id.tripTime)
        btnTimePicker = findViewById(R.id.btnTimePicker)

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(myCalendar)
        }

        //init //firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadLocationInfo()

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
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

            TimePickerDialog(this, timePicker, currentTime.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), true).show()
        }


    }

    private fun loadTripSize() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Trips")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataSize = snapshot.childrenCount
                addTripFirebase(dataSize.toInt())

            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading data: ${error.message}")
            }
        })
    }

    private fun updateLabelTime(currentTime: Calendar) {

        val stf = SimpleDateFormat("HH:mm")
        tvTime.text = stf.format(currentTime.time)
    }

    private fun generateSequenceId(prefix: String, sequenceNumber: Int): String {
        return "${prefix}${sequenceNumber.toString().padStart(4, '0')}"
    }

    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
        tvDatePicker.text = sdf.format(myCalendar.time)
    }

    private var pickup = ""
    private var dropoff = ""
    private var tripTime = ""
    private var tripDate = ""
    private var noOfSeats =""
    private var tripDateTime = ""

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
                        loadTripSize()
                    }
                }
                else{
                    Toast.makeText(this, "Trip Time cannot same within 2 hours", Toast.LENGTH_SHORT).show()
                }

            }
        }

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

    private fun convertStringtoDateTime(tripDateTime: String): LocalDateTime? {
        //convert format from String to dateTime
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        return LocalDateTime.parse(tripDateTime, formatter)
    }

    //check whether the created trip is before current date or not -> return true if the trip is before
    private fun checkTripsPast(tripDateTime: String): Boolean{

        val now = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"))

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

        val dateTimeTrip = LocalDateTime.parse(tripDateTime, formatter)

        //return true if the trip has expired
        return dateTimeTrip.isBefore(now)
    }

    //Create the trip and upload it to firebase
    private fun addTripFirebase(dataSize: Int) {
        //show progress
        progressDialog.show()

        //get timestamp
        val random = generateSequenceId("", Random().nextInt(1000))
//        val timestamp = System.currentTimeMillis()
        val tripId = generateSequenceId("TRP$random", dataSize)
        val tripDateTime = "$tripDate, $tripTime"
        val faresBef = calculateFares(pickup, dropoff)
        val fares = String.format("%.2f",faresBef)
        //Status: Pending -> Completed / Cancelled
        val status = "Pending"
//        val tripDateTimeLDT = convertStringtoDateTime(tripDateTime)

        //setup data to add in firebase db
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = tripId
        hashMap["pickup"] = pickup
        hashMap["dropoff"] = dropoff
        hashMap["tripDate"] = tripDate
        hashMap["tripTime"] = tripTime
        hashMap["tripDateTime"] = tripDateTime
        hashMap["fares"] = fares
        hashMap["status"] = status
        hashMap["noOfSeats"] = noOfSeats
        hashMap["uid"] = "${firebaseAuth.uid}"

        val childHashMap = HashMap<String, Any>()
        if(noOfSeats.toInt() == 1){
            childHashMap["passenger1"] = "Empty Seat"
        }
        else if (noOfSeats.toInt() == 2){
            childHashMap["passenger1"] = "Empty Seat"
            childHashMap["passenger2"] = "Empty Seat"
        }
        else if (noOfSeats.toInt() == 3){
            childHashMap["passenger1"] = "Empty Seat"
            childHashMap["passenger2"] = "Empty Seat"
            childHashMap["passenger3"] = "Empty Seat"
        }
        else if (noOfSeats.toInt() == 4){
            childHashMap["passenger1"] = "Empty Seat"
            childHashMap["passenger2"] = "Empty Seat"
            childHashMap["passenger3"] = "Empty Seat"
            childHashMap["passenger4"] = "Empty Seat"
        }

        val ref = FirebaseDatabase.getInstance().getReference("Trips")
        ref.child(tripId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
//        val childRef = ref.child(tripId).child("passengerList")
//        childRef.setValue(childHashMap)
        val childRef = FirebaseDatabase.getInstance().getReference("PassengerTripList")
        childRef.child(tripId)
            .setValue(childHashMap)

        startActivity(Intent(this, ViewTripActivity::class.java))
        finish()
    }

    //LocationInfo
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

    //Load Location Name into dropdown list from firebase
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

                val arrayAdapter = ArrayAdapter(this@CreateTripActivity, R.layout.dropdown_item, locationInfoArrayList)
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