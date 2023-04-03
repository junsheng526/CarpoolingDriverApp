package com.example.carpoolingdriverappv2.verifyDocumentFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.MapsFragment
import com.example.carpoolingdriverappv2.R
import com.example.carpoolingdriverappv2.databinding.FragmentBackICBinding
import com.example.carpoolingdriverappv2.databinding.FragmentFrontICBinding
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
import kotlinx.android.synthetic.main.fragment_front_i_c.*

class BackICFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentBackICBinding

    private lateinit var progressDialog: ProgressDialog

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBackICBinding.inflate(inflater, container, false)


        firebaseAuth = FirebaseAuth.getInstance()
        loadDriverInfo()
        loadUserInfo()

        binding.photoBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }

        binding.verifyBtn.setOnClickListener {
            startRecognizing()
        }


        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        return binding.root
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("name").value}"

                    //set data
                    binding.greetingTv.text = "HEY $name!"

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun uploadImage() {


        val filePathAndName = "MyKadBackImages/" + firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"

                updateMyKad(uploadImageUrl)

            }
            .addOnFailureListener{ e->
                Toast.makeText(requireContext(), "Fail to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var myKadBackNo = ""
    private var imageUri: Uri?= null

    private fun updateMyKad(uploadedImageUri: String) {
//        progressDialog.setMessage("Uploading MyKad Info...")

        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["myKadBackNo"] = "$icNumber"
        if (imageUri != null){
            hashmap["myKadBack"] = uploadedImageUri
        }

        val reference = FirebaseDatabase.getInstance().getReference("DriverDocument")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Update successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
//                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Fail to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
            }
//            binding.imageView.setImageURI(data!!.data)
        }
    }

    private var icNumber = ""

    private fun splitText(finalText: String) {

        val myKadRegex = Regex("([0-9]{6}-[0-9]{2}-[0-9]{4}-[0-9]{2})")
        val textString = binding.myKadNoTv.text.toString()
        val matchResult = myKadRegex.find(textString)
        icNumber = matchResult?.groupValues?.get(1).toString()

    }

    private fun startRecognizing() {
        progressDialog.setMessage("Verifying MyKad...")
        progressDialog.show()
        if (imageUri != null) {
            binding.myKadNoTv.text = ""
//            val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->

                    val finalText = processResultText(firebaseVisionText)
                    binding.myKadNoTv.text = finalText
                    splitText(finalText)
                    uploadImage()
//                    progressDialog.dismiss()
                }
                .addOnFailureListener {
                    binding.myKadNoTv.text = "Failed"
                    progressDialog.dismiss()
                }
        }
        else {
            Toast.makeText(requireContext(), "Select an Image First", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        }
    }

    private fun validateMyKad(myKadNumber: String): Boolean {
        val regex = Regex("^([0-9]{6}-[0-9]{2}-[0-9]{4}-[0-9]{2}-[0-9]{2})$")
        return regex.matches(myKadNumber)
    }

//    @SuppressLint("SetTextI18n")
    private fun processResultText(resultText: FirebaseVisionText): String {
        if (resultText.textBlocks.size == 0) {
//            binding.myKadNoTv.text = "Error 404 Please Try Again"
            return "Error 404 Please Try Again"
        }
        val stringBuilder = StringBuilder()
        for (block in resultText.textBlocks) {
            val blockText = block.text
            stringBuilder.append("$blockText\n")
        }
        return stringBuilder.toString()
//        for (block in resultText.textBlocks) {
//            val blockText = block.text
//            if (validateMyKad(blockText)){
//                myKadBackNo = blockText
//                uploadImage()
//                binding.myKadNoTv.append(blockText)
//            }
//        }
    }

    private fun loadDriverInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("DriverDocument")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val myKadBackNo = "${snapshot.child("myKadBackNo").value}"
                    val myKadBack = "${snapshot.child("myKadBack").value}"

                    //set data
                    binding.myKadNoTv.text = myKadBackNo
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }




}