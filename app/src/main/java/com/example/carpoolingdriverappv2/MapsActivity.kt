package com.example.carpoolingdriverappv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.carpoolingdriverappv2.databinding.ActivityMainBinding
import com.example.carpoolingdriverappv2.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(MapsFragment())
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mapLayout,fragment)
        fragmentTransaction.commit()
    }
}