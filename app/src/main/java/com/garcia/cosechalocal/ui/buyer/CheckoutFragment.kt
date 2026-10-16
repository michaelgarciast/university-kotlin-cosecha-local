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
            binding.etAddress.setText(getString(R.string.simulated_address))
            binding.tilAddress.error = null
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

        val selectedPayment = when (binding.rgPaymentMethods.checkedRadioButtonId) {
            R.id.rbCard -> getString(R.string.payment_card)
            R.id.rbPse -> getString(R.string.payment_pse)
            R.id.rbNequi -> getString(R.string.payment_nequi)
            else -> {
                Toast.makeText(requireContext(), getString(R.string.error_select_payment), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val totalAmount = cartViewModel.total.value ?: 0
        val formattedTotal = FormatUtils.formatPrice(totalAmount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.checkout_dialog_title))
            .setMessage(getString(R.string.checkout_dialog_msg, address, selectedPayment, formattedTotal))
            .setPositiveButton(getString(R.string.btn_accept)) { dialog, _ ->
                dialog.dismiss()
                cartViewModel.clearCart()
                findNavController().navigate(R.id.navigation_buyer_home)
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
