package com.example.carpoolingdriverappv2.verifyDocumentFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.carpoolingdriverappv2.databinding.FragmentBackICBinding
import com.example.carpoolingdriverappv2.databinding.FragmentSelfieBinding
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

class SelfieFragment : Fragment() {

    //View Binding
    private lateinit var binding: FragmentSelfieBinding

    private lateinit var progressDialog: ProgressDialog

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelfieBinding.inflate(inflater, container, false)


        firebaseAuth = FirebaseAuth.getInstance()

        binding.selfieBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }


        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        return binding.root
    }

    private fun uploadImage() {
        progressDialog.setMessage("Verifying Selfie...")
        progressDialog.show()

        val filePathAndName = "SelfieImages/" + firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"

                updateSelfie(uploadImageUrl)

            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Fail to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var imageUri: Uri?= null

    private fun updateSelfie(uploadedImageUri: String) {
        progressDialog.setMessage("Uploading Selfie...")

        val hashmap: HashMap<String, Any> = HashMap()
        if (imageUri != null){
            hashmap["selfieImage"] = uploadedImageUri
        }

        val reference = FirebaseDatabase.getInstance().getReference("DriverDocument")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Update successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Fail to update profile due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                imageUri = data.data
                uploadImage()
            }
        }
    }

//    private fun startRecognizing() {
//
//        if (imageUri != null) {
//            uploadImage()
//        }
//        else {
//            Toast.makeText(requireContext(), "Select an Image First", Toast.LENGTH_LONG).show()
//        }
//    }

}