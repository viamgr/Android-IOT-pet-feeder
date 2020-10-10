@file:Suppress("DEPRECATION")

package com.viam.feeder.ui.wifi

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.databinding.FragmentWifiBinding
import com.viam.feeder.livedata.EventObserver
import com.viam.feeder.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class WifiFragment : Fragment(R.layout.fragment_wifi) {

    private val binding by viewBinding(FragmentWifiBinding::bind)
    private val viewModel: WifiViewModel by viewModels()

    private val activityViewModels = activityViewModels<MainViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        viewModel.enableWifiClicked.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        })
        viewModel.networkStatus.connection.observe(viewLifecycleOwner, Observer {
            if (it == NetworkStatus.CONNECTION_STATE_SUCCESS) {
                findNavController().navigateUp()
            }
        })
    }

}