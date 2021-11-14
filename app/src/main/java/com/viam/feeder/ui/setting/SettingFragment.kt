package com.viam.feeder.ui.setting

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.databinding.DialogWifiPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment(R.layout.dialog_wifi_password) {

    private val binding by viewBinding(DialogWifiPasswordBinding::bind)

    private val viewModel: SettingViewModel by viewModels()


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.submit -> {
                val ssid = binding.ssid.editText?.text?.toString()
                val password = binding.password.editText?.text?.toString()

                if (ssid.isNullOrEmpty() || ssid.isEmpty()) {
                    binding.ssid.error =
                        getString(R.string.input_wrong, getString(R.string.wifi_ssid))
                } else if (password.isNullOrEmpty() || password.length < 8) {
                    binding.password.error =
                        getString(R.string.input_wrong, getString(R.string.wifi_password))
                } else {
                    viewModel.onPasswordConfirmed(
                        binding.ssid.editText?.text.toString(),
                        binding.password.editText?.text.toString(),
                        binding.staticIp.editText?.text.toString(),
                        binding.gateway.editText?.text.toString(),
                        binding.subnet.editText?.text.toString(),
                        binding.staticIpConfiguration.isChecked,
                    )
                }
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }
        setHasOptionsMenu(true)

    }
}