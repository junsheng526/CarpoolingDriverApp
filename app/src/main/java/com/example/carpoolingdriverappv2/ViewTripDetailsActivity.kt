package com.example.carpoolingdriverappv2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carpoolingdriverappv2.databinding.ActivityViewTripDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ViewTripDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTripDetailsBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private var pickup = ""
    private var dropoff = ""
    private var tripDate = ""
    private var tripTime = ""
    private var noOfSeats = 0
    private var noOfSeatsStr = ""
    private var tripDateTime = ""
    private var fares = ""
    private var tripId = ""
    private var uid = ""
    private var passengerName1 = ""
    private var passengerName2 = ""
    private var passengerName3 = ""
    private var passengerName4 = ""
    private var passengerUid1 = ""
    private var passengerUid2 = ""
    private var passengerUid3 = ""
    private var passengerUid4 = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getStringExtra("tripId")!!
        pickup = intent.getStringExtra("pickup")!!
        dropoff = intent.getStringExtra("dropoff")!!
        tripDate = intent.getStringExtra("tripDate")!!
        tripTime = intent.getStringExtra("tripTime")!!
        noOfSeatsStr = intent.getStringExtra("noOfSeats")!!
        fares = intent.getStringExtra("fares")!!
        uid = intent.getStringExtra("uid")!!
        tripDateTime = intent.getStringExtra("tripDateTime")!!

        noOfSeats = noOfSeatsStr.toInt()



        firebaseAuth = FirebaseAuth.getInstance()
//        loadTripInfo()
        loadPassengerInfo()

        binding.editTripBtn.setOnClickListener {
            val intent = Intent(this, EditTripActivity::class.java)
            intent.putExtra("tripId", tripId)
            intent.putExtra("pickup", pickup)
            intent.putExtra("dropoff", dropoff)
            intent.putExtra("tripDate", tripDate)
            intent.putExtra("tripTime", tripTime)
            intent.putExtra("noOfSeats", noOfSeatsStr)
            intent.putExtra("fares", fares)


            startActivity(intent)
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

//        binding.testBtn.setOnClickListener {
//            val intent = Intent(this, GenerateTripInvoice::class.java)
//            intent.putExtra("tripId", tripId)
//            intent.putExtra("pickup", pickup)
//            intent.putExtra("dropoff", dropoff)
//            intent.putExtra("tripDateTime", tripDateTime)
//            intent.putExtra("tripDate", tripDate)
//            intent.putExtra("tripTime", tripTime)
//            intent.putExtra("fares", fares)
//            intent.putExtra("uid", uid)
//            intent.putExtra("passengerName1", passengerName1)
//            intent.putExtra("passengerName2", passengerName2)
//            intent.putExtra("passengerName3", passengerName3)
//            intent.putExtra("passengerName4", passengerName4)
//
//            startActivity(intent)
//            finish()
//        }

        binding.endTripBtn.setOnClickListener {
            endTrip(tripDateTime)

//            val intent = Intent(this, GenerateTripInvoice::class.java)
//            intent.putExtra("tripId", tripId)
//            intent.putExtra("pickup", pickup)
//            intent.putExtra("dropoff", dropoff)
//            intent.putExtra("tripDateTime", tripDateTime)
//            intent.putExtra("tripDate", tripDate)
//            intent.putExtra("tripTime", tripTime)
//            intent.putExtra("fares", fares)
//            intent.putExtra("uid", uid)
//            intent.putExtra("passengerName1", passengerName1)
//            intent.putExtra("passengerName2", passengerName2)
//            intent.putExtra("passengerName3", passengerName3)
//            intent.putExtra("passengerName4", passengerName4)
//
//            startActivity(intent)
//            finish()
        }

    }

    private fun checkTripDateTime(tripDateTime: String): Boolean{

        //check current date
        val now = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"))

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

        val dateTimeTrip = LocalDateTime.parse(tripDateTime, formatter)

        //return true if the trip is in future
        return dateTimeTrip.isAfter(now)
    }

    private fun loadPassengerInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("PassengerTripList")
        ref.child(tripId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val passenger1UID = "${snapshot.child("passenger1").value}"
                    passengerUid1 = passenger1UID
                    loadPassenger1Name(passengerUid1)

                    val passenger2UID = "${snapshot.child("passenger2").value}"
                    passengerUid2 = passenger2UID
                    loadPassenger2Name(passengerUid2)

                    val passenger3UID = "${snapshot.child("passenger3").value}"
                    passengerUid3 = passenger3UID
                    loadPassenger3Name(passengerUid3)

                    val passenger4UID = "${snapshot.child("passenger4").value}"
                    passengerUid4 = passenger4UID
                    loadPassenger4Name(passengerUid4)
                }

                override fun onCancelled(error: DatabaseError) {}

            })

    }

    private fun loadPassenger1Name(passengerUid:String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(passengerUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("username").value}"
                    if (name != "null"){
                        passengerName1 = name.uppercase()
                    }
                    else{
                        passengerName1 = "Empty Seat"
                    }
                    binding.psg1Tv.text = passengerName1
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadPassenger2Name(passengerUid:String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(passengerUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("username").value}"
                    if (name != "null"){
                        passengerName2 = name.uppercase()
                    }
                    else{
                        passengerName2 = "Empty Seat"
                    }
                    binding.psg2Tv.text = passengerName2
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadPassenger3Name(passengerUid:String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(passengerUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("username").value}"
                    if (name != "null"){
                        passengerName3 = name.uppercase()
                    }
                    else{
                        passengerName3 = "Empty Seat"
                    }
                    binding.psg3Tv.text = passengerName3
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadPassenger4Name(passengerUid:String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(passengerUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("username").value}"
                    if (name != "null"){
                        passengerName4 = name.uppercase()
                    }
                    else{
                        passengerName4 = "Empty Seat"
                    }
                    binding.psg4Tv.text = passengerName4
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private fun endTrip(tripDateTime: String) {

        addTripToBookedTrip()
        addTripToRecord()
        val ref = FirebaseDatabase.getInstance().getReference("Trips")
        ref.child(tripId)
            .removeValue()

//        if (checkTripDateTime(tripDateTime)){
//            Toast.makeText(this, "Please end the trip after it not before!", Toast.LENGTH_SHORT).show()
//        }
//        else {
//            addTripToRecord()
//            addTripToBookedTrip()
//            val ref = FirebaseDatabase.getInstance().getReference("Trips")
//            ref.child(tripId)
//                .removeValue()
//
//            val intent = Intent(this, GenerateTripInvoice::class.java)
//            intent.putExtra("tripId", tripId)
//            intent.putExtra("pickup", pickup)
//            intent.putExtra("dropoff", dropoff)
//            intent.putExtra("tripDateTime", tripDateTime)
//            intent.putExtra("tripDate", tripDate)
//            intent.putExtra("tripTime", tripTime)
//            intent.putExtra("fares", fares)
//            intent.putExtra("uid", uid)
//            intent.putExtra("passengerName1", passengerName1)
//            intent.putExtra("passengerName2", passengerName2)
//            intent.putExtra("passengerName3", passengerName3)
//            intent.putExtra("passengerName4", passengerName4)
//
//            startActivity(intent)
//            finish()
//        }

        val intent = Intent(this, GenerateTripInvoice::class.java)
        intent.putExtra("tripId", tripId)
        intent.putExtra("pickup", pickup)
        intent.putExtra("dropoff", dropoff)
        intent.putExtra("tripDateTime", tripDateTime)
        intent.putExtra("tripDate", tripDate)
        intent.putExtra("tripTime", tripTime)
        intent.putExtra("fares", fares)
        intent.putExtra("uid", uid)
        intent.putExtra("passengerName1", passengerName1)
        intent.putExtra("passengerName2", passengerName2)
        intent.putExtra("passengerName3", passengerName3)
        intent.putExtra("passengerName4", passengerName4)

        startActivity(intent)
        finish()
    }

    private fun addTripToRecord() {
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = tripId
        hashMap["pickup"] = pickup
        hashMap["dropoff"] = dropoff
        hashMap["tripDate"] = tripDate
        hashMap["tripTime"] = tripTime
        hashMap["tripDateTime"] = tripDateTime
        hashMap["noOfSeats"] = noOfSeats.toString()
        hashMap["uid"] = uid
        hashMap["fares"] = fares
        hashMap["status"] = "Completed"

        val ref = FirebaseDatabase.getInstance().getReference("ExistingTripsRecord")
        ref.child(tripId)
            .setValue(hashMap)
    }

    private fun addTripToBookedTrip() {
        val status = "Completed"

        val ref = FirebaseDatabase.getInstance().getReference("BookedTrips")
        ref.child(tripId).child("status")
            .setValue(status)
    }


//    private fun loadTripInfo() {
//        val ref = FirebaseDatabase.getInstance().getReference("Trips")
//        ref.child(tripId)
//            .addValueEventListener(object: ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    //get trip info
//                    val id = "${snapshot.child("id").value}"
//                    val tripDate = "${snapshot.child("tripDate").value}"
//                    val tripTime = "${snapshot.child("tripTime").value}"
//                    val fares = "${snapshot.child("fares").value}"
//                    val pickup = "${snapshot.child("pickup").value}"
//                    val dropoff = "${snapshot.child("dropoff").value}"
//
//                    //set data
//                    binding.tripIdTv.text = "TRIP ID: $id"
//                    binding.tripDate.text = tripDate
//                    binding.tripTime.text = tripTime
//                    binding.faresTv.text = "RM%.2f Per Pax".format(fares.toDouble())
//                    binding.pickupTv.text = pickup
//                    binding.dropoffTv.text = dropoff
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//
//            })
//    }
}