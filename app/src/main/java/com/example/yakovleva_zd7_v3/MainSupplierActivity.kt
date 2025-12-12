package com.example.yakovleva_zd7_v3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainSupplierActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_supplier)

        val userId = intent.getIntExtra("USER_ID", -1)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        prefs.edit().putInt("user_id", userId).apply()

        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_supplier)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_supplier) as NavHostFragment

        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
        navView.setBackgroundColor(ContextCompat.getColor(this, R.color.matcha))

        navView.menu.findItem(R.id.partsListFragment).icon =
            ContextCompat.getDrawable(this, R.drawable.details)
        navView.menu.findItem(R.id.addPartFragment).icon =
            ContextCompat.getDrawable(this, R.drawable.add)

        navView.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
    }
}