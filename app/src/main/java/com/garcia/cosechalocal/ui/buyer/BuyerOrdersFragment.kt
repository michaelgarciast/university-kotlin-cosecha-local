package com.garcia.cosechalocal.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.OrderRepository
import com.garcia.cosechalocal.databinding.FragmentBuyerOrdersBinding
import com.garcia.cosechalocal.ui.seller.OrderAdapter
import com.garcia.cosechalocal.utils.FormatUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration

class BuyerOrdersFragment : Fragment() {
    private var _binding: FragmentBuyerOrdersBinding? = null
    private val binding get() = _binding!!
    private var registration: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuyerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uid = AuthRepository.currentUser?.uid ?: return
        val adapter = OrderAdapter(::showOrder)
        binding.rvBuyerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBuyerOrders.adapter = adapter
        binding.tvEmptyOrders.visibility = View.GONE
        registration = OrderRepository.observeBuyer(uid, { orders ->
            if (_binding != null) {
                adapter.submitList(orders)
                binding.tvEmptyOrders.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            }
        }, { error ->
            if (_binding != null) Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
        })
    }

    private fun showOrder(order: Order) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.order_number_format, order.id))
            .setMessage(getString(R.string.order_detail_text, order.itemsSummary, order.address,
                order.paymentStatus, FormatUtils.formatPrice(order.total)))
            .setNegativeButton(R.string.btn_accept, null)
        if (order.paymentStatus == "PENDIENTE_PAGO") {
            dialog.setPositiveButton(R.string.cancel_order) { _, _ ->
                OrderRepository.cancel(order.id, {}, { error ->
                    if (isAdded) Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                })
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        registration?.remove()
        _binding = null
        super.onDestroyView()
    }
}
