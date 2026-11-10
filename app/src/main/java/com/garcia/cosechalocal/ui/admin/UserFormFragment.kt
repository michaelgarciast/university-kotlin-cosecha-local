package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.UserProfile
import com.garcia.cosechalocal.databinding.FragmentUserFormBinding
import com.garcia.cosechalocal.ui.common.UserRole
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class UserFormFragment : Fragment() {

    private var _binding: FragmentUserFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()
    private var editing: UserProfile? = null
    private val roles = UserRole.values()

    private val editingSelf get() = editing?.let { viewModel.isSelf(it.uid) } == true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editing = viewModel.getUser(arguments?.getString("userId"))

        val labels = roles.map { getString(it.labelRes) }
        binding.actvRole.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels))

        binding.tvFormTitle.setText(if (editing == null) R.string.user_form_title_new else R.string.user_form_title_edit)
        binding.btnDeleteUser.visibility = if (editing == null || editingSelf) View.GONE else View.VISIBLE
        if (savedInstanceState == null) {
            val user = editing
            binding.actvRole.setText(getString((user?.role ?: UserRole.BUYER).labelRes), false)
            binding.switchActive.isChecked = user?.active ?: true
            user?.let {
                binding.etName.setText(it.name)
                binding.etEmail.setText(it.email)
            }
        }

        // El correo de Authentication de otra cuenta no se puede cambiar desde
        // el cliente; el documento debe coincidir con esa credencial.
        if (editing != null) {
            binding.tilEmail.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.tilEmail.helperText = getString(R.string.user_form_email_locked)
        }
        // Evita que el administrador se quite el rol o se desactive a sí mismo.
        if (editingSelf) {
            binding.tilRole.isEnabled = false
            binding.actvRole.isEnabled = false
            binding.switchActive.isEnabled = false
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
        if (editing == null) {
            binding.tilEmail.error = when {
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
                viewModel.emailExists(email, null) -> getString(R.string.error_email_duplicate)
                else -> null
            }
        }
        binding.tilRole.error = if (role == null) getString(R.string.error_role_required) else null
        if (listOf(binding.tilName, binding.tilEmail, binding.tilRole).any { it.error != null }) return

        val active = binding.switchActive.isChecked
        val current = editing
        setLoading(true)
        if (current == null) {
            viewModel.createUser(
                name = name,
                email = email,
                role = role!!,
                active = active,
                onSuccess = {
                    if (!isAdded) return@createUser
                    showToast(R.string.user_created_reset_sent)
                    findNavController().navigateUp()
                },
                onError = { error -> onOperationError(error, R.string.error_user_save) }
            )
        } else {
            viewModel.updateUser(
                uid = current.uid,
                name = name,
                role = if (editingSelf) current.role else role!!,
                active = if (editingSelf) current.active else active,
                onSuccess = {
                    if (!isAdded) return@updateUser
                    showToast(R.string.user_updated)
                    findNavController().navigateUp()
                },
                onError = { error -> onOperationError(error, R.string.error_user_save) }
            )
        }
    }

    private fun confirmDelete() {
        val user = editing ?: return
        if (viewModel.isSelf(user.uid)) {
            showToast(R.string.error_self_account)
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_user_title)
            .setMessage(getString(R.string.delete_user_message, user.name))
            .setPositiveButton(R.string.btn_confirm_delete) { _, _ ->
                setLoading(true)
                viewModel.deleteUser(
                    uid = user.uid,
                    onSuccess = {
                        if (!isAdded) return@deleteUser
                        showToast(R.string.user_deleted)
                        findNavController().navigateUp()
                    },
                    onError = { error -> onOperationError(error, R.string.error_user_delete) }
                )
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun onOperationError(error: Exception, fallbackRes: Int) {
        if (!isAdded) return
        setLoading(false)
        val messageRes = if (error is FirebaseAuthUserCollisionException) {
            R.string.error_email_duplicate
        } else {
            fallbackRes
        }
        showToast(messageRes)
    }

    private fun setLoading(loading: Boolean) {
        val currentBinding = _binding ?: return
        currentBinding.btnSave.isEnabled = !loading
        currentBinding.btnSave.alpha = if (loading) 0.6f else 1f
        currentBinding.btnDeleteUser.isEnabled = !loading
        currentBinding.btnDeleteUser.alpha = if (loading) 0.6f else 1f
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
