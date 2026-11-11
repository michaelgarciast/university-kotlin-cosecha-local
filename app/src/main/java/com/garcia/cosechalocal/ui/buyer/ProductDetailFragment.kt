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
import com.garcia.cosechalocal.utils.ProductImageStore
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
        if (productId == null) {
            showErrorAndExit()
            return
        }

        ProductRepository.getProduct(
            productId = productId,
            onSuccess = { loaded ->
                if (!isAdded) return@getProduct
                if (loaded == null) {
                    showErrorAndExit()
                    return@getProduct
                }
                product = loaded
                setupProductInfo()
                setupListeners()
            },
            onError = {
                if (isAdded) showErrorAndExit()
            }
        )
    }

    private fun setupProductInfo() {
        val p = product ?: return
        binding.tvProductName.text = p.name
        binding.tvProductPrice.text = FormatUtils.formatPrice(p.price)
        binding.chipStock.text = getString(R.string.product_stock, p.stock)
        binding.tvDescription.text = p.description
        binding.tvProducerName.text = p.producer
        binding.tvProducerDistance.visibility =
            if (p.distanceKm > 0.0) View.VISIBLE else View.GONE
        if (p.distanceKm > 0.0) {
            binding.tvProducerDistance.text =
                String.format(Locale.getDefault(), "A %.1f km de tu ubicación", p.distanceKm)
        }
        binding.tvQuantity.text = currentQuantity.toString()
        ProductImageStore.bind(binding.ivProductDetailImage, p)
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

        binding.btnAddToCart.setOnClickListener {
            val p = product ?: return@setOnClickListener
            cartViewModel.addToCart(p, currentQuantity)
            findNavController().navigateUp()
        }
    }

    private fun showErrorAndExit() {
        Toast.makeText(requireContext(), getString(R.string.error_product_not_found), Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
