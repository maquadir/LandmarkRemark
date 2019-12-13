package com.example.landmarkremark


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.landmarkremark.databinding.ActivityGoogleSignInBinding
import kotlinx.android.synthetic.main.custom_toast.*

class SignInActivity : AppCompatActivity() {

    //variable declaration
    private val sharedPrefFile = "usernamesharedpreference"
    private lateinit var binding: ActivityGoogleSignInBinding
    private var sharedSignedValue:Boolean? = false
    private lateinit var viewModel: LocationViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //using data binding to bind data to view
        binding = DataBindingUtil.setContentView(this,R.layout.activity_google_sign_in)

        //calling viewModel
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        //code when sign up button is clicked
        binding.btnSignup.setOnClickListener{

            if (binding.etUsername.text.toString().equals("")) {

                //call warning toast function when username is not entered
                warningToast()
                return@setOnClickListener
            }

            //call function to signin
            signIn()
        }


    }

    public override fun onStart() {
        super.onStart()

        //get username from sharedpreferences, if user is already signed in then navigate directly to MapaActivity
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
        sharedSignedValue = sharedPreferences.getBoolean("signedin",false)
        if(sharedSignedValue == true){
           startMapsActivity()
        }

    }

    //function to navigate to MapsActivity
    private fun startMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    //function to sing in
    private fun signIn() {
        //save username to sharedpreferences which is local storage
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putString("username",binding.etUsername.text.toString())
        editor.putBoolean("signedin",true)
        editor.apply()
        editor.commit()

        //code to call MapsActivity when user is successfully authenticated
        startMapsActivity()
    }


    //function to display warning toast
    fun warningToast(){
        val layout = layoutInflater.inflate(R.layout.custom_toast,linearLayout)
        val myToast = Toast(applicationContext)
        val toast_text = layout.findViewById(R.id.custom_toast_message) as TextView

        myToast.setGravity(Gravity.TOP,0,200)
        myToast.view = layout
        toast_text.setText(getString(R.string.userwarning))
        myToast.show()
    }




}
