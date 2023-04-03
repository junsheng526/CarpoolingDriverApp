package com.example.carpoolingdriverappv2

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.carpoolingdriverappv2.databinding.ActivityWithdrawalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WithdrawalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawalBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var driverId = ""
    private var driverWalletBal = 0.0

    private var amount = 0.0

    private lateinit var progressDialog: ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Withdrawing...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadDriverInfo()

        binding.transferBtn.setOnClickListener {
            validateAmount()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


    }

    private fun loadDriverInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
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
                Toast.makeText(this@WithdrawalActivity, "No enough balance, cannot withdraw", Toast.LENGTH_SHORT).show()
            }
            else{
                transferAmount()
            }
        }
    }

    private fun transferAmount() {
        deduceDriver(amount)
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

//    private fun generateSequenceId(prefix: String, sequenceNumber: Int): String {
//        return "${prefix}${sequenceNumber.toString().padStart(6, '0')}"
//    }

    private fun updateTransaction(newTxnId: String) {
        progressDialog.show()

        val driverUid = driverId
        val passengerUid = "-"
        val txnId = newTxnId
        val fares = "%.2f".format(amount)
        val txnDesc = "Withdrawal"
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

    private fun isValidFare(input: String): Boolean {
        val fareRegex = Regex("^\\d+(\\.\\d{1,2})?$") //regex pattern for fares with up to 2 decimal places
        return fareRegex.matches(input)
    }
}