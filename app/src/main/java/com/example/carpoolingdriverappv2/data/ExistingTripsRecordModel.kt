package com.example.carpoolingdriverappv2.data

class ExistingTripsRecordModel {
    var id:String = ""
    var trip:String = ""
    var pickup:String = ""
    var dropoff:String = ""
    var tripDate:String = ""
    var tripTime:String = ""
    var tripDateTime:String = ""
    var noOfSeats:String = ""
    var uid:String = ""

    //empty constructor
    constructor()

    constructor(
        id: String,
        trip: String,
        pickup: String,
        dropoff: String,
        tripDate: String,
        tripTime: String,
        tripDateTime: String,
        noOfSeats: String,
        uid: String
    ) {
        this.id = id
        this.trip = trip
        this.pickup = pickup
        this.dropoff = dropoff
        this.tripDate = tripDate
        this.tripTime = tripTime
        this.tripDateTime = tripDateTime
        this.noOfSeats = noOfSeats
        this.uid = uid
    }

    //parameterized constructor



}