package com.example.carpoolingdriverappv2.data

class LocationInfo {
    var id: String = ""
    var name: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor()

    constructor(id: String, name: String, latitude: Double, longitude: Double) {
        this.id = id
        this.name = name
        this.latitude = latitude
        this.longitude = longitude
    }

}