package com.example.fitnessapp.presentation

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isFirstRun = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        if (isFirstRun) {
            loadFragment(StartScreenFragment())
            getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply()
        } else {
            loadFragment(LoginPageFragment())
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            handleNavigationItemClick(item.itemId)
            true
        }
    }

    private fun handleNavigationItemClick(itemId: Int) {
        val fragment = when (itemId) {
            R.id.home -> HomePageFragment()
            R.id.stats -> StatisticPageFragment()
            R.id.profile -> ProfilePageFragment()
            else -> null
        }
        fragment?.let { loadFragment(it) }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack("null", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.fragment_container, fragment)
            .commit()
        updateBottomNavigationVisibility(fragment)
    }

    fun loadFragmentWithBackStack(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("null")
            .commit()
        updateBottomNavigationVisibility(fragment)
    }

    private fun updateBottomNavigationVisibility(fragment: Fragment) {
        if (fragment is HomePageFragment || fragment is StatisticPageFragment || fragment is ProfilePageFragment) {
            binding.bottomNavigationView.visibility = View.VISIBLE
        } else {
            binding.bottomNavigationView.visibility = View.GONE
        }
    }
}
