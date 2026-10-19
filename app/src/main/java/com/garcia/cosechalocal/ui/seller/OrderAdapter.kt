package com.garcia.cosechalocal.ui.seller

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.databinding.ItemOrderBinding
import com.garcia.cosechalocal.utils.FormatUtils

class OrderAdapter(
    private val onClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            val ctx = binding.root.context
            binding.tvOrderNumber.text = ctx.getString(R.string.order_number_format, order.id)
            binding.tvBuyerName.text = order.buyerName
            binding.tvItemsSummary.text = order.itemsSummary
            binding.tvOrderTotal.text = FormatUtils.formatPrice(order.total)
            binding.tvOrderDate.text = order.date
            binding.chipStatus.setText(order.status.labelRes)
            binding.chipStatus.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, order.status.bgColorRes))
            binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, order.status.textColorRes))
            binding.root.contentDescription = ctx.getString(R.string.cd_order_item, order.id)
            binding.root.setOnClickListener { onClick(order) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(a: Order, b: Order) = a.id == b.id
        override fun areContentsTheSame(a: Order, b: Order) = a == b
    }
}
