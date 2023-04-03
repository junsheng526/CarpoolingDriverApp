package com.example.carpoolingdriverappv2

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.carpoolingdriverappv2.databinding.ActivityMyKadVerifyBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MyKadVerifyActivity : AppCompatActivity() {

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    lateinit var binding: ActivityMyKadVerifyBinding

    private val REQUEST_IMAGE_CAPTURE = 1

    private var imageBitmap: Bitmap?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_kad_verify)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_kad_verify)

        binding.apply {
            captureImage.setOnClickListener {
                takeImage()
                textView.text = ""
            }

            detectTextImageBtn.setOnClickListener {
                processImage()
            }
        }

    }

    private fun processImage() {
        if (imageBitmap!=null){
            val image = imageBitmap?.let {
                InputImage.fromBitmap(it, 0)
            }

            image?.let {
                recognizer.process(it)
                    .addOnSuccessListener {
                        binding.textView.text=it.text
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Nothing to show", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        else{
            Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takeImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)

        }
        catch (e:java.lang.Exception){

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            val extras: Bundle?=data?.extras

            imageBitmap=extras?.get("data") as Bitmap

            if (imageBitmap!=null){
                binding.imageView.setImageBitmap(imageBitmap)
            }

        }
    }
}