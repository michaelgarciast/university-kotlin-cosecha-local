package com.garcia.cosechalocal.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.CartItem
import com.garcia.cosechalocal.databinding.ItemCartBinding
import com.garcia.cosechalocal.utils.FormatUtils

class CartAdapter(
    private val onIncreaseClick: (CartItem) -> Unit,
    private val onDecreaseClick: (CartItem) -> Unit,
    private val onRemoveClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val context = binding.root.context
            val product = item.product

            binding.tvProductName.text = product.name
            binding.tvProducerName.text = product.producer
            binding.tvUnitPrice.text = context.getString(
                R.string.unit_price_format,
                FormatUtils.formatPrice(product.price)
            )
            binding.tvQuantity.text = item.quantity.toString()

            val itemTotal = product.price * item.quantity
            binding.tvItemSubtotal.text = FormatUtils.formatPrice(itemTotal)

            binding.btnIncrease.setOnClickListener {
                onIncreaseClick(item)
            }

            binding.btnDecrease.setOnClickListener {
                onDecreaseClick(item)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.quantity == newItem.quantity && oldItem.product == newItem.product
        }
    }
}
