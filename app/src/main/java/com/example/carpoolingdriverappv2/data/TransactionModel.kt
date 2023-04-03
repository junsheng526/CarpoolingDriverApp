package com.example.carpoolingdriverappv2.data

class TransactionModel {
    var txnId:String = ""
    var txnDesc:String = ""
    var txnDateTime:String = ""
    var fares:String = ""
    var driverUid:String = ""
    var passengerUid:String = ""

    constructor()

    constructor(
        txnId: String,
        txnDesc: String,
        txnDateTime: String,
        fares: String,
        driverUid: String,
        passengerUid: String
    ) {
        this.txnId = txnId
        this.txnDesc = txnDesc
        this.txnDateTime = txnDateTime
        this.fares = fares
        this.driverUid = driverUid
        this.passengerUid = passengerUid
    }


}