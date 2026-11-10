package com.garcia.cosechalocal.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.databinding.FragmentSellerProfileBinding
import com.garcia.cosechalocal.ui.common.logoutToLogin

class SellerProfileFragment : Fragment() {

    private var _binding: FragmentSellerProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSellerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AuthRepository.loadCurrentProfile(
            onSuccess = { profile ->
                if (!isAdded) return@loadCurrentProfile
                binding.tvStoreName.text = profile.name
                binding.tvStoreEmail.text = profile.email
            },
            onError = {
                // loadCurrentProfile ya cerró la sesión si el perfil no existe
            }
        )

        binding.btnLogout.setOnClickListener { logoutToLogin() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
