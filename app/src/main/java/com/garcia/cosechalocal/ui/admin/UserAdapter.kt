package com.garcia.cosechalocal.ui.admin

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.User
import com.garcia.cosechalocal.databinding.ItemUserBinding

class UserAdapter(
    private val onEdit: (User) -> Unit,
    private val onToggleBlock: (User) -> Unit
) : ListAdapter<User, UserAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            val ctx = binding.root.context
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            binding.chipRole.setText(user.role.labelRes)
            binding.chipRole.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, user.role.bgColorRes))
            binding.chipRole.setTextColor(ContextCompat.getColor(ctx, user.role.textColorRes))

            val statusColor = ContextCompat.getColor(ctx, if (user.active) R.color.primary_green else R.color.error_red)
            binding.viewStatusDot.backgroundTintList = ColorStateList.valueOf(statusColor)
            binding.tvAccountStatus.setText(if (user.active) R.string.account_active else R.string.account_blocked)
            binding.tvAccountStatus.setTextColor(statusColor)
            binding.layoutStatus.contentDescription = ctx.getString(
                if (user.active) R.string.cd_block_user else R.string.cd_unblock_user, user.name
            )
            binding.layoutStatus.setOnClickListener { onToggleBlock(user) }
            binding.btnEditUser.contentDescription = ctx.getString(R.string.cd_edit_user, user.name)
            binding.btnEditUser.setOnClickListener { onEdit(user) }
        }
    }

    class Diff : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(a: User, b: User) = a.id == b.id
        override fun areContentsTheSame(a: User, b: User) = a == b
    }
}
