package com.example.carpoolingdriverappv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.carpoolingdriverappv2.data.TransactionModel
import com.example.carpoolingdriverappv2.databinding.ActivityViewTripDetailsBinding
import com.example.carpoolingdriverappv2.databinding.ActivityWalletBinding
import com.example.carpoolingdriverappv2.util.TransactionAdapter
import com.example.carpoolingdriverappv2.util.TripAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var transactionArrayList: ArrayList<TransactionModel>

    //adapter
    private lateinit var transactionAdapter: TransactionAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadUserInfo()
        loadTransaction()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.expandBtn.setOnClickListener{
            startActivity(Intent(this, ViewTransactionActivity::class.java))
        }

        binding.transferBtn.setOnClickListener {
            startActivity(Intent(this, TransferActivity::class.java))
        }

        binding.withdrawalBtn.setOnClickListener {
            startActivity(Intent(this, WithdrawalActivity::class.java))
        }

        binding.topupBtn.setOnClickListener {
            startActivity(Intent(this, TopupActivity::class.java))
        }

    }

    private fun loadTransaction() {
        //init arraylist
        transactionArrayList = ArrayList()
        //get all trips
        //val ref = FirebaseDatabase.getInstance().getReference("Trips")
        //only get the trip which created by the current user
        val ref = FirebaseDatabase.getInstance().getReference("Transactions")
            .orderByChild("driverUid").equalTo(firebaseAuth.currentUser?.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                transactionArrayList.clear()
                for (ds in snapshot.children){

                    //get data as model
                    val model = ds.getValue(TransactionModel::class.java)

                    //add to array list
                    transactionArrayList.add(model!!)
                }
                transactionArrayList.sortByDescending { convertStringtoDateTime(it.txnDateTime) }

                //setup adapter
                transactionAdapter = TransactionAdapter(this@WalletActivity, transactionArrayList)
                //set adapter to recycle view
                binding.txnRv.adapter = transactionAdapter

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun convertStringtoDateTime(tripDateTime: String): LocalDateTime? {
        //convert format from String to dateTime
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        return LocalDateTime.parse(tripDateTime, formatter)
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val walletBal = "${snapshot.child("walletBal").value}"

                    //set data
                    binding.walletTv.text = "RM%.2f".format(walletBal.toDouble())

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}