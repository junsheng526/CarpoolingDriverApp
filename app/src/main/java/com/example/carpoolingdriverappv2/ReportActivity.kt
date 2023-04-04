package com.example.carpoolingdriverappv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.carpoolingdriverappv2.databinding.ActivityReportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding

    // Define variables to store the total fares, total fares this week, and total fares this month
    var totalFares = 0.0
    var totalFaresThisWeek = 0.0
    var totalFaresThisMonth = 0.0

    // Get the current week and month
    val calendar = Calendar.getInstance()
    val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadTotalIncome()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadTotalIncome() {
        val ref = FirebaseDatabase.getInstance().getReference("ExistingTripsRecord")
            .orderByChild("uid").equalTo(firebaseAuth.currentUser?.uid)

// Retrieve the fares from the Realtime Database and calculate the totals
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                totalFares = 0.0
                totalFaresThisWeek = 0.0
                totalFaresThisMonth = 0.0

                for (tripSnapshot in dataSnapshot.children) {
//                    val tripId = tripSnapshot.key
                    val faresSnapshot = tripSnapshot.child("fares")
                    val seatsSnapshot = tripSnapshot.child("noOfSeats")
                    val datesSnapshot = tripSnapshot.child("tripDateTime")
                    val fareString = faresSnapshot.getValue(String::class.java)
                    val seatString = seatsSnapshot.getValue(String::class.java)
                    val dateString = datesSnapshot.getValue(String::class.java)
                    val fare = fareString!!.toDouble()
                    val noOfSeats = seatString!!.toDouble()

                    val inputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val inputDate = inputFormat.parse(dateString)
                    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

                    val inputMonth = Calendar.getInstance().apply { time = inputDate }.get(Calendar.MONTH)

                    if (inputMonth == currentMonth) {
                        totalFaresThisMonth += (fare*noOfSeats)
                    }

                    // Add the fare to the total
                    totalFares += (fare*noOfSeats)

                }
                // Update the UI with the totals
                // (You'll need to implement this yourself based on your UI design)
                updateUIWithTotals(totalFares, totalFaresThisMonth)

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun updateUIWithTotals(totalFares: Double, totalFaresThisMonth: Double) {
        val faresTotal = String.format("RM%.2f",totalFares)
        val faresTotalMonth = String.format("RM%.2f",totalFaresThisMonth)
        binding.totalFaresTv.text = faresTotal
        binding.totalFaresMonthTv.text = faresTotalMonth
    }
}