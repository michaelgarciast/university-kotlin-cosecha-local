package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.data.repository.AdminRepository
import com.garcia.cosechalocal.databinding.FragmentAdminDashboardBinding
import com.garcia.cosechalocal.utils.FormatUtils
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"))
        binding.tvUsersValue.text = nf.format(AdminRepository.TOTAL_USERS)
        binding.tvSellersValue.text = nf.format(AdminRepository.TOTAL_SELLERS)
        binding.tvProductsValue.text = nf.format(AdminRepository.TOTAL_PRODUCTS)
        binding.tvSalesTodayValue.text = FormatUtils.formatMillions(AdminRepository.SALES_TODAY)

        val orders = AdminRepository.getOrdersLast7Days()
        binding.chartOrders.setData(orders, orders.map { it.value.toString() })

        viewModel.pendingApprovals.observe(viewLifecycleOwner) { binding.tvPendingCount.text = it.toString() }
        binding.cardPendingApproval.setOnClickListener {
            viewModel.setStatusFilter(ModerationStatus.POR_APROBAR)
            findNavController().navigate(R.id.navigation_admin_products)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
