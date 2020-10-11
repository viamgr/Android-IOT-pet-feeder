@file:Suppress("DEPRECATION")

package com.viam.feeder.ui.wifi

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.viam.feeder.BR
import com.viam.feeder.R
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.databinding.FragmentWifiBinding
import com.viam.feeder.livedata.EventObserver
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class WifiFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentWifiBinding
    private val viewModel: WifiViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FrameLayout(requireActivity()).also { layout ->
        DataBindingUtil.inflate<FragmentWifiBinding>(inflater, R.layout.fragment_wifi, layout, true)
            ?.apply {
                lifecycleOwner = viewLifecycleOwner
                setVariable(BR.vm, viewModel)
            }?.also {
                binding = it
            }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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