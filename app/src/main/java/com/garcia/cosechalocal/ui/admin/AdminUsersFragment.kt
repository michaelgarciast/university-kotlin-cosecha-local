package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            onEdit = {
                val args = Bundle().apply { putString("userId", it.uid) }
                findNavController().navigate(R.id.userFormFragment, args)
            },
            onToggleBlock = { user ->
                if (viewModel.isSelf(user.uid)) {
                    showToast(R.string.error_self_account)
                } else {
                    viewModel.setUserActive(
                        uid = user.uid,
                        active = !user.active,
                        onSuccess = {},
                        onError = { showToast(R.string.error_user_status) }
                    )
                }
            }
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

        viewModel.usersLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressUsers.visibility = if (loading) View.VISIBLE else View.GONE
            if (loading) binding.tvEmptyUsers.visibility = View.GONE
        }
        viewModel.usersError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showToast(R.string.error_users_load)
                viewModel.clearUsersError()
            }
        }
        viewModel.filteredUsers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            val showEmpty = it.isEmpty() && viewModel.usersLoading.value != true
            binding.tvEmptyUsers.visibility = if (showEmpty) View.VISIBLE else View.GONE
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

    private fun showToast(messageRes: Int) {
        if (!isAdded) return
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
