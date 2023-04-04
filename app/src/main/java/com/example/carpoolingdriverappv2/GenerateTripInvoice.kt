package com.example.carpoolingdriverappv2

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.carpoolingdriverappv2.databinding.ActivityGenerateTripInvoiceBinding
import com.example.carpoolingdriverappv2.databinding.ActivityViewTripDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream

class GenerateTripInvoice : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateTripInvoiceBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private var pickup = ""
    private var dropoff = ""
    private var tripDate = ""
    private var tripTime = ""
    private var tripDateTime = ""
    private var fares = ""
    private var tripId = ""
    private var uid = ""
    private val STORAGE_CODE = 1001
    private var passengerName = ""
    private var driverName = ""
    private var passengerUid = ""
    private var passengerName1 = ""
    private var passengerName2 = ""
    private var passengerName3 = ""
    private var passengerName4 = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateTripInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getStringExtra("tripId")!!
        pickup = intent.getStringExtra("pickup")!!
        dropoff = intent.getStringExtra("dropoff")!!
        tripDate = intent.getStringExtra("tripDate")!!
        tripTime = intent.getStringExtra("tripTime")!!
        fares = intent.getStringExtra("fares")!!
        uid = intent.getStringExtra("uid")!!
        tripDateTime = intent.getStringExtra("tripDateTime")!!

        passengerName1 = intent.getStringExtra("passengerName1")!!
        passengerName2 = intent.getStringExtra("passengerName2")!!
        passengerName3 = intent.getStringExtra("passengerName3")!!
        passengerName4 = intent.getStringExtra("passengerName4")!!

        val psgNameArray = arrayOf(passengerName1, passengerName2, passengerName3, passengerName4)
        val arrayAdapter = ArrayAdapter(this@GenerateTripInvoice, R.layout.dropdown_item, psgNameArray)
        binding.psgName.setAdapter(arrayAdapter)

        firebaseAuth = FirebaseAuth.getInstance()

        setTripInfo()
        loadPassengerInfo()
        loadDriverInfo()

        binding.pdfBtn.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, STORAGE_CODE)
            }
            else{
                savePDF()
            }
        }

        binding.viewBtn.setOnClickListener {
            if (passengerName == "" || passengerName == "NULL"){
                Toast.makeText(this, "Please select a passenger name!", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this@GenerateTripInvoice, ViewPdfActivity::class.java)
                intent.putExtra("passengerName", passengerName)
                intent.putExtra("tripId", tripId)
                startActivity(intent)
            }

        }

        binding.homeBtn.setOnClickListener {
            val intent = Intent(this@GenerateTripInvoice, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.sendBtn.setOnClickListener {
            val mFileName = "$tripId-$passengerName"

            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + mFileName + ".pdf"
            val file = File(filePath)


            val uri = FileProvider.getUriForFile(applicationContext, "com.example.carpoolingdriverappv2.provider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share PDF file"))
        }
    }

    private fun savePDF() {

//        loadPassengerName()

        passengerName = binding.psgName.text.toString()

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(500, 500, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()

// Add text
        paint.textSize = 20f
        canvas.drawText("TARUMT INVOICE", 50f, 50f, paint)

// Add a rectangle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(50f, 100f, 450f, 400f, paint)

// Add more text
        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        canvas.drawText("TRIP ID", 53f, 150f, paint)
        canvas.drawText(tripId, 300f, 150f, paint)

        canvas.drawText("PICKUP", 53f, 180f, paint)
        canvas.drawText(pickup, 300f, 180f, paint)

        canvas.drawText("DROPOFF", 53f, 210f, paint)
        canvas.drawText(dropoff, 300f, 210f, paint)

        canvas.drawText("DATE AND TIME", 53f, 240f, paint)
        canvas.drawText(tripDateTime, 300f, 240f, paint)

        canvas.drawText("TRIP FARES", 53f, 270f, paint)
        canvas.drawText(fares, 300f, 270f, paint)

        canvas.drawText("TRANSFER FROM", 53f, 300f, paint)
        canvas.drawText(passengerName, 300f, 300f, paint)

        canvas.drawText("TRANSFER TO", 53f, 330f, paint)
        canvas.drawText(driverName, 300f, 330f, paint)

// ...

// Add a footer
        paint.textSize = 12f
        canvas.drawText("Thank you and have a nice day!", 50f, 450f, paint)

        pdfDocument.finishPage(page)

        try {
            val mFileName = "$tripId-$passengerName"

            val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + mFileName + ".pdf"
            val file = File(filePath)

            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)

            pdfDocument.close()
            fileOutputStream.close()

            Toast.makeText(this, "Download successfully!", Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception){
            Toast.makeText(this, "Download failed due to $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                savePDF()
            }
            else{
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPassengerInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("PassengerTripList")
        ref.child(tripId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get trip info
                    val passenger1UID = "${snapshot.child("passenger1").value}"
                    passengerUid = passenger1UID
                    loadPassengerName()

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


    }

    private fun loadPassengerName(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(passengerUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get trip info
                    val name = "${snapshot.child("username").value}"
                    passengerName = name.uppercase()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadDriverInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(uid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get trip info
                    val name = "${snapshot.child("name").value}"
                    driverName = name
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun setTripInfo() {
        binding.tripIdTv.text = tripId
        binding.tripDateTime.text = tripDateTime
        binding.faresTv.text = fares
        binding.pickupTv.text = pickup
        binding.dropoffTv.text = dropoff
    }
}