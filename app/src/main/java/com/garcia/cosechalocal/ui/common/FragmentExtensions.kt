package com.garcia.cosechalocal.ui.common

import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.ui.buyer.CartViewModel

fun Fragment.logoutToLogin() {
    AuthRepository.signOut()
    requireActivity().let { activity ->
        androidx.lifecycle.ViewModelProvider(activity)[CartViewModel::class.java].connect()
    }
    findNavController().navigate(
        R.id.loginFragment,
        null,
        NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
    )
}
