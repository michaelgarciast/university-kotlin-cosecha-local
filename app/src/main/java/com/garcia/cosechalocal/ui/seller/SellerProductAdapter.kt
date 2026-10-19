package com.garcia.cosechalocal.ui.seller

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.databinding.ItemSellerProductBinding
import com.garcia.cosechalocal.utils.FormatUtils

class SellerProductAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : ListAdapter<Product, SellerProductAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemSellerProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemSellerProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            val ctx = binding.root.context
            val low = product.stock <= LOW_STOCK
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = FormatUtils.formatPrice(product.price)
            binding.chipStock.text = ctx.getString(
                if (low) R.string.stock_low_format else R.string.stock_format, product.stock
            )
            binding.chipStock.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, if (low) R.color.stock_low_bg else R.color.chip_selected_bg)
            )
            binding.chipStock.setTextColor(
                ContextCompat.getColor(ctx, if (low) R.color.error_red else R.color.primary_green)
            )
            binding.btnEdit.contentDescription = ctx.getString(R.string.cd_edit_product, product.name)
            binding.btnDelete.contentDescription = ctx.getString(R.string.cd_delete_product, product.name)
            binding.btnEdit.setOnClickListener { onEdit(product) }
            binding.btnDelete.setOnClickListener { onDelete(product) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
        override fun areContentsTheSame(a: Product, b: Product) = a == b
    }

    companion object {
        const val LOW_STOCK = 5
    }
}
