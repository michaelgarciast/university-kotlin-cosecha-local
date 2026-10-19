package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.User
import com.garcia.cosechalocal.databinding.FragmentUserFormBinding
import com.garcia.cosechalocal.ui.common.UserRole
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UserFormFragment : Fragment() {

    private var _binding: FragmentUserFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private var editing: User? = null
    private val roles = UserRole.values()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editing = viewModel.getUser(arguments?.getInt("userId", -1) ?: -1)

        val labels = roles.map { getString(it.labelRes) }
        binding.actvRole.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels))

        binding.tvFormTitle.setText(if (editing == null) R.string.user_form_title_new else R.string.user_form_title_edit)
        binding.btnDeleteUser.visibility = if (editing == null) View.GONE else View.VISIBLE
        if (savedInstanceState == null) {
            val user = editing
            binding.actvRole.setText(getString((user?.role ?: UserRole.BUYER).labelRes), false)
            binding.switchActive.isChecked = user?.active ?: true
            user?.let {
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
            }
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSave.setOnClickListener { save() }
        binding.btnDeleteUser.setOnClickListener { confirmDelete() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val role = roles.firstOrNull { getString(it.labelRes) == binding.actvRole.text.toString() }

        binding.tilName.error = if (name.isEmpty()) getString(R.string.error_name_required) else null
        binding.tilEmail.error = when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            viewModel.emailExists(email, editing?.id) -> getString(R.string.error_email_duplicate)
            else -> null
        }
        binding.tilRole.error = if (role == null) getString(R.string.error_role_required) else null
        if (listOf(binding.tilName, binding.tilEmail, binding.tilRole).any { it.error != null }) return

        val active = binding.switchActive.isChecked
        editing?.let { viewModel.updateUser(it.copy(name = name, email = email, role = role!!, active = active)) }
            ?: viewModel.createUser(name, email, role!!, active)
        findNavController().navigateUp()
    }

    private fun confirmDelete() {
        val user = editing ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_user_title)
            .setMessage(getString(R.string.delete_user_message, user.name))
            .setPositiveButton(R.string.btn_confirm_delete) { _, _ ->
                viewModel.deleteUser(user.id)
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
