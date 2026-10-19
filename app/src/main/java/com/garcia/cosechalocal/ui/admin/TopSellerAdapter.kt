package com.garcia.cosechalocal.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.data.model.SellerSales
import com.garcia.cosechalocal.databinding.ItemTopSellerBinding
import com.garcia.cosechalocal.utils.FormatUtils

class TopSellerAdapter : ListAdapter<SellerSales, TopSellerAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemTopSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position + 1, getItem(position))

    class ViewHolder(private val binding: ItemTopSellerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rank: Int, item: SellerSales) {
            binding.tvRank.text = rank.toString()
            binding.tvSellerName.text = item.name
            binding.tvSellerAmount.text = FormatUtils.formatMillions(item.amount)
        }
    }

    class Diff : DiffUtil.ItemCallback<SellerSales>() {
        override fun areItemsTheSame(a: SellerSales, b: SellerSales) = a.name == b.name
        override fun areContentsTheSame(a: SellerSales, b: SellerSales) = a == b
    }
}
