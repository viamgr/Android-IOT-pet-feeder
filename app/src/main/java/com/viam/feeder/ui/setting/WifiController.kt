package com.viam.feeder.ui.setting

import com.airbnb.epoxy.TypedEpoxyController
import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.wifiList

class WifiController : TypedEpoxyController<List<WifiDevice>>() {
    lateinit var clickListener: (WifiDevice) -> Unit
    override fun buildModels(data: List<WifiDevice>?) {
        data?.forEach { wifiDevice: WifiDevice ->
            wifiList {
                id(wifiDevice.ssid)
                ssid(wifiDevice.ssid)
                lock(wifiDevice.secure != 7)
                clickListener { _ ->
                    this@WifiController.clickListener(wifiDevice)
                }
            }
        }
    }
}