package com.mochamadahya.mediasantri

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mochamadahya.mediasantri.fragment.HomeFragment
import com.mochamadahya.mediasantri.fragment.NotificationFragment
import com.mochamadahya.mediasantri.fragment.ProfileFragment
import com.mochamadahya.mediasantri.fragment.SearchFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_nav.setOnNavigationItemSelectedListener (onBottomNavListener)

        val frag = supportFragmentManager.beginTransaction()
        frag.add(R.id.frame_container,
            HomeFragment()
        )
        frag.commit()

    }

    private val onBottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener { i->

        var selectedFragment: Fragment =
            HomeFragment()

        when (i.itemId){
            R.id.itemHome -> {
                selectedFragment =
                    HomeFragment()
            }
            R.id.itemSearch -> {
                selectedFragment =
                    SearchFragment()
            }
            R.id.itemAddPost -> {
                startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
            }
            R.id.itemFavourite -> {
                selectedFragment = NotificationFragment()
            }
            R.id.itemProfile -> {
                selectedFragment =
                    ProfileFragment()
            }


        }

        val frag = supportFragmentManager.beginTransaction()
        frag.replace(R.id.frame_container, selectedFragment)
        frag.commit()

        true

    }
}