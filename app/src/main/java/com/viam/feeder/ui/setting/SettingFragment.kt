package com.viam.feeder.ui.setting

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.utils.toMessage
import com.viam.feeder.databinding.FragmentSettingBinding
import com.viam.feeder.model.WifiDevice
import com.viam.resource.onError
import com.viam.resource.onSuccess
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.fragment_setting) {

    private val binding by viewBinding(FragmentSettingBinding::bind)

    private val viewModel: SettingViewModel by viewModels()
    private val controller = WifiController()
        .also { wifiController ->
            wifiController.clickListener = {
                showPasswordDialog(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
            wifiList.setController(controller)
        }

        viewModel.getWifiListTask.observe(viewLifecycleOwner, {
            it?.onSuccess { list ->
                controller.setData(list)
            }?.onError { exception ->
                controller.setData(emptyList())
                Toast.makeText(requireContext(), exception.toMessage(requireContext()), Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun showPasswordDialog(wifiDevice: WifiDevice) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_wifi_password, null)
        val inputLayout = dialogView?.findViewById<TextInputLayout>(R.id.password)!!
        val staticIpConfiguration = dialogView.findViewById<MaterialCheckBox>(R.id.static_ip_configuration)!!
        val staticIp = dialogView.findViewById<TextInputLayout>(R.id.static_ip)!!
        val gateway = dialogView.findViewById<TextInputLayout>(R.id.gateway)!!
        val subnet = dialogView.findViewById<TextInputLayout>(R.id.subnet)!!
        val title = dialogView.findViewById<TextView>(R.id.title)!!
        val dialog = MaterialAlertDialogBuilder(requireActivity())
            .setView(dialogView)
            .setPositiveButton(R.string.confirm, null)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel, null)
            .create()
        staticIpConfiguration.setOnCheckedChangeListener { _, value ->
            staticIp.isVisible = value
            gateway.isVisible = value
            subnet.isVisible = value
        }
        title.text = wifiDevice.ssid
        inputLayout.editText?.doAfterTextChanged {
            inputLayout.error = null
        }
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        inputLayout.editText?.requestFocus()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val password = inputLayout.editText?.text?.toString()
            if (password.isNullOrEmpty() || password.length < 8) {
                inputLayout.error =
                    getString(R.string.input_wrong, getString(R.string.wifi_password))
            } else {
                dialog.dismiss()
                viewModel.onPasswordConfirmed(
                    wifiDevice,
                    password,
                    staticIp.editText?.text.toString(),
                    gateway.editText?.text.toString(),
                    subnet.editText?.text.toString(),
                )
            }
        }
    }
}