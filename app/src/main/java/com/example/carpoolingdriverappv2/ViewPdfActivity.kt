package com.example.carpoolingdriverappv2

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_view_pdf.*
import java.io.File


class ViewPdfActivity : AppCompatActivity() {

    private var passengerName = ""
    private var tripId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pdf)

        tripId = intent.getStringExtra("tripId")!!
        passengerName = intent.getStringExtra("passengerName")!!

        backBtn.setOnClickListener {
            onBackPressed()
        }


        val pdfImageView = findViewById<ImageView>(R.id.pdfImageView)

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        val mFileName = "$tripId-$passengerName"
        val pdfFile = File("$path/$mFileName.pdf")
        val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))

        val page = pdfRenderer.openPage(0)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfImageView.setImageBitmap(bitmap)
        page.close()
        pdfRenderer.close()

    }
}