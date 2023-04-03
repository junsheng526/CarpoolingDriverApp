package com.example.carpoolingdriverappv2.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Users (
    var name : String? = null,
    var phone : String? = null,
    var studentId : String? = null,
    var email : String? = null,
//    var password : String? = null,
    var walletBal : Double? = 0.0,
    var rating : Double? = 0.0,
    var profileImage : String? = null
)


