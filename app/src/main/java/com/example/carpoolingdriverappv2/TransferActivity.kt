package com.example.carpoolingdriverappv2

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.databinding.ActivityTransferBinding
import com.example.carpoolingdriverappv2.databinding.ActivityWalletBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TransferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var name = ""
    private var driverId = ""
    private var receiverId = ""
    private var driverWalletBal = 0.0
    private var receiverWalletBal = 0.0

    private var amount = 0.0

    private lateinit var progressDialog: ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Transferring...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadDriverInfo()

        binding.findBtn.setOnClickListener {
            validateData()
            binding.txtUsername.text = "Transfer to: $name"

        }

        binding.transferBtn.setOnClickListener {
            validateAmount()
//            transferAmount()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


    }

    private fun loadDriverInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val userId = "${firebaseAuth.uid}"
                    val name = "${snapshot.child("name").value}"
                    val walletBal = "${snapshot.child("walletBal").value}"

                    driverId = userId
                    driverWalletBal = walletBal.toDouble()


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun validateAmount() {
        val amountStr = binding.edtAmount.text.toString().trim()
        if (amountStr.isEmpty()){
            binding.edtAmount.error = "Amount cannot be empty"
        }
        else if (!isValidFare(amountStr)){
            binding.edtAmount.error = "Amount only can be integer or decimal!"
        }
        else {
            amount = amountStr.toDouble()
            if (amount > driverWalletBal){
                Toast.makeText(this@TransferActivity, "Low balance, cannot transfer", Toast.LENGTH_SHORT).show()
            }
            else{
                transferAmount()
            }
        }
    }

    private fun transferToUser(amount: Double) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiverId)
            .addValueEventListener(object: ValueEventListener{
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newBal = receiverWalletBal + amount
                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                    ref.child(receiverId).child("wallet")
                        .setValue(newBal)
                }
                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private fun transferAmount() {
        deduceDriver(amount)
        transferToUser(amount)
        loadTransaction()
    }

    private fun loadTransaction() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Transactions")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataSize = snapshot.childrenCount
                if (dataSize.toInt() != 0){
                    generateId()
                }
                else{
                    updateTransaction("TXN000000")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading data: ${error.message}")
            }
        })
    }

//    private fun generateSequenceId(prefix: String, sequenceNumber: Int): String {
//        return "${prefix}${sequenceNumber.toString().padStart(6, '0')}"
//    }

    private var newTxnId = ""

    private fun generateId(){
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var lastSequenceNumber = 0
                for (transactionSnapshot in dataSnapshot.children) {
                    val transactionID = transactionSnapshot.key
                    if (transactionID != null && transactionID.startsWith("TXN")) {
                        val sequenceNumber = transactionID.substring(3).toIntOrNull()
                        if (sequenceNumber != null && sequenceNumber > lastSequenceNumber) {
                            lastSequenceNumber = sequenceNumber
                        }
                    }
                }
                val nextSequenceNumber = lastSequenceNumber + 1
                val paddedSequenceNumber = String.format("%06d", nextSequenceNumber)
                newTxnId = "TXN$paddedSequenceNumber"
                updateTransaction(newTxnId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateTransaction(newTxnId: String) {
        progressDialog.show()

        val driverUid = driverId
        val passengerUid = receiverId
        val txnId = newTxnId
        val fares = "%.2f".format(amount)
        val txnDesc = "Transfer"
        val now = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"))
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        val txnDateTime = now.format(formatter)

        val hashMap = HashMap<String, Any>()
        hashMap["txnId"] = txnId
        hashMap["fares"] = fares
        hashMap["txnDesc"] = txnDesc
        hashMap["txnDateTime"] = txnDateTime
        hashMap["driverUid"] = driverUid
        hashMap["passengerUid"] = passengerUid

        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
        ref.child(txnId)
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

    private fun deduceDriver(amount: Double) {
        val newBal = driverWalletBal - amount
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(driverId).child("walletBal")
            .setValue(newBal)

    }

    private fun validateData() {
        val phone = binding.edtPhone.text.toString().trim()

        if (phone.isEmpty()) {
            binding.edtPhone.error = "Phone cannot be empty"
        }
        else if (!isValidPhoneNumber(phone)){
            binding.edtPhone.error = "Invalid phone format"
        }
        else {
            loadReceiverNameByPhone(phone)
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

    //return receiver name
    private fun loadReceiverNameByPhone(phone: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("mobileNo").equalTo(phone)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // The phone number exists in the database
                    for (userSnapshot in snapshot.children) {
                        val username = userSnapshot.child("username").getValue(String::class.java)
//                        val userId = userSnapshot.child("userId").getValue(String::class.java)
                        val walletBal = userSnapshot.child("wallet").getValue(Double::class.java)
                        name = username!!
//                        receiverId = userId!!
                        receiverWalletBal = walletBal!!
                    }
                } else {
                    Toast.makeText(this@TransferActivity, "Find user Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun isValidFare(input: String): Boolean {
        val fareRegex = Regex("^\\d+(\\.\\d{1,2})?$") //regex pattern for fares with up to 2 decimal places
        return fareRegex.matches(input)
    }
}