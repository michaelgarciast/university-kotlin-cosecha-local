package com.garcia.cosechalocal.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentCheckoutBinding
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.OrderRepository
import com.garcia.cosechalocal.utils.FormatUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTotals()
        cartViewModel.connect()
        setupListeners()
    }

    private fun setupTotals() {
        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.tvSubtotal.text = FormatUtils.formatPrice(subtotal)
        }

        cartViewModel.total.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = FormatUtils.formatPrice(total)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUseLocation.setOnClickListener {
            Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT).show()
        }

        binding.btnPay.setOnClickListener {
            processPayment()
        }
    }

    private fun processPayment() {
        val address = binding.etAddress.text?.toString()?.trim().orEmpty()
        if (address.isEmpty()) {
            binding.tilAddress.error = getString(R.string.delivery_address_hint)
            return
        } else {
            binding.tilAddress.error = null
        }

        val uid = AuthRepository.currentUser?.uid ?: return
        binding.btnPay.isEnabled = false
        OrderRepository.checkout(uid, address, { id ->
            if (_binding != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.order_created_title)
                    .setMessage(getString(R.string.order_created_message, id))
                    .setPositiveButton(R.string.btn_accept) { _, _ ->
                        findNavController().navigate(R.id.navigation_buyer_orders)
                    }.show()
            }
        }, { error ->
            if (_binding != null) {
                binding.btnPay.isEnabled = true
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
