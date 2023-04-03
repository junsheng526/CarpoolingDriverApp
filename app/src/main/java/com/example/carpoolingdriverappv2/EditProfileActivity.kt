package com.example.carpoolingdriverappv2

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.databinding.ActivityEditProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri?= null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        firebaseAuth = FirebaseAuth.getInstance()

        //load user info for display
        loadUserInfo()
        checkUser()

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.cancelBtn.setOnClickListener {
            onBackPressed()
        }

        //update profile pic
        binding.imgCamera.setOnClickListener {
            showImageAttachMenu()
        }

        //update profile details
        binding.updateBtn.setOnClickListener {
            validateData()
        }

    }

    private var phone = ""
    private var name = ""
    private var studentId = ""

    private fun validateData() {
        //get data
        phone = binding.edtPhone.text.toString().trim()
        name = binding.edtName.text.toString().uppercase().trim()
        studentId = binding.edtStudentId.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || studentId.isEmpty()){
            if (name.isEmpty()){
                binding.edtName.error = "Name cannot be empty"
            }
            if (phone.isEmpty()){
                binding.edtPhone.error = "Phone cannot be empty"
            }
            if (studentId.isEmpty()){
                binding.edtStudentId.error = "Student Id cannot be empty"
            }
        }
        else if (!isValidPhoneNumber(phone)){
            Toast.makeText(this, "Please follow the valid number (E.g. 0123456789)", Toast.LENGTH_SHORT).show()
        }
        else if (!isValidStudentId(studentId)){
            Toast.makeText(this, "Please follow the valid id (E.g. 1234567)", Toast.LENGTH_SHORT).show()
        }
        else{
            if (imageUri == null){
                updateProfile("")
            }
            else{
                uploadImage()
            }
        }
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
        if (studentId[0] != '2') {
            return false
        }
        for (i in studentId.indices){
            if (!Character.isDigit(studentId[i])){
                return false
            }
        }
        return true
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        val filePathAndName = "ProfileImages/" + firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadImageUrl = "${uriTask.result}"

                updateProfile(uploadImageUrl)


            }
            .addOnFailureListener{ e->
                progressDialog.dismiss()
                Toast.makeText(this, "Fail to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        //check user is logged in or not

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            //user not null, user is logged in, get user info
            val email = firebaseUser.email
            //set to text view
            binding.emailTv.text = email

        }
        else{
            //user is null, user is not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun updateProfile(uploadedImageUri: String) {
        progressDialog.setMessage("Updating profile...")

        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["phone"] = "$phone"
        hashmap["name"] = "$name"
        hashmap["studentId"] = "$studentId"
//        hashmap["password"] = "$password"
        if (imageUri != null){
            hashmap["profileImage"] = uploadedImageUri
        }

        val reference = FirebaseDatabase.getInstance().getReference("users")
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

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phone = "${snapshot.child("phone").value}"
                    val studentId = "${snapshot.child("studentId").value}"
//                    val password = "${snapshot.child("password").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    binding.edtName.setText(name)
                    binding.edtPhone.setText(phone)
                    binding.edtStudentId.setText(studentId)
//                    binding.edtPassword.setText(password)

                    //set image
                    try {
                        Glide.with(this@EditProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_baseline_person_24)
                            .into(binding.imgProfile)
                    }catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun showImageAttachMenu(){

        //setup popup menu
        val popupMenu = PopupMenu(this, binding.imgProfile)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item->
            //get id of clicked item
            val id = item.itemId
            if (id == 0){
                //camera clicked
                pickImageCamera()
            }
            else if (id==1){
                //gallery clicked
                pickImageGallery()
            }

            true
        }

    }

    private fun pickImageCamera() {
        //intent to pick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = this.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK){
                val data = result.data
//                imageUri = data!!.data

                //set to image view
                binding.imgProfile.setImageURI(imageUri)
            }
            else{
                //cancel
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun pickImageGallery() {
        //intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set to image view
                binding.imgProfile.setImageURI(imageUri)
            }
            else{
                //cancel
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}