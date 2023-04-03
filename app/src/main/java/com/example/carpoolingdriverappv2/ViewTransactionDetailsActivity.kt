package com.example.carpoolingdriverappv2

import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.carpoolingdriverappv2.databinding.ActivityViewTransactionBinding
import com.example.carpoolingdriverappv2.databinding.ActivityViewTransactionDetailsBinding
import com.example.carpoolingdriverappv2.databinding.ActivityViewTripDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream

class ViewTransactionDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTransactionDetailsBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var txnId = ""
    private var  txnDesc = ""
    private var txnDateTime = ""
    private var fares = ""
    private var driverUid = ""
    private var passengerUid = ""
    private val STORAGE_CODE = 1001
    private var senderName = ""
    private var receiverName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTransactionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txnId = intent.getStringExtra("txnId")!!
        txnDesc = intent.getStringExtra("txnDesc")!!
        txnDateTime = intent.getStringExtra("txnDateTime")!!
        fares = intent.getStringExtra("fares")!!
        driverUid = intent.getStringExtra("driverUid")!!
        passengerUid = intent.getStringExtra("passengerUid")!!

        binding.faresTv.text = fares
        binding.txnDescTv.text = txnDesc
        binding.txnDateTimeTv.text = txnDateTime
        binding.txnIdTv.text = txnId

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.pdfBtn.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, STORAGE_CODE)
            }
            else{
                savePDF()
            }
        }

    }

    private fun savePDF() {
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
        canvas.drawText("TRANSACTION ID", 53f, 150f, paint)
        canvas.drawText(txnId, 300f, 150f, paint)

        canvas.drawText("DESCRIPTION", 53f, 180f, paint)
        canvas.drawText(txnDesc, 300f, 180f, paint)

        canvas.drawText("DATE AND TIME", 53f, 210f, paint)
        canvas.drawText(txnDateTime, 300f, 210f, paint)

        canvas.drawText("TRANSACTION AMOUNT", 53f, 240f, paint)
        canvas.drawText(fares, 300f, 240f, paint)

        canvas.drawText("TRANSFER FROM", 53f, 270f, paint)
        canvas.drawText(senderName, 300f, 270f, paint)

        canvas.drawText("TRANSFER TO", 53f, 300f, paint)
        canvas.drawText(receiverName, 300f, 300f, paint)

// ...

// Add a footer
        paint.textSize = 12f
        canvas.drawText("Thank you and have a nice day!", 50f, 450f, paint)

        pdfDocument.finishPage(page)

        try {
            val mFileName = txnId

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

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(passengerUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get trip info
                    val name = "${snapshot.child("username").value}"
                    senderName = name

                    binding.senderTv.text = name.uppercase()

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        val ref2 = FirebaseDatabase.getInstance().getReference("users")
        ref2.child(driverUid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get trip info
                    val name = "${snapshot.child("name").value}"
                    receiverName = name
                    binding.receiverTv.text = name
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}