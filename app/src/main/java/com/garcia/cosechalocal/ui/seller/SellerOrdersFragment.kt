package com.garcia.cosechalocal.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.model.OrderStatus
import com.garcia.cosechalocal.databinding.DialogOrderDetailBinding
import com.garcia.cosechalocal.databinding.FragmentSellerOrdersBinding
import com.garcia.cosechalocal.utils.FormatUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SellerOrdersFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SellerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = OrderAdapter(::showOrderDialog)
        binding.rvSellerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSellerOrders.adapter = adapter

        viewModel.filteredOrders.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmptyOrders.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.chipGroupOrders.setOnCheckedStateChangeListener { _, ids ->
            viewModel.setStatusFilter(
                when (ids.firstOrNull()) {
                    R.id.chipOrdersPendiente -> OrderStatus.PENDIENTE
                    R.id.chipOrdersPreparando -> OrderStatus.PREPARANDO
                    R.id.chipOrdersEnviado -> OrderStatus.ENVIADO
                    R.id.chipOrdersEntregado -> OrderStatus.ENTREGADO
                    else -> null
                }
            )
        }
    }

    private fun showOrderDialog(order: Order) {
        val dialogBinding = DialogOrderDetailBinding.inflate(layoutInflater)
        dialogBinding.tvDetailBuyer.text = order.buyerName
        dialogBinding.tvDetailItems.text = order.itemsSummary
        dialogBinding.tvDetailTotal.text = FormatUtils.formatPrice(order.total)
        dialogBinding.tvDetailDate.text = order.date
        dialogBinding.chipGroupStatus.check(
            when (order.status) {
                OrderStatus.PENDIENTE -> R.id.chipStatusPendiente
                OrderStatus.PREPARANDO -> R.id.chipStatusPreparando
                OrderStatus.ENVIADO -> R.id.chipStatusEnviado
                OrderStatus.ENTREGADO -> R.id.chipStatusEntregado
            }
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.order_number_format, order.id))
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val status = when (dialogBinding.chipGroupStatus.checkedChipId) {
                    R.id.chipStatusPreparando -> OrderStatus.PREPARANDO
                    R.id.chipStatusEnviado -> OrderStatus.ENVIADO
                    R.id.chipStatusEntregado -> OrderStatus.ENTREGADO
                    else -> OrderStatus.PENDIENTE
                }
                viewModel.updateOrderStatus(order.id, status)
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setStatusFilter(null)
        _binding = null
    }
}
