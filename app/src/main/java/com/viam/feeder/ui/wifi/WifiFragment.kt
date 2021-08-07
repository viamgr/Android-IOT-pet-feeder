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
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.viam.feeder.BR
import com.viam.feeder.R
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentWifiBinding
import com.viam.feeder.main.MainActivity
import com.viam.feeder.shared.ACCESS_POINT_PASSWORD
import com.viam.feeder.shared.ACCESS_POINT_SSID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class WifiFragment : DialogFragment() {


    private lateinit var binding: FragmentWifiBinding

    @Inject
    lateinit var connectionUtil: NetworkStatusObserver
    private val viewModel: WifiViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
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
        turnOnWifi()
        autoConnect()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
//        wifiAutoConnect.stop()
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

        /*connectionUtil.observe(viewLifecycleOwner) {
            val ignoredInitialValue = !viewModel.ignoredInitialValue.compareAndSet(false, true)
            val unknownOrKnownWifiConnection =
                requireContext().isUnknownOrKnownWifiConnection(ACCESS_POINT_SSID)
            if (ignoredInitialValue && unknownOrKnownWifiConnection) {
                dismiss()
            }
        }*/
    }

    fun autoConnect() {
        WifiUtils.withContext(requireContext())
            .connectWith(ACCESS_POINT_SSID, ACCESS_POINT_PASSWORD)
            .setTimeout(40000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    dismiss()
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    showWrongWifiDialog()
                    autoConnect()
                }
            })
            .start()
    }

    private fun checkResult(isSuccess: Boolean) {
        if (isSuccess) Toast.makeText(requireContext(), "WIFI ENABLED", Toast.LENGTH_SHORT)
            .show() else Toast.makeText(
            requireContext(),
            "COULDN'T ENABLE WIFI",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showWrongWifiDialog() {
        Toast.makeText(
            requireContext(),
            getString(R.string.wrong_connected, ACCESS_POINT_SSID),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun turnOnWifi() {
        WifiUtils.withContext(requireContext()).enableWifi(this::checkResult);

    }
}