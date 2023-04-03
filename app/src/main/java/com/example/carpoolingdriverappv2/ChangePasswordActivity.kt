package com.example.carpoolingdriverappv2

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_change_password.*

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth :FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        btnChangePw.setOnClickListener {
            changePassword()
        }
        backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun changePassword() {
        if (edtCurrentPw.text.isNotEmpty() &&
                edtNewPw.text.isNotEmpty() &&
                edtRetypePw.text.isNotEmpty()){
            if (edtNewPw.text.toString() == edtRetypePw.text.toString()){
                val user = auth.currentUser
                if (user != null && user.email != null){
                    val credential = EmailAuthProvider.getCredential(user.email!!, edtCurrentPw.text.toString())
                    user?.reauthenticate(credential)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(this, "Re-authentication successful", Toast.LENGTH_SHORT).show()
                                user?.updatePassword(edtNewPw.text.toString())
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful){
                                            Toast.makeText(this, "Change password successful", Toast.LENGTH_SHORT).show()
                                            auth.signOut()
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish()
                                        }
                                    }
                            }
                            else{
                                Toast.makeText(this, "Re-authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                else{
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            else{
                Toast.makeText(this, "Password mismatching", Toast.LENGTH_SHORT).show()
            }
        }
    }
}