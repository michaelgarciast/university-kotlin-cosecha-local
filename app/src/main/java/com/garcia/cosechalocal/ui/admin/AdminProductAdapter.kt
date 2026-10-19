package com.garcia.cosechalocal.ui.admin

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.ProductModeration
import com.garcia.cosechalocal.databinding.ItemAdminProductBinding
import com.garcia.cosechalocal.utils.FormatUtils

class AdminProductAdapter(
    private val onApprove: (ProductModeration) -> Unit,
    private val onSuspend: (ProductModeration) -> Unit,
    private val onDelete: (ProductModeration) -> Unit
) : ListAdapter<ProductModeration, AdminProductAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAdminProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemAdminProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductModeration) {
            val ctx = binding.root.context
            val name = item.product.name
            binding.tvProductName.text = name
            binding.tvSellerName.text = item.sellerName
            binding.tvProductPrice.text = FormatUtils.formatPrice(item.product.price)
            binding.chipProductStatus.setText(item.status.labelRes)
            binding.chipProductStatus.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, item.status.bgColorRes))
            binding.chipProductStatus.setTextColor(ContextCompat.getColor(ctx, item.status.textColorRes))
            binding.btnApprove.contentDescription = ctx.getString(R.string.cd_approve_product, name)
            binding.btnSuspend.contentDescription = ctx.getString(R.string.cd_suspend_product, name)
            binding.btnDeleteProduct.contentDescription = ctx.getString(R.string.cd_delete_product, name)
            binding.btnApprove.setOnClickListener { onApprove(item) }
            binding.btnSuspend.setOnClickListener { onSuspend(item) }
            binding.btnDeleteProduct.setOnClickListener { onDelete(item) }
        }
    }

    class Diff : DiffUtil.ItemCallback<ProductModeration>() {
        override fun areItemsTheSame(a: ProductModeration, b: ProductModeration) = a.product.id == b.product.id
        override fun areContentsTheSame(a: ProductModeration, b: ProductModeration) = a == b
    }
}
