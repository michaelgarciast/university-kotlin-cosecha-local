package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentAdminUsersBinding
import com.garcia.cosechalocal.ui.common.UserRole

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = UserAdapter(
            onEdit = { findNavController().navigate(R.id.userFormFragment, bundleOf("userId" to it.id)) },
            onToggleBlock = { viewModel.toggleBlock(it.id) }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        binding.etSearchUsers.setText(viewModel.query)
        binding.chipGroupUsers.check(
            when (viewModel.roleFilter) {
                UserRole.BUYER -> R.id.chipUsersBuyers
                UserRole.SELLER -> R.id.chipUsersSellers
                else -> R.id.chipUsersAll
            }
        )

        viewModel.filteredUsers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmptyUsers.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        binding.etSearchUsers.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
        binding.chipGroupUsers.setOnCheckedStateChangeListener { _, ids ->
            viewModel.setRoleFilter(
                when (ids.firstOrNull()) {
                    R.id.chipUsersBuyers -> UserRole.BUYER
                    R.id.chipUsersSellers -> UserRole.SELLER
                    else -> null
                }
            )
        }
        binding.fabAddUser.setOnClickListener { findNavController().navigate(R.id.userFormFragment) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
