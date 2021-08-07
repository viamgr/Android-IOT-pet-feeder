package com.viam.feeder.ui.setting

import com.airbnb.epoxy.TypedEpoxyController
import com.viam.feeder.wifiList

class WifiController : TypedEpoxyController<List<com.viam.feeder.model.WifiDevice>>() {
    lateinit var clickListener: (com.viam.feeder.model.WifiDevice) -> Unit
    override fun buildModels(data: List<com.viam.feeder.model.WifiDevice>?) {
        data?.forEach { wifiDevice: com.viam.feeder.model.WifiDevice ->
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