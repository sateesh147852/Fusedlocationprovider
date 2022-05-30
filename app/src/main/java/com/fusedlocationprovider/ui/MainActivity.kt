package com.fusedlocationprovider.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.fusedlocationprovider.databinding.ActivityMainBinding
import com.fusedlocationprovider.model.LocationData
import com.fusedlocationprovider.service.LocationService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EventBus.getDefault().register(this)
        startLocationService()
    }

    private fun startLocationService() {
        Intent(this,LocationService::class.java).also {
            startService(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onUpdateLocation(locationData: LocationData){
        Toast.makeText(this,""+locationData.latitude + " " + locationData.longitude,Toast.LENGTH_SHORT).show()
        Log.i(TAG, "onUpdateLocation: "+locationData.latitude + " " + locationData.longitude)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}