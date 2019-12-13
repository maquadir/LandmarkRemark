package com.example.landmarkremark

import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.landmarkremark.databinding.ActivityMapsBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.custom_toast.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,GoogleMap.OnMapClickListener,GoogleMap.OnInfoWindowClickListener,
    GoogleMap.InfoWindowAdapter{

    //declare variables
    private lateinit var marker:Marker
    private lateinit var viewModel: LocationViewModel
    private lateinit var dialog :Dialog
    private val sharedPrefFile = "usernamesharedpreference"
    private lateinit var binding: ActivityMapsBinding
    private var sharedNameValue:String? = ""


    //code to declare permission variables and tags
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private val TAG = "MapsActivity"
        private const val PLACE_PICKER_REQUEST = 3

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //use of databinding to bind data to view
        binding = DataBindingUtil.setContentView(this,R.layout.activity_maps)

        //calling viewModel
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        //get username from sharedpreferences
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
        sharedNameValue = sharedPreferences.getString("username","defaultname")

        //set action bar title
        getSupportActionBar()?.setTitle("Welcome "+sharedNameValue)

        //fetch data from firebase database
        viewModel.fetchFirebaseData()

        //show loading toast message
        showToast("Loading map...")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ////set up the location permissions. If they don't accept, they will still be able to see other user's landmarks
        viewModel.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Request current location permission
        if(!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }


    }

    override fun onResume(){
        super.onResume()


        //code when location permission is granted to the user
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //code that draws a light blue dot on the user’s location & adds a button to the map that,
            // when tapped, centers the map on the user’s location.

            //check if google map is initialized
            if(viewModel.checkInitialized() == true) {

                viewModel.mMap.isMyLocationEnabled = true

                //this code gives you the most recent location currently available.
                viewModel.fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        viewModel.lastLocation = location
                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        viewModel.placeMarkerOnMap(currentLatLng)//code to add a marker to location
                        viewModel.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                    }
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //code to retrieve details about the selected place if it has a RESULT_OK result for a PLACE_PICKER_REQUEST request,
        // and then place a marker on that position on the map.
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

//                placeMarkerOnMap(place.latLng)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.actionbar_menu, menu)

        //search view bar and item
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu?.findItem(R.id.search_location)
        val searchView = searchItem?.actionView as SearchView

        // Assumes current activity is the searchable activity
//       val closeButton = searchView?.findViewById(R.id.search_close_btn) as ImageView

        //search for a note based on contained text or user-name
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                //convert all notes or username to lowercase
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    viewModel.note.replaceAll(String::toLowerCase)
                    viewModel.username.replaceAll(String::toLowerCase)
                }

                when (newText.toLowerCase()) {
                    in viewModel.note -> searchNote(newText.toLowerCase())
                    in viewModel.username -> searchUsername(newText.toLowerCase())
                }


                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                return false
            }

        })

        //when close button of search view is clicked
        searchView.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {

                //clear map
                viewModel.mMap.clear()

                val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)

                //reset map markers to original state
                for ((index) in viewModel.username.withIndex()) {
                    val location = LatLng(viewModel.latitude[index], viewModel.longitude[index])
                    val markerOptions = MarkerOptions().position(location)
                    markerOptions.title(viewModel.note[index])
                        .snippet("Username : " + viewModel.username[index] + "\n" + "Latitude : " + viewModel.latitude[index] + "\n" + "Longitude : " + viewModel.longitude[index])
                    .icon(icon)

                    viewModel.mMap.addMarker(markerOptions)

                }

                return false
            }


        })

        return true
    }

    //function to search map based on username
    private fun searchUsername(newText: String) {

        //Calculate the markers to get their position
        val b = LatLngBounds.Builder()
        var count = 0

        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)

        viewModel.mMap.clear()
        //search all markers corresponding to the typed username
        for ((index, value) in viewModel.username.withIndex()) {
            if (value == newText) {
                b.include(viewModel.markers[index].position)
                count = count + 1

                //collect all the  markers
                val location = LatLng(viewModel.latitude[index], viewModel.longitude[index])
                val markerOptions = MarkerOptions().position(location)
                markerOptions.title(viewModel.note[index])
                    .snippet("Username : " + viewModel.username[index] + "\n" + "Latitude : " + viewModel.latitude[index] + "\n" + "Longitude : " + viewModel.longitude[index])

                viewModel.mMap.addMarker(markerOptions)
            } else {

                //change markers
                val location = LatLng(viewModel.latitude[index], viewModel.longitude[index])
                val markerOptions = MarkerOptions().position(location)
                markerOptions.title(viewModel.note[index])
                    .snippet("Username : " + viewModel.username[index] + "\n" + "Latitude : " + viewModel.latitude[index] + "\n" + "Longitude : " + viewModel.longitude[index])
                    .icon(icon)

                viewModel.mMap.addMarker(markerOptions)
            }
        }

        val bounds = b.build()

        //Change the padding as per needed and animate camera to show all the markers found
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 50,50,5)
        viewModel.mMap.animateCamera(cu)

        //dismiss soft input keyboard
        dismissKeyboard()

        //display toast with count of found result
        displayToast(count)

    }

    //function to search map based on note
    private fun searchNote(newText:String) {

        //Calculate the markers to get their position
        val b = LatLngBounds.Builder()
        var count = 0
        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)


        viewModel.mMap.clear()
        //search all markers corresponding to the typed note
        for ((index, value) in viewModel.note.withIndex()) {
            if(value == newText) {
                b.include(viewModel.markers[index].position)
                count = count + 1

                //change markers
                val location = LatLng(viewModel.latitude[index],viewModel.longitude[index])
                val markerOptions = MarkerOptions().position(location)
                markerOptions.title(viewModel.note[index]).snippet("Username : "+viewModel.username[index]+"\n"+"Latitude : "+viewModel.latitude[index]+"\n"+"Longitude : "+viewModel.longitude[index])

                viewModel.mMap.addMarker(markerOptions)
            }else{

                //change markers
                val location = LatLng(viewModel.latitude[index],viewModel.longitude[index])
                val markerOptions = MarkerOptions().position(location)
                markerOptions.title(viewModel.note[index]).snippet("Username : "+viewModel.username[index]+"\n"+"Latitude : "+viewModel.latitude[index]+"\n"+"Longitude : "+viewModel.longitude[index])
                    .icon(icon)

                viewModel.mMap.addMarker(markerOptions)

            }
        }

        val bounds = b.build()

        //Change the padding as per needed and animate camera to show all the markers found
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 50,50,5)
        viewModel.mMap.animateCamera(cu)

        //dismiss soft input keyboard
        dismissKeyboard()

        //display toast with count of found result
        displayToast(count)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
         // as you specify a parent activity in AndroidManifest.xml.
         var id:Int = item.getItemId()

        //menu bar item buttons to search and signOut
        when (id) {
            R.id.search_location -> loadPlacePicker()
            R.id.signout -> signOut()
        }

         return super.onOptionsItemSelected(item)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //initialise GoogleMap
        viewModel.mMap = googleMap

        //code to enable the zoom controls on the map
        viewModel.mMap.getUiSettings().setZoomControlsEnabled(true)
        viewModel.mMap.setOnMarkerClickListener(this)
        viewModel.mMap.setOnMapClickListener(this)
        viewModel.mMap.setOnInfoWindowClickListener(this)

        //code to check if the app has been granted the location access , if not we request the user
        setUpMap()

    }

    //This method creates a new builder for an intent to start the Place Picker UI and then starts the PlacePicker intent
    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(
                builder.build(this@MapsActivity),
                PLACE_PICKER_REQUEST
            )
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    //sign out from current user
    private fun signOut() {
        //code to update sharedpreferences
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putString("username","")
        editor.putBoolean("signedin",false)
        editor.apply()
        editor.commit()

        //code to transit to Main Screen
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    //function called when marker is clicked or tapped which displays a dialog to add note
    override fun onMarkerClick(p0: Marker?): Boolean {
        Log.i("Marker", "marker clicked")

        //display dialog to add a note when location is valid
        if (p0 != null) {
            showDialog(p0.position.latitude,p0.position.longitude,p0)
        }
        return true
    }

    //function called when any other random location is clicked on the map
    override fun onMapClick(location: LatLng) {
        Log.i("maps", "position clicked")

        val icon = BitmapDescriptorFactory.fromResource(R.drawable.check)
        //to add notes to random locations on map
        viewModel.mMap.setOnMarkerClickListener(this)
        val markerOptions = MarkerOptions().position(location).icon(icon)
        marker = viewModel.mMap.addMarker(markerOptions)
        showDialog(location?.latitude,location?.longitude,marker)

    }

//  override fun onMarkerClick(p0: Marker?) = false

    //Check the ACCESS_FINE_LOCATION permission
    private fun checkLocationPermission() =
        ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onInfoWindowClick(p0: Marker?) {
        Log.i("Marker info", p0?.position?.longitude.toString())
        if (p0 != null) {

            //code to show dialog to edit an existing note of the user and restricting users to edit other notes
            if(p0.snippet.contains(sharedNameValue.toString())) {
                editNoteDialog(p0)
            }else{
               showToast("Note cannot be edited, belongs to a different user")
            }
        }
    }

    //display dialog for editing note
    private fun editNoteDialog( marker: Marker){

        //initialize dialog and its attributes
        dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(false)
        dialog .setContentView(R.layout.username_dialog)
        val okButton = dialog .findViewById(R.id.btn_ok) as Button
        val cancelButton = dialog .findViewById(R.id.btn_cancel) as Button
        val usertext = dialog .findViewById(R.id.et_note) as EditText
        val dialog_title = dialog .findViewById(R.id.tv_dialog_title) as TextView

        //add dialog title
        dialog_title.setText(getString(R.string.edit_note))
        usertext.setText(viewModel.note[viewModel.latitude.indexOf(marker.position.latitude)].toString())


        //add dialog attributes
        val wmlp = dialog.window!!.attributes
        wmlp.gravity = Gravity.TOP
        wmlp.y = 150

        //show dialog
        dialog .show()

        //code when ok button is clicked
        okButton.setOnClickListener {

            if (!usertext.text.toString().equals("")) {

                //update cloud firestore
                viewModel.db.collection("UserDataCollection").document(marker.position.latitude.toString()+","+marker.position.longitude.toString())
                    .update("note", usertext.text.toString())
                    .addOnSuccessListener { Log.d(MapsActivity.TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(MapsActivity.TAG, "Error updating document", e) }

                //code to dismiss soft input keyboard
                dismissKeyboard()

                //code to refresh activity and dismiss dialog
                recreate()
                dialog.dismiss()

            } else {

                //code to display note to enter a note
                showToast("Please enter a note to proceed")
            }
        }

        //code when cancel button is clicked
        cancelButton.setOnClickListener {
            dismissKeyboard()
            dialog .dismiss()
        }

    }


    private fun showDialog(lat: Double,lon: Double,marker:Marker ){

        //initialize dialog and its attributes
        dialog = Dialog(this)
        dialog .requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog .setCancelable(false)
        dialog .setContentView(R.layout.input_dialog)
        val okButton = dialog .findViewById(R.id.btn_ok) as Button
        val cancelButton = dialog .findViewById(R.id.btn_cancel) as Button
        val note_text = dialog .findViewById(R.id.et_note) as EditText
        val dialog_title = dialog .findViewById(R.id.tv_dialog_title) as TextView

        //add dialog title
        dialog_title.setText(getString(R.string.add_note))


        //add dialog attributes
        val wmlp = dialog.window!!.attributes
        wmlp.gravity = Gravity.TOP
        wmlp.y = 150

        //show dialog
        dialog .show()

        //code when ok button is clicked
        okButton.setOnClickListener {

            if (note_text.text.toString().equals("")) {

                // code to display dialog to enter a note
                showToast("Please enter a note to proceed")

            } else {

                //note data structure
                val note = hashMapOf(
                    "username" to sharedNameValue,
                    "note" to note_text.text.toString(),
                    "latitude" to lat,
                    "longitude" to lon
                )

                // Add a new document with a generated ID
                viewModel.db.collection("UserDataCollection")
                    .document(lat.toString()+","+lon.toString())
                    .set(note)
                    .addOnSuccessListener { documentReference ->
                        Log.d(MapsActivity.TAG, "DocumentSnapshot added with ID: ${documentReference}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(MapsActivity.TAG, "Error adding document", e)
                    }

                //code to dismiss keyboard
                dismissKeyboard()

                //code to refresh activity and dismiss dialog
                recreate()
                dialog.dismiss()

            }
        }

        //code when cancel button is clicked
        cancelButton.setOnClickListener {

            //code to dismiss keyboard, dialog and remove marker from position
            dismissKeyboard()
            viewModel.mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { false })
            marker.remove()
            dialog .dismiss()

        }

    }

    //function to setup map
    private fun setUpMap() {

        //code to check if the app has been granted the location access , if not we request the user
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MapsActivity.LOCATION_PERMISSION_REQUEST_CODE
            )

            return
        }

        viewModel.mMap.isMyLocationEnabled = true

        //this code gives you the most recent location currently available.
        viewModel.fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                viewModel.lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
//                viewModel.placeMarkerOnMap(currentLatLng)//code to add a marker to location
                viewModel.mMap.setInfoWindowAdapter(this)
                viewModel.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))


            }
        }


    }

    //infor adapter methods to display custom info window
    override fun getInfoContents(p0: Marker?): View? {

        // Getting view from the layout file infowindowlayout.xml
            val v:View = getLayoutInflater().inflate(R.layout.custom_info_window, null);

            //declare layout views
            val tv1:TextView = v.findViewById(R.id.tv_note)
            val tv2:TextView = v.findViewById(R.id.tv_snippet)
            val title: String? = p0?.title
            val notes: String? = p0?.snippet


            if(title == null || notes == null){
                return null
            }

            //set text and snippet of marker options to layout views
            tv1.text = title
            tv2.text = notes

            return v

    }

    override fun getInfoWindow(p0: Marker?):View? {
       return null
    }

    //function to dismiss keyboard
    fun dismissKeyboard(){
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(getCurrentFocus()?.getWindowToken(), 0)
    }


    //display custom toast with count of searched locations
    fun displayToast(count:Int){

        val layout = layoutInflater.inflate(R.layout.custom_toast,linearLayout)
        val myToast = Toast(applicationContext)
        myToast.setGravity(Gravity.TOP,0,200)
        myToast.view = layout
        val toast_text = layout.findViewById(R.id.custom_toast_message) as TextView
        if(count == 1){
            toast_text.text = "Found "+count+" location"
        }else if(count > 1){
            toast_text.text = "Found "+count+" locations"
        }

        myToast.show()
    }

    //display custom toast
    fun showToast(text:String){
        val layout = layoutInflater.inflate(R.layout.custom_toast,linearLayout)
        val myToast = Toast(applicationContext)
        myToast.setGravity(Gravity.TOP,0,200)
        myToast.view = layout
        val toast_text = layout.findViewById(R.id.custom_toast_message) as TextView
        toast_text.setText(text)
        myToast.show()
    }

    //onSaveInstance function
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("GoogleSignIn", "onSaveInstanceState")
    }

    //onRestoreInstance function
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i("GoogleSignIn", "onRestoreInstanceState")
    }


}
