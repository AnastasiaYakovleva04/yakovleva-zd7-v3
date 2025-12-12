package com.example.yakovleva_zd7_v3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment

class MainClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_client)

        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
        navView.setBackgroundColor(ContextCompat.getColor(this, R.color.matcha))
        navView.menu.findItem(R.id.catalogFragment).icon =
            ContextCompat.getDrawable(this, R.drawable.catalog)
        navView.menu.findItem(R.id.cartFragment).icon =
            ContextCompat.getDrawable(this, R.drawable.cart)
        navView.menu.findItem(R.id.clientOrdersFragment).icon =
            ContextCompat.getDrawable(this, R.drawable.orders)
        navView.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

    }
}