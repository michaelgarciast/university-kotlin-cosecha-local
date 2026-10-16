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
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.repository.ProductRepository
import com.garcia.cosechalocal.databinding.FragmentProductDetailBinding
import com.garcia.cosechalocal.utils.FormatUtils
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private var product: Product? = null
    private var currentQuantity: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId")
        if (productId != null) {
            product = ProductRepository.getProductById(productId)
        }

        if (product == null) {
            Toast.makeText(requireContext(), "Producto no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupProductInfo()
        setupListeners()
    }

    private fun setupProductInfo() {
        val p = product ?: return
        binding.tvProductName.text = p.name
        binding.tvProductPrice.text = FormatUtils.formatPrice(p.price)
        binding.chipStock.text = getString(R.string.product_stock, p.stock)
        binding.tvDescription.text = p.description
        binding.tvProducerName.text = p.producer
        binding.tvProducerDistance.text = String.format(Locale.getDefault(), "A %.1f km de tu ubicación", p.distanceKm)
        binding.tvQuantity.text = currentQuantity.toString()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnDecrease.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }

        binding.btnIncrease.setOnClickListener {
            val maxStock = product?.stock ?: 99
            if (currentQuantity < maxStock) {
                currentQuantity++
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }

        binding.btnViewMap.setOnClickListener {
            val producer = product?.producer.orEmpty()
            Toast.makeText(
                requireContext(),
                getString(R.string.producer_map_simulated, producer),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnAddToCart.setOnClickListener {
            val p = product ?: return@setOnClickListener
            cartViewModel.addToCart(p, currentQuantity)
            Toast.makeText(
                requireContext(),
                getString(R.string.added_to_cart_toast, p.name, currentQuantity),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
