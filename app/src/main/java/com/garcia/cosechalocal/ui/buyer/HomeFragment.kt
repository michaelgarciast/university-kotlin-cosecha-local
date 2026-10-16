package com.garcia.cosechalocal.ui.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.repository.ProductRepository
import com.garcia.cosechalocal.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var adapter: ProductAdapter
    private var selectedCategory: String = "Todo"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        filterProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onProductClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.productDetailFragment, bundle)
            },
            onAddToCartClick = { product ->
                cartViewModel.addToCart(product, 1)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.added_to_cart_toast, product.name, 1),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvNearbyProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvNearbyProducts.adapter = adapter
    }

    private fun setupFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = if (checkedIds.isNotEmpty()) {
                when (checkedIds.first()) {
                    R.id.chipCafe -> getString(R.string.category_cafe)
                    R.id.chipFrutas -> getString(R.string.category_frutas)
                    R.id.chipLacteos -> getString(R.string.category_lacteos)
                    R.id.chipMiel -> getString(R.string.category_miel)
                    R.id.chipPanela -> getString(R.string.category_panela)
                    else -> getString(R.string.category_all)
                }
            } else {
                getString(R.string.category_all)
            }
            filterProducts()
        }
    }

    private fun filterProducts() {
        val query = binding.etSearch.text?.toString()?.trim().orEmpty()
        val filteredList = ProductRepository.filterProducts(query, selectedCategory)
        adapter.submitList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
