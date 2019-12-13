package com.example.landmarkremark

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class LocationViewModel:ViewModel() {

    //declare variables
    val db = FirebaseFirestore.getInstance()
    lateinit var mMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var lastLocation: Location

    //declare array lists
    val note = ArrayList<String>()//Creating an empty arraylist
    val username = ArrayList<String>()
    val latitude = ArrayList<Double>()
    val longitude = ArrayList<Double>()
    val latlon = ArrayList<String>()
    val markers = ArrayList<Marker>()

    //initialize view model
    init {
        Log.i("LocationVM", "LocationVM created")

    }

    override fun onCleared(){
         super.onCleared()
    }

    //function to fetch data from firebase cloudstore
    fun fetchFirebaseData() {

        //clear the array lists before appending
        note.clear()
        username.clear()
        latitude.clear()
        longitude.clear()
        latlon.clear()
        markers.clear()

        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)

        //fetch data from firebase
        db.collection("UserDataCollection")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    //store data in local arraylists
                    note.add("${document.data["note"]}")
                    username.add("${document.data["username"]}")
                    latitude.add("${document.data["latitude"]}".toDouble())
                    longitude.add("${document.data["longitude"]}".toDouble())
                    latlon.add("${document.data["latitude"]}"+","+"${document.data["longitude"]}")


                    //add marker to saved locations
                    val location: LatLng = LatLng("${document.data["latitude"]}".toDouble(),"${document.data["longitude"]}".toDouble())
                    val markerOptions = MarkerOptions().position(location)
                    markerOptions.title("note : "+"${document.data["note"]}").snippet("Username : "+"${document.data["username"]}"+"\n"+"Latitude : "+"${document.data["latitude"]}"+"\n"+"Longitude : "+"${document.data["latitude"]}").icon(icon)
                    mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { false })

                    //add markers to map
                    mMap.addMarker(markerOptions)
                    markers.add(mMap.addMarker(markerOptions))

                }
            }
            .addOnFailureListener { exception ->
                Log.i("MapsActivity", "Error getting documents.", exception)
            }
    }

    //function to check if google map is initialized
    fun checkInitialized(): Boolean {
        if(::mMap.isInitialized){
             return true
        }
        return false
    }

    //code to add a marker to location
//    fun placeMarkerOnMap(location: LatLng) {
//        val markerOptions = MarkerOptions().position(location)
////        mMap.addMarker(markerOptions)
//
//    }




}