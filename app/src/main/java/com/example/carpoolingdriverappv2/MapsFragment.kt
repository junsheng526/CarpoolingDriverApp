package com.example.carpoolingdriverappv2

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.carpoolingdriverappv2.data.LocationInfo
import com.example.carpoolingdriverappv2.databinding.FragmentMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase



class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var appPermission: AppPermissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionToRequest = mutableListOf<String>()
    private var isLocationPermissionOk = false
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var currentLocation: Location
    private var currentMarker: Marker? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var isTrafficEnable: Boolean = false

    private lateinit var database : FirebaseDatabase


    val tarumt_gate2 = LatLng(3.213657, 101.726999)
    val tarumt_maingate = LatLng(3.215021, 101.728418)
    val tarumt_hostel = LatLng(3.217382, 101.732491)
    val pv128 = LatLng(3.200647, 101.717896)
    val pv16 = LatLng(3.201927, 101.717246)
    val pv15 = LatLng(3.202295, 101.717021)
    val pv10 = LatLng(3.204056, 101.714990)
    val pv12 = LatLng(3.206772, 101.717333)
    val prima_setapak = LatLng(3.197373, 101.710913)

    private var locationArray: ArrayList<LatLng>? = null
    private var locationNameArray: ArrayList<String>? = null

    val locations = arrayOf(
        LocationInfo("","TARUMT GATE 2", 3.213657, 101.726999),
        LocationInfo("","TARUMT MAIN GATE", 3.215021, 101.728418),
        LocationInfo("","TARUMT HOSTEL", 3.217382, 101.732491),
        LocationInfo("","PV128", 3.200647, 101.717896),
        LocationInfo("","PV16", 3.201927, 101.717246),
        LocationInfo("","PV15", 3.202295, 101.717021),
        LocationInfo("","PV10", 3.204056, 101.714990),
        LocationInfo("","PV12", 3.206772, 101.717333),
        LocationInfo("","PRIMA SETAPAK", 3.197373, 101.710913)
    )



    private var radius = 1500

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appPermission = AppPermissions()
        firebaseAuth = Firebase.auth
        database = FirebaseDatabase.getInstance()
//        uploadLocationInfo()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionOk =
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                            && permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true

                if (isLocationPermissionOk)
                    setUpGoogleMap()
                else
                    Snackbar.make(binding.root, "Location permission denied", Snackbar.LENGTH_LONG)
                        .show()

            }

        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.homeMap) as SupportMapFragment?)
        mapFragment?.getMapAsync(this)

        locationArray = ArrayList()

        locationArray!!.add(tarumt_gate2)
        locationArray!!.add(tarumt_maingate)
        locationArray!!.add(tarumt_hostel)
        locationArray!!.add(pv128)
        locationArray!!.add(pv16)
        locationArray!!.add(pv15)
        locationArray!!.add(pv10)
        locationArray!!.add(pv12)
        locationArray!!.add(prima_setapak)


        binding.currentLocation.setOnClickListener { getCurrentLocation() }

        binding.enableTraffic.setOnClickListener {

            if (isTrafficEnable) {
                mGoogleMap?.apply {
                    isTrafficEnabled = false
                    isTrafficEnable = false
                }
            } else {
                mGoogleMap?.apply {
                    isTrafficEnabled = true
                    isTrafficEnable = true
                }
            }
        }

        binding.btnMapType.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)

            popupMenu.apply {
                menuInflater.inflate(
                    R.menu.map_type_menu,
                    popupMenu.menu
                )
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {

                        R.id.btnNormal -> mGoogleMap?.mapType =
                            GoogleMap.MAP_TYPE_NORMAL
                        R.id.btnSatellite -> mGoogleMap?.mapType =
                            GoogleMap.MAP_TYPE_SATELLITE
                        R.id.btnTerrain -> mGoogleMap?.mapType =
                            GoogleMap.MAP_TYPE_TERRAIN
                    }
                    true
                }

                show()
            }
        }

    }

    private fun uploadLocationInfo() {
        val locationsMap = HashMap<String, Any>()
        for (i in locations.indices) {
            val location = locations[i]
            val id = "LCTN$i"
            locationsMap["id"] = id
            locationsMap["name"] = location.name
            locationsMap["latitude"] = location.latitude
            locationsMap["longitude"] = location.longitude

            FirebaseDatabase.getInstance().getReference("LocationInfo")
                .child(id)
                .setValue(locationsMap)
        }


    }

    private fun setUpGoogleMap() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isTiltGesturesEnabled = true

        setUpLocationUpdate()
    }

    private fun setUpLocationUpdate() {

        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                for (location in locationResult?.locations!!) {
                    Log.d("TAG", "onLocationResult: ${location.longitude} ${location.latitude}")
                }
            }
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Location update start", Toast.LENGTH_SHORT).show()
            }
        }

        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionOk = false
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {

            currentLocation = it

            moveCameraToLocation(currentLocation)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        Log.d("TAG", "stopLocationUpdates: Location Update Stop")
    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient != null) {
            startLocationUpdates()
            currentMarker?.remove()
        }
    }

    private fun moveCameraToLocation(location: Location) {

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                location.latitude,
                location.longitude
            ), 17f
        )

        val markerOption = MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .snippet(firebaseAuth.currentUser?.displayName)

        currentMarker?.remove()
        currentMarker = mGoogleMap?.addMarker(markerOption)
        currentMarker?.tag = 703
        mGoogleMap?.animateCamera(cameraUpdate)

    }

    override fun onMapReady(googleMap: GoogleMap) {

        mGoogleMap = googleMap
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isLocationPermissionOk = true
                setUpGoogleMap()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission")
                    .setMessage("Near me required location permission to access your location")
                    .setPositiveButton("Ok") { _, _ ->
                        requestLocation()
                    }.create().show()
            }

            else -> {
                requestLocation()
            }
        }

        for (location in locations){
            mGoogleMap!!.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.name)
            )
        }


    }

    private fun requestLocation() {
        permissionToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun getNearByPlace(placeType: String) {
        val url = ("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + currentLocation.latitude + "," + currentLocation.longitude
                + "&radius=" + radius + "&type=" + placeType + "&key=" +
                resources.getString(com.example.carpoolingdriverappv2.R.string.API_KEY))


    }



}