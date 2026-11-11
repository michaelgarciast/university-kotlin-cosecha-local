package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.data.model.ProductModeration
import com.garcia.cosechalocal.databinding.FragmentAdminProductsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AdminProductAdapter(
            onApprove = { viewModel.setProductStatus(it.product.id, ModerationStatus.APROBADO) },
            onSuspend = { viewModel.setProductStatus(it.product.id, ModerationStatus.SUSPENDIDO) },
            onDelete = ::confirmDelete
        )
        binding.rvAdminProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminProducts.adapter = adapter

        binding.chipGroupProducts.check(
            when (viewModel.statusFilter) {
                ModerationStatus.POR_APROBAR -> R.id.chipProductsPending
                ModerationStatus.APROBADO -> R.id.chipProductsApproved
                ModerationStatus.SUSPENDIDO -> R.id.chipProductsSuspended
                null -> R.id.chipProductsAll
            }
        )
        binding.chipGroupProducts.setOnCheckedStateChangeListener { _, ids ->
            viewModel.setStatusFilter(
                when (ids.firstOrNull()) {
                    R.id.chipProductsPending -> ModerationStatus.POR_APROBAR
                    R.id.chipProductsApproved -> ModerationStatus.APROBADO
                    R.id.chipProductsSuspended -> ModerationStatus.SUSPENDIDO
                    else -> null
                }
            )
        }
        viewModel.productsLoading.observe(viewLifecycleOwner) { loading ->
            val showEmpty = !loading && viewModel.filteredProducts.value.isNullOrEmpty()
            binding.tvEmptyProducts.visibility = if (showEmpty) View.VISIBLE else View.GONE
        }
        viewModel.productsError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showToast(R.string.error_product_status)
                viewModel.clearProductsError()
            }
        }
        viewModel.filteredProducts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            val showEmpty = it.isEmpty() && viewModel.productsLoading.value != true
            binding.tvEmptyProducts.visibility = if (showEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun showToast(messageRes: Int) {
        if (!isAdded) return
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    private fun confirmDelete(item: ProductModeration) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_product_title)
            .setMessage(getString(R.string.admin_delete_product_message, item.product.name))
            .setPositiveButton(R.string.btn_confirm_delete) { _, _ -> viewModel.deleteProduct(item.product.id) }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
