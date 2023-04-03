package com.example.carpoolingdriverappv2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.fragment_profile.*

class OcrTestActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var editText: TextView
    private lateinit var progressDialog: ProgressDialog


    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_test)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
//        loadDriverInfo()

        imageView = findViewById(R.id.imageView)
        editText = findViewById(R.id.editText)
    }

    fun selectImage(v: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

//    private fun uploadImage() {
//        progressDialog.setMessage("Verifying MyKad...")
//        progressDialog.show()
//
//        val filePathAndName = "MyKadImages/" + firebaseAuth.uid
//
//        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
//        reference.putFile(imageUri!!)
//            .addOnSuccessListener { taskSnapshot->
//                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
//                while (!uriTask.isSuccessful);
//                val uploadImageUrl = "${uriTask.result}"
//
//                updateMyKad(uploadImageUrl)
//
//            }
//            .addOnFailureListener{ e->
//                progressDialog.dismiss()
//                Toast.makeText(this, "Fail to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private var myKadNo = ""
    private var imageUri: Uri?= null

//    private fun updateMyKad(uploadedImageUri: String) {
//        progressDialog.setMessage("Uploading MyKad Info...")
//
//        val hashmap: HashMap<String, Any> = HashMap()
//        hashmap["myKadNo"] = "$myKadNo"
//        if (imageUri != null){
//            hashmap["myKadImage"] = uploadedImageUri
//        }
//
//        val reference = FirebaseDatabase.getInstance().getReference("DriverDocument")
//        reference.child(firebaseAuth.uid!!)
//            .updateChildren(hashmap)
//            .addOnSuccessListener {
//                progressDialog.dismiss()
//                Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {e->
//                progressDialog.dismiss()
//                Toast.makeText(this, "Fail to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()
//
//            }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
            }
            imageView.setImageURI(data!!.data)
        }
    }

    @SuppressLint("SetTextI18n")
    fun startRecognizing(v: View) {

        if (imageUri != null) {
            editText.text = ""
            v.isEnabled = false
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    v.isEnabled = true
                    val finalText = processResultText(firebaseVisionText)
                    editText.text = finalText
                    splitText(finalText)
                }
                .addOnFailureListener {
                    v.isEnabled = true
                    editText.text = "Failed"
                }
        }
        else {
            Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
        }
    }

    private fun splitText(finalText: String) {

        val rows = finalText.split("\n")

        // Extract the values after the colon in each row
        val pemunyaBerdaftar = rows[4].substringAfter(":").uppercase().trim()
        val noId = rows[3].substringAfter(":").trim()
        val namaModel = rows[5].substringAfter(":").uppercase().trim()

        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["ownerName"] = pemunyaBerdaftar
        hashmap["ownerId"] = noId
        hashmap["carModel"] = namaModel

//        if (imageUri != null){
//            hashmap["myKadImage"] = uploadedImageUri
//        }

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

        println(pemunyaBerdaftar) // Output: Choo Jun Sheng
        println(noId) // Output: 123456-12-1234
        println(namaModel) // Output: Proton Wira
    }

//    private fun validateMyKad(myKadNumber: String): Boolean {
//        val regex = Regex("^([0-9]{6}-[0-9]{2}-[0-9]{4})$")
//        return regex.matches(myKadNumber)
//    }

    @SuppressLint("SetTextI18n")
    private fun processResultText(resultText: FirebaseVisionText): String {
        if (resultText.textBlocks.size == 0) {
//            editText.text = "Error 404 Please Try Again"
            return "Error 404 Please Try Again"
        }
        val stringBuilder = StringBuilder()
        for (block in resultText.textBlocks) {
            val blockText = block.text
            stringBuilder.append("$blockText\n")
//            if (validateMyKad(blockText)){
//                myKadNo = blockText
//                uploadImage()
//                editText.append(blockText)
//            }
        }
        return stringBuilder.toString()
    }

//    private fun loadDriverInfo() {
//        val ref = FirebaseDatabase.getInstance().getReference("DriverDocument")
//        ref.child(firebaseAuth.uid!!)
//            .addValueEventListener(object: ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    //get user info
//                    val myKadNo = "${snapshot.child("myKadNo").value}"
//                    val myKadImage = "${snapshot.child("myKadImage").value}"
//
//                    //set data
//                    editText.text = myKadNo
//
//                    //set image
//                    try {
//                        Glide.with(this@OcrTestActivity)
//                            .load(myKadImage)
//                            .placeholder(R.drawable.ic_baseline_person_24)
//                            .into(imageView)
//                    }catch (e: Exception){
//
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//    }
}