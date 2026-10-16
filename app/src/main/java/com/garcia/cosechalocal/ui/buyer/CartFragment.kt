package com.garcia.cosechalocal.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentCartBinding
import com.garcia.cosechalocal.utils.FormatUtils

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCart()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            onIncreaseClick = { item ->
                cartViewModel.updateQuantity(item.product.id, item.quantity + 1)
            },
            onDecreaseClick = { item ->
                cartViewModel.updateQuantity(item.product.id, item.quantity - 1)
            },
            onRemoveClick = { item ->
                cartViewModel.removeFromCart(item.product.id)
            }
        )

        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter
    }

    private fun observeCart() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            if (items.isNullOrEmpty()) {
                binding.layoutEmptyCart.visibility = View.VISIBLE
                binding.rvCartItems.visibility = View.GONE
                binding.layoutSummary.visibility = View.GONE
            } else {
                binding.layoutEmptyCart.visibility = View.GONE
                binding.rvCartItems.visibility = View.VISIBLE
                binding.layoutSummary.visibility = View.VISIBLE
            }
        }

        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.tvSubtotal.text = FormatUtils.formatPrice(subtotal)
        }

        cartViewModel.total.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = FormatUtils.formatPrice(total)
        }
    }

    private fun setupListeners() {
        binding.btnExploreProducts.setOnClickListener {
            findNavController().navigate(R.id.navigation_buyer_home)
        }

        binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_buyer_cart_to_checkout)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
