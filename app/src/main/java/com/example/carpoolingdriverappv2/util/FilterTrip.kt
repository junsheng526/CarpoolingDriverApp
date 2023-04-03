package com.example.carpoolingdriverappv2.util

import com.example.carpoolingdriverappv2.TripModel

class FilterTrip: android.widget.Filter {

    //arraylist we want to search
    private var filterList: ArrayList<TripModel>

    private var tripAdapter: TripAdapter

    //constructor
    constructor(filterList: ArrayList<TripModel>, tripAdapter: TripAdapter) {
        this.filterList = filterList
        this.tripAdapter = tripAdapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //value should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()){
            //searched value is nor null not empty

            //change to upper case or lower case to avoid sensitive
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<TripModel> = ArrayList()
            for (i in 0 until filterList.size){
                //validate
                if (filterList[i].dropoff.uppercase().contains(constraint)){
                    //add to filter list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        tripAdapter.tripArrayList = results.values as ArrayList<TripModel>

        //notify changes
        tripAdapter.notifyDataSetChanged()
    }

}