package com.garcia.cosechalocal.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.ActivityMainBinding
import com.garcia.cosechalocal.ui.buyer.CartViewModel
import com.garcia.cosechalocal.ui.common.UserRole

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.recoverPasswordFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }

        setupCartBadgeObserver()
    }

    private fun setupCartBadgeObserver() {
        cartViewModel.itemCount.observe(this) { count ->
            val cartMenuItem = binding.bottomNavigation.menu.findItem(R.id.navigation_buyer_cart)
            if (cartMenuItem != null) {
                val badge = binding.bottomNavigation.getOrCreateBadge(R.id.navigation_buyer_cart)
                if (count > 0) {
                    badge.number = count
                    badge.isVisible = true
                    badge.backgroundColor = ContextCompat.getColor(this, R.color.amber_accent)
                    badge.badgeTextColor = ContextCompat.getColor(this, R.color.dark_green)
                } else {
                    badge.isVisible = false
                }
            }
        }
    }

    fun setupRoleMenu(role: UserRole) {
        val menuRes = when (role) {
            UserRole.BUYER -> R.menu.menu_buyer
            UserRole.SELLER -> R.menu.menu_seller
            UserRole.ADMIN -> R.menu.menu_admin
        }

        val startDestId = when (role) {
            UserRole.BUYER -> R.id.navigation_buyer_home
            UserRole.SELLER -> R.id.navigation_seller_dashboard
            UserRole.ADMIN -> R.id.navigation_admin_dashboard
        }

        binding.bottomNavigation.menu.clear()
        binding.bottomNavigation.inflateMenu(menuRes)
        binding.bottomNavigation.setupWithNavController(navController)

        if (role == UserRole.BUYER) {
            val count = cartViewModel.itemCount.value ?: 0
            val badge = binding.bottomNavigation.getOrCreateBadge(R.id.navigation_buyer_cart)
            if (count > 0) {
                badge.number = count
                badge.isVisible = true
                badge.backgroundColor = ContextCompat.getColor(this, R.color.amber_accent)
                badge.badgeTextColor = ContextCompat.getColor(this, R.color.dark_green)
            } else {
                badge.isVisible = false
            }
        }

        navController.navigate(startDestId)
    }
}
