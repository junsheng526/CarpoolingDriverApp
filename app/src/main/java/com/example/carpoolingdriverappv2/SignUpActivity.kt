package com.example.carpoolingdriverappv2

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.carpoolingdriverappv2.data.Users
import com.example.carpoolingdriverappv2.databinding.ActivitySignUpBinding
import com.example.carpoolingdriverappv2.util.TripAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SignUpActivity : AppCompatActivity() {

    //ViewBinding
    private lateinit var binding: ActivitySignUpBinding
    //ProgressDialog
    private lateinit var progressDialog: ProgressDialog
    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password = ""


    //Realtime Database
    private lateinit var database : FirebaseDatabase
    private var name = ""
    private var phone = ""
    private var studentId = ""
    private var walletBal = 0.0
    private var profileImage = ""
    private var rating = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Creating Account...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //handle click, open signup activity
        binding.haveAccountTv.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        //handle click, begin signup
        binding.signUpBtn.setOnClickListener {
            //validate data
            validateData()
        }

    }

    private fun retrieveAndStoreToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid

                    FirebaseDatabase.getInstance().getReference("Tokens").child(userId)
                        .setValue(token)

                }
            }
    }

    private fun validateData() {
        //get data
        name = binding.edtName.text.toString().trim()
        phone = binding.edtPhone.text.toString().trim()
        studentId = binding.edtStudentId.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()


        //validate data
        if (name.isEmpty() || phone.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()){
            if (name.isEmpty()){
                binding.edtName.error = "Name cannot be empty"
            }
            if (phone.isEmpty()){
                binding.edtPhone.error = "Phone cannot be empty"
            }
            if (studentId.isEmpty()){
                binding.edtStudentId.error = "Student Id cannot be empty"
            }
            if (email.isEmpty()){
                binding.emailEt.error = "Email cannot be empty"
            }
            if (password.isEmpty()){
                binding.passwordEt.error = "Password cannot be empty"
            }
        }
        else if (!isValidPhoneNumber(phone)){
            binding.edtPhone.error = "Invalid phone format"
        }
        else if (!isValidStudentId(studentId)){
            binding.edtStudentId.error = "Invalid Student ID format"
        }
        else if (!isValidTarumtEmail(email)){
            binding.emailEt.error = "Invalid email format/please use tarumt email"
        }
        else if (password.length < 6){
            //password length is less than 6
            binding.passwordEt.error = "Password must at least 6 characters long"
        }
        else{
            //data is valid, continue signup
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        //show progress
        progressDialog.show()
        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                val databaseRef = database.reference.child("users").child(firebaseAuth.currentUser!!.uid)
                val users : Users = Users(name, phone, studentId, email, walletBal, rating, profileImage)

                databaseRef.setValue(users).addOnCompleteListener {
                    if (it.isSuccessful){
                        progressDialog.dismiss()
                        //get current user
                        val firebaseUser = firebaseAuth.currentUser
                        val email = firebaseUser!!.email
                        Toast.makeText(this, "Account created with email $email", Toast.LENGTH_SHORT).show()

                        retrieveAndStoreToken()
                        //open profile
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }else{
                        progressDialog.dismiss()
                        Toast.makeText(this, "SignUp Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }else {
                try {
                    throw it.exception!!
                } catch (e: FirebaseAuthUserCollisionException) {
                    // email already in use
                    Toast.makeText(applicationContext, "Email already taken!", Toast.LENGTH_SHORT)
                        .show()
                }
                progressDialog.dismiss()
                Toast.makeText(this, "SignUp Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun isValidPhoneNumber(phone: String):Boolean {
        if (phone.length < 10 || phone.length > 11){
            return false
        }
        if (phone[0] != '0') {
            return false
        }
        if (phone[1] != '1'){
            return false
        }
        for (i in phone.indices){
            if (!Character.isDigit(phone[i])){
                return false
            }
        }
        return true
    }

    private fun isValidStudentId(studentId: String):Boolean {
        if (studentId.length != 7){
            return false
        }

        for (i in studentId.indices){
            if (!Character.isDigit(studentId[i])){
                return false
            }
        }
        return true
    }

    private fun isValidTarumtEmail(email: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return false
        }
        if (!email.contains("@student.tarc.edu.my")){
            return false
        }
        return true
    }

}