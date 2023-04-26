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
import com.example.carpoolingdriverappv2.TripModel
import com.example.carpoolingdriverappv2.ViewTripDetailsActivity
import com.example.carpoolingdriverappv2.databinding.RowTripBinding
import com.google.firebase.database.FirebaseDatabase
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TripAdapter :RecyclerView.Adapter<TripAdapter.HolderTrip>, Filterable{


    private val context: Context
    public var tripArrayList: ArrayList<TripModel>
    private var filterList: ArrayList<TripModel>

    private var filter: FilterTrip? = null

    //constructor
    constructor(context: Context, tripArrayList: ArrayList<TripModel>) {
        this.context = context
        this.tripArrayList = tripArrayList
        this.filterList = tripArrayList
    }

    private lateinit var binding: RowTripBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderTrip {
        //inflate bind row_trip
        binding = RowTripBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderTrip(binding.root)
    }

    override fun onBindViewHolder(holder: HolderTrip, position: Int) {
        //get set data handle click

        //get data
        val model = tripArrayList[position]
        val id = model.id
        val tripDate = model.tripDate
        val tripTime = model.tripTime
        val pickup = model.pickup
        val dropoff = model.dropoff
        val noOfSeats = model.noOfSeats
        val uid = model.uid
        val tripDateTime = model.tripDateTime
        val fares = model.fares

        //check list if trips date is past ,copy it to another list and remove from current list
        for (i in tripArrayList){
            if (checkTripsPast(tripDateTime)){
                //add to another list
                addTripToRecord(model, holder)
                //get id of trip to delete
                val ref = FirebaseDatabase.getInstance().getReference("Trips")
                ref.child(id)
                    .removeValue()
            }
        }

        //set data
//        holder.tripTv.text = trip
        holder.pickupTv.text = pickup
        holder.dropoffTv.text = dropoff
        holder.tripDateTv.text = tripDate
        holder.tripTimeTv.text = tripTime
        holder.tripIdTv.text = id

        //handle click delete trip
        holder.deleteBtn.setOnClickListener{
            //confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure want to delete?")
                .setPositiveButton("Confirm"){ a, d->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteTrip(model, holder)
                }
                .setNegativeButton("Cancel"){ a, d->
                    a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewTripDetailsActivity::class.java)
            intent.putExtra("tripId", id)
            intent.putExtra("pickup", pickup)
            intent.putExtra("dropoff", dropoff)
            intent.putExtra("tripDate", tripDate)
            intent.putExtra("tripTime", tripTime)
            intent.putExtra("noOfSeats", noOfSeats)
            intent.putExtra("fares", fares)
            intent.putExtra("tripDateTime", tripDateTime)
            intent.putExtra("uid", uid)

            context.startActivity(intent)
        }


    }

    private fun addTripToRecord(model: TripModel, holder: HolderTrip) {
        //setup data to add in firebase db
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = model.id
        hashMap["pickup"] = model.pickup
        hashMap["dropoff"] = model.dropoff
        hashMap["tripDate"] = model.tripDate
        hashMap["tripTime"] = model.tripTime
        hashMap["tripDateTime"] = model.tripDateTime
        hashMap["noOfSeats"] = model.noOfSeats
        hashMap["uid"] = model.uid
        hashMap["fares"] = model.fares
        hashMap["status"] = "Completed"


        val ref = FirebaseDatabase.getInstance().getReference("ExistingTripsRecord")
        ref.child(model.id)
            .setValue(hashMap)
    }

    private fun checkTripsPast(tripDateTime: String): Boolean{

        val now = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"))
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        val dateTimeTrip = LocalDateTime.parse(tripDateTime, formatter)

        //return true if the trip has expired
        return dateTimeTrip.isBefore(now)
    }

    private fun deleteTrip(model: TripModel, holder: HolderTrip) {
        //get id of trip to delete
        val id = model.id
        //firebase db > trip > id
        val ref = FirebaseDatabase.getInstance().getReference("Trips")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Trip Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Deleting failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return tripArrayList.size
    }

    //view holder class
    inner class HolderTrip(itemView: View): RecyclerView.ViewHolder(itemView){
        var pickupTv: TextView = binding.pickupTv
        var dropoffTv: TextView = binding.dropoffTv
        var tripDateTv: TextView = binding.tripDateTv
        var tripTimeTv: TextView = binding.tripTimeTv
        var deleteBtn: TextView = binding.deleteBtn
        var tripIdTv: TextView = binding.tripIdTv
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterTrip(filterList, this)
        }
        return filter as FilterTrip
    }


}