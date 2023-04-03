package com.example.carpoolingdriverappv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.carpoolingdriverappv2.databinding.ActivityVerifyDocumentBinding
import com.example.carpoolingdriverappv2.verifyDocumentFragment.BackICFragment
import com.example.carpoolingdriverappv2.verifyDocumentFragment.FrontICFragment
import com.example.carpoolingdriverappv2.verifyDocumentFragment.SelfieFragment
import com.google.android.material.tabs.TabLayout

class VerifyDocumentActivity : AppCompatActivity() {

    private lateinit var binding : ActivityVerifyDocumentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        var viewPager = findViewById<ViewPager>(R.id.viewPager)
        var tabLayout = findViewById<TabLayout>(R.id.tablayout)

        val fragmentAdapter = VerifyFragmentAdapter(supportFragmentManager)
        fragmentAdapter.addFragment(FrontICFragment(), "1. FRONT")
        fragmentAdapter.addFragment(BackICFragment(), "2. BACK")
        fragmentAdapter.addFragment(SelfieFragment(), "3. SELFIE")

        viewPager.adapter = fragmentAdapter
        tabLayout.setupWithViewPager(viewPager)

    }
}