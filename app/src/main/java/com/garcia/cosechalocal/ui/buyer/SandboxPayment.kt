package com.garcia.cosechalocal.ui.buyer

import android.content.Context
import android.widget.Toast
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.repository.OrderRepository
import com.garcia.cosechalocal.utils.FormatUtils
import com.garcia.cosechalocal.utils.userMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object SandboxPayment {
    fun show(context: Context, orderId: String, total: Int, onDone: () -> Unit = {}) {
        val options = arrayOf(
            context.getString(R.string.pay_sandbox_approved),
            context.getString(R.string.pay_sandbox_rejected),
            context.getString(R.string.pay_sandbox_pending)
        )
        var choice = 0
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.pay_sandbox_title, FormatUtils.formatPrice(total)))
            .setSingleChoiceItems(options, 0) { _, which -> choice = which }
            .setPositiveButton(R.string.pay_sandbox_confirm) { _, _ ->
                if (choice == 2) {
                    Toast.makeText(context, R.string.pay_result_pending, Toast.LENGTH_LONG).show()
                    onDone()
                    return@setPositiveButton
                }
                OrderRepository.payWithSandbox(orderId, choice == 0, { status, ref ->
                    val msg = if (status == "APROBADO") context.getString(R.string.pay_result_approved, ref.orEmpty())
                    else context.getString(R.string.pay_result_rejected)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    onDone()
                }, { error ->
                    Toast.makeText(context, error.userMessage(context), Toast.LENGTH_LONG).show()
                    onDone()
                })
            }
            .setNegativeButton(R.string.btn_cancel) { _, _ -> onDone() }
            .setOnCancelListener { onDone() }
            .show()
    }
}
