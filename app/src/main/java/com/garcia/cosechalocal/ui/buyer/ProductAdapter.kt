package com.garcia.cosechalocal.ui.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.databinding.ItemProductBinding
import com.garcia.cosechalocal.utils.FormatUtils
import java.util.Locale

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProducerName.text = product.producer
            binding.tvDistance.text = String.format(Locale.getDefault(), "%.1f km", product.distanceKm)
            binding.tvProductPrice.text = FormatUtils.formatPrice(product.price)

            binding.btnAddToCart.contentDescription =
                binding.root.context.getString(R.string.cd_add_to_cart)

            binding.root.setOnClickListener {
                onProductClick(product)
            }

            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
