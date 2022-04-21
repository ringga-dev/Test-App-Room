package com.music.testapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.music.testapp.databinding.ActivityActionDataBinding
import com.music.testapp.room.Location
import com.music.testapp.utills.toals
import com.music.testapp.viewModel.LocationViewModel
import com.music.testapp.viewModel.UserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ActionDataActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {
    private lateinit var binding: ActivityActionDataBinding
    private lateinit var userViewModel: LocationViewModel

    private val REQUEST_CODE = 5678
    private var loc: Boolean? = false
    private lateinit var map: MapboxMap
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var callback: LocationChangeListeningCallback

    private var cameraPosisi: LatLng? = null

    private var lat: Double? = null
    private var long: Double? = null
    private var stts = "active"


    private var action: String = ""

    private var locasiEdit: LatLng? = null
    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        binding = ActivityActionDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        action = intent.getStringExtra("action")!!
        id = intent.getIntExtra("id", 0)

        userViewModel.getState().observer(this, Observer {
            handleUiState(it)
        })
        binding.mapView.getMapAsync(this)

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            changeMap()
        }

        binding.setLocation.setOnClickListener {
            cameraPosisi = map.cameraPosition.target
        }
        binding.active.setOnClickListener {
            stts = "active"
            binding.inActive.setBackgroundResource(R.drawable.bg_right_inactive)
            binding.active.setBackgroundResource(R.drawable.bg_left_active)
        }

        binding.close.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding.cencel.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.inActive.setOnClickListener {
            stts = "inactive"
            binding.inActive.setBackgroundResource(R.drawable.bg_right_active)
            binding.active.setBackgroundResource(R.drawable.bg_left_inactive)
        }

        if (action == "edit") {
            binding.delete.visibility = View.VISIBLE
            binding.barStatus.text = "Edit Location"
        } else {
            binding.delete.visibility = View.GONE
            binding.barStatus.text = "Add Location"
        }

        binding.delete.setOnClickListener {
            userViewModel.delete(id!!)
            finish()
        }

//keyboard show
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = binding.root.rootView.height - binding.root.height
            if (heightDiff > dpToPx(
                    200f
                )
            ) { // if more than 200 dp, it's probably a keyboard...
                binding.mapView.visibility = View.GONE
            } else {
                // ... not a keyboard...
                binding.mapView.visibility = View.VISIBLE
            }
        }
        binding.saveData.setOnClickListener {
            val name = binding.edName.text.toString().trim()
            val addres = binding.address.text.toString().trim()
            val city = binding.city.text.toString().trim()
            val zip_code = binding.zipCode.text.toString().trim()


            if (name.isEmpty()) {
                binding.edName.error = "Please enter name"
                binding.edName.requestFocus()
                return@setOnClickListener
            }
            if (addres.isEmpty()) {
                binding.address.error = "Please enter address"
                binding.address.requestFocus()
                return@setOnClickListener
            }
            if (city.isEmpty()) {
                binding.city.error = "Please enter city"
                binding.city.requestFocus()
                return@setOnClickListener
            }
            if (zip_code.isEmpty()) {
                binding.zipCode.error = "Please enter zip code"
                binding.zipCode.requestFocus()
                return@setOnClickListener
            }
            if (action == "add") {
                saveData(Location(name, addres, city, zip_code, "$cameraPosisi", stts))
            } else {
                updateLocation(Location(name, addres, city, zip_code, "$cameraPosisi", stts))
            }

        }
    }


    suspend fun changeMap() {
        Handler().postDelayed({
            loc = true
        }, 5000)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        if (action != "add") {
            userViewModel.getDataById(id!!)
        }
        callback = LocationChangeListeningCallback()
        mapboxMap.setStyle(Style.SATELLITE_STREETS) {
            enableLocationComponent(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .useDefaultLocationEngine(false)
                    .build()


            map.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled =
                    true                       // Enable to make component visible
                cameraMode =
                    CameraMode.NONE                        // Set the component's camera mode
                renderMode =
                    RenderMode.COMPASS                         // Set the component's render mode
            }
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest
            .Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .build()
        locationEngine.requestLocationUpdates(request, callback, mainLooper)
        locationEngine.getLastLocation(callback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private inner class LocationChangeListeningCallback :
        LocationEngineCallback<LocationEngineResult> {

        override fun onSuccess(result: LocationEngineResult?) {
            result?.lastLocation
                ?: return //BECAREFULL HERE, IF NAME LOCATION UPDATE DONT USER -> val resLoc = result.lastLocation ?: return
            if (result.lastLocation != null) {
                lat = result.lastLocation?.latitude!!
                long = result.lastLocation?.longitude!!
                val latLng = LatLng(lat!!, long!!)
                val lat = map.cameraPosition
                val currentZoom = lat.target
                if (action == "add") {
                    if (loc == false) {

                        if (result.lastLocation != null) {
                            cameraPosisi = latLng
                            map.locationComponent.forceLocationUpdate(result.lastLocation)
                            val position = CameraPosition.Builder()
                                .target(latLng)
                                .zoom(18.0)
                                .build()
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                        }
                    } else {
                        if (result.lastLocation != null) {
                            cameraPosisi = currentZoom
                            map.locationComponent.forceLocationUpdate(result.lastLocation)
                            val position = CameraPosition.Builder()
                                .target(currentZoom)
                                .zoom(lat.zoom)
                                .build()
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                        }
                    }
                } else {
                    if (loc == false) {
                        map.locationComponent.forceLocationUpdate(result.lastLocation)
                        val position = CameraPosition.Builder()
                            .target(cameraPosisi)
                            .zoom(18.0)
                            .build()
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                    } else {
                        if (result.lastLocation != null) {
                            cameraPosisi = currentZoom
                            map.locationComponent.forceLocationUpdate(result.lastLocation)
                            val position = CameraPosition.Builder()
                                .target(currentZoom)
                                .zoom(lat.zoom)
                                .build()
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                        }
                    }
                }

            }


        }

        override fun onFailure(exception: Exception) {}
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Permission not granted!!", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            map.getStyle {
                enableLocationComponent(it)
            }
        } else {
            Toast.makeText(this, "Permission not granted!! app will be EXIT", Toast.LENGTH_LONG)
                .show()
            Handler().postDelayed({
                finish()
            }, 3000)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine.removeLocationUpdates(callback)
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }


    companion object {
        const val DEFAULT_INTERVAL_IN_MILLISECONDS = 500L
        const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }

    private fun saveData(location: Location) {
        userViewModel.saveLocation(location)
    }

    private fun updateLocation(location: Location) {
        userViewModel.updateLocation(id!!, location)
    }

    private fun handleUiState(it: UserState?) {
        when (it) {

            is UserState.Error -> {
                toals(this, it.err)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            is UserState.ShoewToals -> toals(this, it.message)

            is UserState.Data -> {
                toals(this, it.locationData.toString())
            }
            is UserState.DataById -> {
                //set text to edit text
                val data = it.locationData
                binding.edName.setText(data.name)
                binding.address.setText(data.address)
                binding.city.setText(data.city)
                binding.zipCode.setText(data.zip_code)

                //pecah string lat long
                val marker = data.lat_long?.split(",")
                val lat = marker?.get(0)?.split("=")?.get(1)?.toDouble()
                val long = marker?.get(1)?.split("=")?.get(1)?.toDouble()
                cameraPosisi = LatLng(lat!!, long!!)

                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat!!, long!!))
                        .title("Lokasi")
                )

                if (data.stts == "active") {
                    stts = "active"
                    binding.inActive.setBackgroundResource(R.drawable.bg_right_inactive)
                    binding.active.setBackgroundResource(R.drawable.bg_left_active)
                } else {
                    stts = "inactive"
                    binding.inActive.setBackgroundResource(R.drawable.bg_right_active)
                    binding.active.setBackgroundResource(R.drawable.bg_left_inactive)
                }
            }

            is UserState.Success -> {
                toals(this, it.message)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

    }


}