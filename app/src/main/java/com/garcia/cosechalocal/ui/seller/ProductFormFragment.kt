package com.garcia.cosechalocal.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.repository.SellerRepository
import com.garcia.cosechalocal.databinding.FragmentProductFormBinding
import com.google.android.material.snackbar.Snackbar

class ProductFormFragment : Fragment() {

    private var _binding: FragmentProductFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SellerViewModel by activityViewModels()
    private var editing: Product? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editing = viewModel.getProduct(arguments?.getString("productId"))

        val categories = resources.getStringArray(R.array.product_categories)
        binding.actvCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories))

        binding.tvFormTitle.setText(if (editing == null) R.string.form_title_new else R.string.form_title_edit)
        if (savedInstanceState == null) editing?.let {
            binding.etName.setText(it.name)
            binding.etPrice.setText(it.price.toString())
            binding.etStock.setText(it.stock.toString())
            binding.actvCategory.setText(it.category, false)
            binding.etDescription.setText(it.description)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnTakePhoto.setOnClickListener {
            Snackbar.make(binding.root, R.string.photo_simulated, Snackbar.LENGTH_SHORT).show()
        }
        binding.btnUseLocation.setOnClickListener {
            Snackbar.make(binding.root, R.string.location_simulated, Snackbar.LENGTH_SHORT).show()
        }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim().toIntOrNull()
        val stock = binding.etStock.text.toString().trim().toIntOrNull()
        val category = binding.actvCategory.text.toString().trim()

        binding.tilName.error = if (name.isEmpty()) getString(R.string.error_name_required) else null
        binding.tilPrice.error = if (price == null || price <= 0) getString(R.string.error_price_invalid) else null
        binding.tilStock.error = if (stock == null || stock < 0) getString(R.string.error_stock_invalid) else null
        binding.tilCategory.error = if (category.isEmpty()) getString(R.string.error_category_required) else null

        if (listOf(binding.tilName, binding.tilPrice, binding.tilStock, binding.tilCategory).any { it.error != null }) return

        val product = Product(
            id = editing?.id ?: viewModel.newProductId(),
            name = name,
            producer = SellerRepository.STORE_NAME,
            distanceKm = editing?.distanceKm ?: 0.0,
            price = price!!,
            stock = stock!!,
            category = category,
            description = binding.etDescription.text.toString().trim()
        )
        if (editing == null) viewModel.createProduct(product) else viewModel.updateProduct(product)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
