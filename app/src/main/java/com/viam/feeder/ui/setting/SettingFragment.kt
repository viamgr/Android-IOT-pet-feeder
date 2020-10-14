package com.viam.feeder.ui.setting

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val binding by viewBinding(FragmentSettingBinding::bind)

    private val viewModel: SettingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
            wifiList.setController(viewModel.controller)
        }

        viewModel.itemClicked.observe(viewLifecycleOwner, EventObserver {
            showPasswordDialog()
        })
    }

    private fun showPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_wifi_password, null)
        val inputLayout = dialogView?.findViewById<TextInputLayout>(R.id.password)!!
        val dialog = MaterialAlertDialogBuilder(requireActivity())
            .setView(dialogView)
            .setPositiveButton(R.string.confirm, null)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel, null)
            .create()

        inputLayout.editText?.doAfterTextChanged {
            inputLayout.error = null
        }
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show();
        inputLayout.editText?.requestFocus()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = inputLayout.editText?.text?.toString()
            if (text.isNullOrEmpty() || text.length < 8) {
                inputLayout.error =
                    getString(R.string.input_wrong, getString(R.string.wifi_password))
            } else {
                dialog.dismiss()
                viewModel.onPasswordConfirmed(text)
            }
        }

    }

}