package com.example.carpoolingdriverappv2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.SwitchCompat
import com.example.carpoolingdriverappv2.databinding.ActivitySettingBinding
import java.util.*


class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadLocale()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.btnLanguages.setOnClickListener {
            showChangeLang()
        }

        binding.changePwBtn.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        val switch: SwitchCompat = findViewById(R.id.theme)
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
            else{
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }


    }

    private fun showChangeLang() {
        val listItems = arrayOf("English", "中文")

        val mBuilder = AlertDialog.Builder(this@SettingActivity)
        mBuilder.setTitle("Choose Language")
        mBuilder.setSingleChoiceItems(listItems,-1){ dialog, which ->
            if (which == 0){
                setLocale("en")
                val i1 = Intent(this, MainActivity::class.java)
                i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i1)
                finish()
            }
            else if (which == 1){
                setLocale("zh")
                val i2 = Intent(this, MainActivity::class.java)
                i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i2)
                finish()
            }

            dialog.dismiss()
        }
        val mDialog = mBuilder.create()

        mDialog.show()

    }

    private fun setLocale(Lang: String?) {

        val locale = Locale(Lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", Lang)
        editor.apply()
    }

    private fun loadLocale(){
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        val language = sharedPreferences.getString("My_Lang", "")
        setLocale(language)
    }

}