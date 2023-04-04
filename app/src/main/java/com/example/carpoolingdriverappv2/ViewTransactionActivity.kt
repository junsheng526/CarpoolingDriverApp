package com.example.carpoolingdriverappv2

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.example.carpoolingdriverappv2.data.TransactionModel
import com.example.carpoolingdriverappv2.databinding.ActivityViewTransactionBinding
import com.example.carpoolingdriverappv2.util.TransactionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_kad_verify.*
import kotlinx.android.synthetic.main.activity_view_transaction.view.*
import kotlinx.android.synthetic.main.filter_date_dialog.view.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ViewTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewTransactionBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var transactionArrayList: ArrayList<TransactionModel>

    //adapter
    private lateinit var transactionAdapter: TransactionAdapter


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadTransaction()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

//        binding.filterDateDialog.setOnClickListener {
//            val dialog = BottomSheetDialog(this)
//            val view = layoutInflater.inflate(R.layout.filter_date_dialog, null)
//
//            val close = view.findViewById<ImageView>(R.id.cancelBtn)
//            close.setOnClickListener {
//                dialog.dismiss()
//            }
//
//            dialog.setContentView(view)
//            dialog.show()
//
//
//            val todayFilter = view.findViewById<CardView>(R.id.todayFilterBtn)
//            todayFilter.setOnClickListener {
//
//            }
//            val yesterdayFilter = view.findViewById<CardView>(R.id.yesterdayFilter)
//            yesterdayFilter.setOnClickListener {
//
//            }
//
//        }

        binding.filterTxnDesc.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.filter_desc_dialog, null)

            val close = view.findViewById<ImageView>(R.id.cancelBtn)
            close.setOnClickListener {
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()

            val tripFaresFilter = view.findViewById<RelativeLayout>(R.id.filterByTripFaresDesc)
            val transferFilter = view.findViewById<RelativeLayout>(R.id.filterByTransfer)
            val withdrawalFilter = view.findViewById<RelativeLayout>(R.id.filterByWithdrawal)
            val topupFilter = view.findViewById<RelativeLayout>(R.id.topupFilter)
            val clearFilter = view.findViewById<RelativeLayout>(R.id.resetFilter)

            tripFaresFilter.setOnClickListener {
                transactionAdapter.filter.filter("Trip Fares")
                dialog.dismiss()
            }
            transferFilter.setOnClickListener {
                transactionAdapter.filter.filter("Transfer")
                dialog.dismiss()
            }
            withdrawalFilter.setOnClickListener {
                transactionAdapter.filter.filter("Withdrawal")
                dialog.dismiss()
            }
            topupFilter.setOnClickListener {
                transactionAdapter.filter.filter("Topup")
                dialog.dismiss()
            }

            clearFilter.setOnClickListener {
                transactionAdapter.filter.filter("")
                dialog.dismiss()
            }
        }

    }

    private fun loadTransaction() {
        //init arraylist
        transactionArrayList = ArrayList()

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
                transactionAdapter = TransactionAdapter(this@ViewTransactionActivity, transactionArrayList)
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

    private fun convertYesterdayToString(): String{
        val yesterday = LocalDate.now(ZoneId.of("Asia/Kuala_Lumpur")).minusDays(1)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return yesterday.format(formatter)
    }


}