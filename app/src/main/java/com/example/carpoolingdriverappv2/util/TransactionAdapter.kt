package com.example.carpoolingdriverappv2.util

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carpoolingdriverappv2.TripModel
import com.example.carpoolingdriverappv2.ViewTransactionDetailsActivity
import com.example.carpoolingdriverappv2.data.TransactionModel
import com.example.carpoolingdriverappv2.databinding.RowTransactionBinding

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.HolderTransaction>, Filterable {

    private val context: Context
    public var transactionArrayList: ArrayList<TransactionModel>
    private var filterList: ArrayList<TransactionModel>

    private var filterDesc: FilterTransaction? = null

    constructor(context: Context, transactionArrayList: ArrayList<TransactionModel>) {
        this.context = context
        this.transactionArrayList = transactionArrayList
        this.filterList = transactionArrayList
    }

    private lateinit var binding: RowTransactionBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionAdapter.HolderTransaction {
        //inflate bind row_trip
        binding = RowTransactionBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderTransaction(binding.root)
    }

    override fun onBindViewHolder(holder: TransactionAdapter.HolderTransaction, position: Int) {
        //get set data handle click

        //get data
        val model = transactionArrayList[position]
        val txnId = model.txnId
        val txnDesc = model.txnDesc
        val txnDateTime = model.txnDateTime
        val fares = model.fares
        val driverUid = model.driverUid
        val passengerUid = model.passengerUid

        //set data
//        holder.tripTv.text = trip
        holder.txnDescTv.text = txnDesc
        holder.txnDateTimeTv.text = txnDateTime
        holder.faresTv.text = fares


        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewTransactionDetailsActivity::class.java)
            intent.putExtra("txnId", txnId)
            intent.putExtra("txnDesc", txnDesc)
            intent.putExtra("txnDateTime", txnDateTime)
            intent.putExtra("fares", fares)
            intent.putExtra("driverUid", driverUid)
            intent.putExtra("passengerUid", passengerUid)

            context.startActivity(intent)
        }


    }

    inner class HolderTransaction(itemView: View): RecyclerView.ViewHolder(itemView){
        var txnDescTv: TextView = binding.txnDescTv
        var txnDateTimeTv: TextView = binding.txnDateTimeTv
        var faresTv: TextView = binding.faresTv
    }

    override fun getItemCount(): Int {
        return transactionArrayList.size
    }

    override fun getFilter(): Filter {
        if (filterDesc == null){
            filterDesc = FilterTransaction(filterList, this)
        }
        return filterDesc as FilterTransaction
    }

}