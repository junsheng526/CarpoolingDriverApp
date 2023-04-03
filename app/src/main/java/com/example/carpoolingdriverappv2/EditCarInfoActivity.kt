package com.example.carpoolingdriverappv2

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.carpoolingdriverappv2.databinding.ActivityEditCarInfoBinding
import com.example.carpoolingdriverappv2.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditCarInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCarInfoBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Saving car details...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.saveBtn.setOnClickListener {
            validateData()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.cancelBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun validateData() {
        //get data
        carModel = binding.edtCarModel.text.toString().trim()
        carNoPlate = binding.edtCarPlate.text.toString().trim()
        carColor = binding.edtCarColor.text.toString().trim()

        //validate data
        if (carColor.isEmpty() || carModel.isEmpty() || carNoPlate.isEmpty()){
            if (carModel.isEmpty()){
                binding.edtCarModel.error = "Car model cannot be empty"
            }
            if (carColor.isEmpty()){
                binding.edtCarColor.error = "Car color cannot be empty"
            }
            if (carNoPlate.isEmpty()){
                binding.edtCarPlate.error = "Car No plate cannot be empty"
            }
        }

        else if (carNoPlate.length < 6){
            //password length is less than 6
            binding.edtCarPlate.error = "Please enter the valid car plate"
        }
        else{
            //data is valid, continue signup
            addCarFirebase()
        }
    }

    private var carModel = ""
    private var carNoPlate = ""
    private var carColor = ""

    private fun addCarFirebase() {
        //show progress
        progressDialog.show()

        //setup data to add in firebase db
        val hashMap = HashMap<String, Any>()
        hashMap["carModel"] = carModel
        hashMap["carNoPlate"] = carNoPlate
        hashMap["carColor"] = carColor
        hashMap["uid"] = "${firebaseAuth.uid}"


        val ref = FirebaseDatabase.getInstance().getReference("Cars")
        ref.child("${firebaseAuth.uid}")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}