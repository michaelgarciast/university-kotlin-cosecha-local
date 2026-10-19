package com.garcia.cosechalocal.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentSellerDashboardBinding
import com.garcia.cosechalocal.utils.FormatUtils

class SellerDashboardFragment : Fragment() {

    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SellerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSellerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.monthlySales.observe(viewLifecycleOwner) { binding.tvSalesValue.text = FormatUtils.formatPrice(it) }
        viewModel.pendingOrders.observe(viewLifecycleOwner) { binding.tvPendingValue.text = it.toString() }
        viewModel.activeProducts.observe(viewLifecycleOwner) { binding.tvActiveValue.text = it.toString() }
        binding.btnQuickAddProduct.setOnClickListener {
            findNavController().navigate(R.id.productFormFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
