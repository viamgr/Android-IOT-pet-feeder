@file:Suppress("DEPRECATION")

package com.viam.feeder.ui.wifi

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.viam.feeder.BR
import com.viam.feeder.R
import com.viam.feeder.constants.ACCESS_POINT_PASSWORD
import com.viam.feeder.constants.ACCESS_POINT_SSID
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentWifiBinding
import com.viam.feeder.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WifiFragment : DialogFragment() {

    private lateinit var binding: FragmentWifiBinding
    private val viewModel: WifiViewModel by viewModels()

    @Inject
    lateinit var connectionUtil: NetworkStatusObserver

    @Inject
    lateinit var wifiAutoConnect: WifiAutoConnect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        wifiAutoConnect.connect(ACCESS_POINT_SSID, ACCESS_POINT_PASSWORD) { connected ->
            if (connected) dismiss()
        }
    }

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
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        wifiAutoConnect.stop()
        (requireActivity() as MainActivity).setIsWifiDialogShowing(false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.enableWifiClicked.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        })

        connectionUtil.observe(viewLifecycleOwner) {
            if (it.isUnknownWifi()) {
                showWrongWifiDialog()
            } else if (it.isConnectedToPreferredDevice(ACCESS_POINT_SSID)) {
                dismiss()
            }
        }
    }

    private fun showWrongWifiDialog() {
        Toast.makeText(
            requireContext(),
            getString(R.string.wrong_connected, ACCESS_POINT_SSID),
            Toast.LENGTH_SHORT
        ).show()
//        dismiss()
    }
}