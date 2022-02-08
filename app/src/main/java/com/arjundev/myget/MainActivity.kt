package com.arjundev.myget
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var myFusedLocation: FusedLocationProviderClient
    private val myRQCODE = 1010
    private val myAPIKEY = "45b201de86bb9f899ffcb8c6c0b27bbd"
    private lateinit var mainBackground: LinearLayout
    private lateinit var myIcon: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainBackground = findViewById(R.id.mainBackground)
        myFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        myIcon = findViewById(R.id.weatherIcon)

        // function to getLast Location of the User
        // All the LOCATION_PERMISSION checks are done here
        // weather details and layouts are also managed here
        getLastLocation()
    }

    private fun getWeatherDetails(myLong: String, myLat: String) {
        // a string array to display today's day
        val daysArray = arrayOf("Sun", "Mon", "Tue", "Wed", "Thurs", "Fri", "Sat")

        // getting the today's day number of a week ranging from 1 - 7
        // with help of a calendar instance
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_WEEK]

        // getting respective day of the week from the array
        val currentDay = daysArray[day-1]

        // finding the text to display today's date and day
        val todayDate: TextView = findViewById(R.id.todayDateText)

        // fetching the Current Date and Time and formatting the day format
        val fetchedDateTime = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        val currentDate: String = fetchedDateTime.format(Date())

        // assigning the current day and time equal to the text to display
        todayDate.text = currentDay.plus(", ").plus(currentDate)

        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(myLat.toDouble(),myLong.toDouble(), 1)
        val currCity: TextView = findViewById(R.id.cityNameText)

        if(addresses.isNotEmpty()) currCity.text = addresses[0].locality

        val queue = Volley.newRequestQueue(this)
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=${myLat}&lon=${myLong}&units=metric&appid=${myAPIKEY}"
        val airQualityUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat=${myLat}&lon=${myLong}&appid=${myAPIKEY}"

        val weatherJSON = JsonObjectRequest(
            Request.Method.GET,weatherUrl,null,
            {
                    response->
                setWeatherDetails(response)
            },
            {
                Toast.makeText(this,"Unable to Fetch Weather Details",Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(weatherJSON)

        val airJSON = JsonObjectRequest(
            Request.Method.GET,airQualityUrl,null,
            {
                    response->
                setAirDetails(response)
            },
            {
                Toast.makeText(this,"Unable to Fetch Weather Details",Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(airJSON)
    }

    private fun setWeatherDetails(response: JSONObject?) {
        val currTempD: TextView = findViewById(R.id.currTemp)
        val todayMinMaxTextD: TextView = findViewById(R.id.todayMinMaxText)
        val cloudsTextD: TextView = findViewById(R.id.cloudsVal)
        val weatherDescTextD: TextView = findViewById(R.id.weatherDescText)
        val windSpeedValD: TextView = findViewById(R.id.windSpeedVal)
        val visibilityValD: TextView = findViewById(R.id.visibiltyVal)
        val airPressValD: TextView = findViewById(R.id.airPressValue)
        val humidValD: TextView = findViewById(R.id.humidityVal)
        val mainWeather: TextView = findViewById(R.id.mainWeather)
        val currCity: TextView = findViewById(R.id.cityNameText)

        if(currCity.text == "CITY_NAME") currCity.text = response!!.getString("name")
        todayMinMaxTextD.text = response!!.getJSONObject("main").getString("temp_min").plus("°C / ").plus(
            response.getJSONObject("main").getString("temp_max")).plus("°C")
        currTempD.text = response.getJSONObject("main").getString("temp").toDouble().toInt().toString()
        humidValD.text = response.getJSONObject("main").getString("humidity").plus(" %")
        airPressValD.text = response.getJSONObject("main").getString("pressure").plus(" hPa")
        windSpeedValD.text = response.getJSONObject("wind").getString("speed").plus(" m/s")
        visibilityValD.text = response.getString("visibility").plus("m")
        cloudsTextD.text = response.getJSONObject("clouds").getString("all").plus("%")
        weatherDescTextD.text = response.getJSONArray("weather").getJSONObject(0).getString("description")
        mainWeather.text = response.getJSONArray("weather").getJSONObject(0).getString("main")

        val helper:String = mainWeather.text.toString()
        when {
            "Thunderstorm" in helper -> {
                mainBackground.setBackgroundResource(R.drawable.thunder_bg)
            }
            "Rain" in helper -> {
                mainBackground.setBackgroundResource(R.drawable.rain_bg)
            }
            "Snow" in helper -> {
                mainBackground.setBackgroundResource(R.drawable.snow_bg)
            }
            "Clear" in helper -> {
                mainBackground.setBackgroundResource(R.drawable.sunny_weather_bg)
            }
            "Clouds" in helper -> {
                mainBackground.setBackgroundResource(R.drawable.clouds_bg)
            }
            helper.isNotEmpty() -> {
                mainBackground.setBackgroundResource(R.drawable.clouds_bg)
            }
        }

    }

    private fun setAirDetails(response: JSONObject?) {
        val currAqiTextD: TextView = findViewById(R.id.aqiLevel)

        val currAqiValD = response!!.getJSONArray("list").getJSONObject(0).getJSONObject("main").getString("aqi")
        currAqiTextD.text = when(currAqiValD){
            "1" -> "Good"
            "2" -> "Fair"
            "3" -> "Moderate"
            "4" -> "Poor"
            else -> {
                "Very Poor"
            }
        }

        myIcon.visibility = View.INVISIBLE
        mainBackground.visibility = View.VISIBLE
    }

    // function to get Last Location of the user
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if(isPermissionGiven()){
            if(isLocationEnabled()){
                myFusedLocation.lastLocation.addOnCompleteListener{
                        task->
                    val myLocation: Location?=task.result
                    if(myLocation==null){
                        getNewLocation()
                    }else{
                        getWeatherDetails(myLocation.longitude.toString(),myLocation.latitude.toString())
                    }
                }
            }else{
                Toast.makeText(this,"Please Enable GPS Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()
        }
    }

    // function to request user to give permission for location access
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            myRQCODE)
    }

    // overriding the onRequestPermissionResult function to
    // modify the events which will happen according to whether
    // user has given permission or not
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == myRQCODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocation()
            }
        }else{
            Toast.makeText(this,"Unable to Fetch Location",Toast.LENGTH_SHORT).show()
        }
    }

    // function to check whether permission is given or not
    private fun isPermissionGiven(): Boolean {
        return (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    // checking whether the location is enabled or not
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation(){

         val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        myFusedLocation.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for (location in p0.locations){
                getWeatherDetails(location.longitude.toString(),location.latitude.toString())
            }
        }
    }
}
