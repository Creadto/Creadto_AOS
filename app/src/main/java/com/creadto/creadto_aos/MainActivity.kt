package com.creadto.creadto_aos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.creadto.creadto_aos.camera.ui.CameraFragment
import com.creadto.creadto_aos.convert.ConvertFragment
import com.creadto.creadto_aos.convert.network.ApiRemoteSource
import com.creadto.creadto_aos.databinding.ActivityMainBinding
import com.creadto.creadto_aos.gallery.GalleryFragment


class MainActivity : AppCompatActivity() {

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val apiRemoteSource = ApiRemoteSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpBottomNavigationBar()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_container,
                CameraFragment()
            ).commit()

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setUpBottomNavigationBar() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_gallery -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, GalleryFragment()).commit()
                    true
                }

                R.id.nav_camera -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container,
                            CameraFragment()
                        ).commit()
                    true
                }

                R.id.nav_convert -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, ConvertFragment(apiRemoteSource)).commit()
                    true
                }

                else -> false

            }
        }

        binding.bottomNavigation.setOnItemReselectedListener {
            when(it.itemId){
                else -> { }
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_camera

    }
}