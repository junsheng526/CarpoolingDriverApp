package com.example.carpoolingdriverappv2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_verify_car_info.*

class VerifyCarInfoActivity : AppCompatActivity() {

    lateinit var carGrantTv: TextView
    private lateinit var progressDialog: ProgressDialog


    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri?= null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_car_info)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadUserInfo()
        loadCarInfo()

        carGrantTv = findViewById(R.id.carGrantTv)

        backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    fun selectImage(v: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
            }
//            imageView.setImageURI(data!!.data)
        }
    }

    @SuppressLint("SetTextI18n")
    fun startRecognizing(v: View) {

        if (imageUri != null) {
            carGrantTv.text = ""
            v.isEnabled = false

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    v.isEnabled = true
                    val finalText = processResultText(firebaseVisionText)
                    carGrantTv.text = finalText
                    splitText(finalText)
                    uploadImage()
                }
                .addOnFailureListener {
                    v.isEnabled = true
                    carGrantTv.text = "Failed"
                }
        }
        else {
            Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
        }
    }

    private var ownerName = ""
    private var ownerId = ""
    private var carModel = ""
    private var carPlate = ""

    private fun splitText(finalText: String) {
        val rows = finalText.split("\n")

        // Extract the values after the colon in each row
        ownerName = rows[4].substringAfter(":").uppercase().trim()
        ownerId = rows[3].substringAfter(":").trim()
        carModel = rows[5].substringAfter(":").uppercase().trim()
        carPlate = rows[2].substringAfter(":").uppercase().trim()

    }

    private fun updateCarInfo(uploadedImageUri: String){

        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["ownerName"] = ownerName
        hashmap["ownerId"] = ownerId
        hashmap["carModel"] = carModel
        hashmap["carPlate"] = carPlate

        if (imageUri != null){
            hashmap["carGrantImage"] = uploadedImageUri
        }

        val reference = FirebaseDatabase.getInstance().getReference("CarInfo")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Fail to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun processResultText(resultText: FirebaseVisionText): String {
        if (resultText.textBlocks.size == 0) {
            return "Error 404 Please Try Again"
        }
        val stringBuilder = StringBuilder()
        for (block in resultText.textBlocks) {
            val blockText = block.text
            stringBuilder.append("$blockText\n")
        }
        return stringBuilder.toString()
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"

                    //set data
                    greetingTv.text = "HEY $name!"

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun uploadImage() {
        progressDialog.setMessage("Verifying Car Grant...")
        progressDialog.show()

        val filePathAndName = "CarGrantImages/" + firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"

                updateCarInfo(uploadImageUrl)

            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(this, "Fail to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCarInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("CarInfo")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val ownerId = "${snapshot.child("ownerId").value}"
                    val ownerName = "${snapshot.child("ownerName").value}"
                    val carModel = "${snapshot.child("carModel").value}"
                    val carPlate = "${snapshot.child("carPlate").value}"

                    //set data
                    carGrantTv.text = "Name: $ownerName\n" +
                            "No.Id: $ownerId\n" +
                            "Car Model: $carModel\n" +
                            "Car Plate: $carPlate"

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }


}