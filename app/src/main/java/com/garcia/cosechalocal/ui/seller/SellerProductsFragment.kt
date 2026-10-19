package com.garcia.cosechalocal.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.databinding.FragmentSellerProductsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SellerProductsFragment : Fragment() {

    private var _binding: FragmentSellerProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SellerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSellerProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SellerProductAdapter(
            onEdit = { findNavController().navigate(R.id.productFormFragment, bundleOf("productId" to it.id)) },
            onDelete = ::confirmDelete
        )
        binding.rvSellerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSellerProducts.adapter = adapter

        viewModel.filteredProducts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.emptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.rvSellerProducts.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.etSearchProducts.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
        binding.fabAddProduct.setOnClickListener { findNavController().navigate(R.id.productFormFragment) }
    }

    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_product_title)
            .setMessage(getString(R.string.delete_product_message, product.name))
            .setPositiveButton(R.string.btn_confirm_delete) { _, _ ->
                viewModel.deleteProduct(product.id)
                Snackbar.make(binding.root, R.string.product_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setQuery("")
        _binding = null
    }
}
