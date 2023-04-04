package com.example.carpoolingdriverappv2

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.carpoolingdriverappv2.databinding.ActivityReportBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding

    // Define variables to store the total fares, total fares this week, and total fares this month
    var totalFares = 0.0
    var totalFaresThisMonth = 0.0

    lateinit var pieChart: PieChart

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

    private fun loadPieChart(totalFares: Double, totalFaresThisMonth: Double) {

        val thisMonthPercentage = (this.totalFaresThisMonth / this.totalFares) * 100
        val othersPercentage = 100 - thisMonthPercentage

        pieChart = findViewById(R.id.pieChart)

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        // on below line we are setting drag for our pie chart
        pieChart.dragDecelerationFrictionCoef = 0.95f

        // on below line we are setting hole
        // and hole color for pie chart
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(R.color.white)

        // on below line we are setting circle color and alpha
        pieChart.setTransparentCircleColor(R.color.white)
        pieChart.setTransparentCircleAlpha(110)

        // on  below line we are setting hole radius
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        // on below line we are setting center text
        pieChart.setDrawCenterText(true)

        // on below line we are setting
        // rotation for our pie chart
        pieChart.rotationAngle = 0f

        // enable rotation of the pieChart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        // on below line we are setting animation for our pie chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // on below line we are disabling our legend for pie chart
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(R.color.white)
        pieChart.setEntryLabelTextSize(12f)

        // on below line we are creating array list and
        // adding data to it to display in pie chart
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(thisMonthPercentage.toFloat()))
        entries.add(PieEntry(othersPercentage.toFloat()))
//        entries.add(PieEntry(10f))

        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "")

        // on below line we are setting icons.
        dataSet.setDrawIcons(false)

        // on below line we are setting slice for pie
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(resources.getColor(R.color.purple_200))
        colors.add(resources.getColor(R.color.Cyan))

        // on below line we are setting colors.
        dataSet.colors = colors

        // on below line we are setting pie data set
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(R.color.white)
        pieChart.data = data

        // undo all highlights
        pieChart.highlightValues(null)

        // loading chart
        pieChart.invalidate()
    }

    private fun loadTotalIncome() {
        val ref = FirebaseDatabase.getInstance().getReference("ExistingTripsRecord")
            .orderByChild("uid").equalTo(firebaseAuth.currentUser?.uid)

// Retrieve the fares from the Realtime Database and calculate the totals
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                totalFares = 0.0
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
                loadPieChart(totalFares, totalFaresThisMonth)

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