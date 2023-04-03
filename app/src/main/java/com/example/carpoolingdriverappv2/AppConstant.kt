package com.example.carpoolingdriverappv2

class AppConstant {
    companion object {
        @JvmStatic
        val STORAGE_REQUEST_CODE = 1000

        @JvmStatic
        val PROFILE_PATH = "/Profile/image_profile.jpg"

        const val LOCATION_REQUEST_CODE = 2000


        @JvmStatic
        val placesName =
            listOf<PlaceModel>(
                PlaceModel(1, R.drawable.ic_baseline_restaurant_24, "Restaurant", "restaurant"),
                PlaceModel(2, R.drawable.ic_baseline_local_atm_24, "ATM", "atm"),
                PlaceModel(3, R.drawable.ic_baseline_local_gas_station_24, "Gas", "gas_station"),
            )
    }
}