package com.example.carpoolingdriverappv2.util

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.carpoolingdriverappv2.EditTripActivity
import com.example.carpoolingdriverappv2.data.ExistingTripsRecordModel
import com.example.carpoolingdriverappv2.databinding.RowTripBinding
import com.example.carpoolingdriverappv2.databinding.RowTripTestBinding
import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.ArrayList

class ExistingTripAdapter :RecyclerView.Adapter<ExistingTripAdapter.HolderTrip>, Filterable{


    private val context: Context
    public var existingTripArrayList: ArrayList<ExistingTripsRecordModel>
    private var filterList: ArrayList<ExistingTripsRecordModel>

    private var filter: FilterExistingTrip? = null

    //constructor
    constructor(context: Context, existingTripArrayList: ArrayList<ExistingTripsRecordModel>) {
        this.context = context
        this.existingTripArrayList = existingTripArrayList
        this.filterList = existingTripArrayList
    }

    private lateinit var binding: RowTripTestBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderTrip {
        //inflate bind row_trip
        binding = RowTripTestBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderTrip(binding.root)
    }

    override fun onBindViewHolder(holder: HolderTrip, position: Int) {
        //get set data handle click

        //get data
        val model = existingTripArrayList[position]
        val id = model.id
        val tripDate = model.tripDate
        val tripTime = model.tripTime
        val pickup = model.pickup
        val dropoff = model.dropoff
        val noOfSeats = model.noOfSeats
        val uid = model.uid
        val tripDateTime = model.tripDateTime

        //set data
//        holder.tripTv.text = trip
        holder.pickupTv.text = pickup
        holder.dropoffTv.text = dropoff
        holder.tripDateTv.text = tripDate
        holder.tripTimeTv.text = tripTime
        holder.tripIdTv.text = id

//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, EditTripActivity::class.java)
//            intent.putExtra("tripId", id)
//            intent.putExtra("pickup", pickup)
//            intent.putExtra("dropoff", dropoff)
//            intent.putExtra("tripDate", tripDate)
//            intent.putExtra("tripTime", tripTime)
//            intent.putExtra("noOfSeats", noOfSeats)
//
//            context.startActivity(intent)
//        }


    }

    override fun getItemCount(): Int {
        return existingTripArrayList.size
    }

    //view holder class
    inner class HolderTrip(itemView: View): RecyclerView.ViewHolder(itemView){
        //        var tripTv: TextView = binding.tripTv
        var pickupTv: TextView = binding.pickupTv
        var dropoffTv: TextView = binding.dropoffTv
        var tripDateTv: TextView = binding.tripDateTv
        var tripTimeTv: TextView = binding.tripTimeTv
        var tripIdTv: TextView = binding.tripIdTv
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterExistingTrip(filterList, this)
        }
        return filter as FilterTrip
    }


}