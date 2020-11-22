package com.viam.feeder.core.utility.dexter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringDef
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.viam.feeder.R


val appPermissionList = mapOf(
    Manifest.permission.RECORD_AUDIO to Pair(R.string.record_audio, R.string.record_audio),
    Manifest.permission.WRITE_EXTERNAL_STORAGE to Pair(
        R.string.record_audio,
        R.string.record_audio
    ),
    Manifest.permission.READ_EXTERNAL_STORAGE to Pair(
        R.string.read_external_storage,
        R.string.read_external_storage
    ),
    Manifest.permission.ACCESS_FINE_LOCATION to Pair(
        R.string.access_fine_location,
        R.string.access_fine_location
    ),
    Manifest.permission.ACCESS_COARSE_LOCATION to Pair(
        R.string.access_coarse_location,
        R.string.access_coarse_location
    ),
    Manifest.permission.CHANGE_WIFI_STATE to Pair(
        R.string.change_wifi_state,
        R.string.change_wifi_state
    ),
    Manifest.permission.ACCESS_WIFI_STATE to Pair(
        R.string.access_wifi_state,
        R.string.access_wifi_state
    ),
    Manifest.permission.CHANGE_NETWORK_STATE to Pair(
        R.string.change_network_state,
        R.string.change_network_state
    ),
)

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
)
annotation class PermissionConstant

interface PermissionRequester {
    fun request(
        @PermissionConstant vararg permissions: String,
        requiredPermissions: Array<out String>? = permissions,
        callback: () -> Unit
    )
}


class PermissionContract<T>(
    private val fragmentActivity: T,
) : PermissionRequester, LifecycleObserver where T : LifecycleOwner {

    var isRequesting = false
    var isRequested = false

    private var listenerEnabled = false
    private val onActivityResult: (result: MutableMap<String, Boolean>) -> Unit = { grantResults ->
        if (!grantResults
                .filter { requiredPermissions?.contains(it.key) == true }
                .any { !it.value }
        ) {
            requestedCallback?.invoke()
        } else {
            showSettingSnackBar()
        }
    }
    private var requestPermissionLauncher: ActivityResultLauncher<Array<out String>>
    private var requestedCallback: (() -> Unit)? = null
    private var requestedPermissions: Array<out String>? = null
    private var requiredPermissions: Array<out String>? = null
    private var deniedPermissions: List<String>? = null
    override fun request(
        vararg permissions: String,
        requiredPermissions: Array<out String>?,
        callback: () -> Unit
    ) {
        isRequested = true
        isRequesting = true
        requestedPermissions = permissions
        requestedCallback = {
            isRequesting = false
            callback.invoke()
        }
        this.requiredPermissions = requiredPermissions
        // TODO: 11/11/2020 get it from host
        val shouldShowRequestPermissionRationale = true
        deniedPermissions = getDeniedPermissions()

        when {
            deniedPermissions.isNullOrEmpty() -> {
                requestedCallback?.invoke()
            }
            shouldShowRequestPermissionRationale -> {
                showRationalDialog(deniedPermissions!!)
            }
            else -> {
                requestPermissions()
            }
        }
    }

    fun isRequestingOrRequested() = isRequested || isRequesting

    fun isGranted(): Boolean {
        return getDeniedPermissions()
            ?.intersect(requiredPermissions?.toList().orEmpty())
            ?.isNullOrEmpty() ?: false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (listenerEnabled) {
            listenerEnabled = false
            if (deniedPermissions.isNullOrEmpty()) {
                getView()?.postDelayed(300L) {
                    getView()?.let {
                        requestedCallback?.invoke()
                    }
                }
            }
        }

    }

    private fun getDeniedPermissions(): List<String>? {
        return requestedPermissions?.filter {
            checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_DENIED
        }
    }

    private fun getView(): View? {
        return if (isFragment()) (fragmentActivity as Fragment).view else (fragmentActivity as Activity).window.decorView.rootView
    }

    private fun isFragment() = fragmentActivity is Fragment
    private fun registerListener() {
        listenerEnabled = true
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(deniedPermissions!!.toTypedArray())
    }

    private fun requireContext(): Context {
        return if (isFragment()) (fragmentActivity as Fragment).requireContext() else fragmentActivity as Context
    }

    private fun showRationalDialog(requestedPermissions: List<String>) {

        val parentLayout =
            View.inflate(requireContext(), R.layout.layout_permission_wrapper, null) as ViewGroup
        requestedPermissions.forEach {
            val view = View.inflate(
                requireContext(),
                R.layout.layout_permission_item, null
            )
            val permission = appPermissionList[it] ?: error("Invalid Permission ")
            view.findViewById<TextView>(R.id.label).text =
                requireContext().getString(permission.first)
            view.findViewById<TextView>(R.id.description).text =
                requireContext().getString(permission.second)
            view.findViewById<View>(R.id.required).isVisible =
                requiredPermissions?.contains(it) == true
            parentLayout.findViewById<ViewGroup>(R.id.wrapper).addView(view)
        }

        val decorView = MaterialAlertDialogBuilder(requireContext())
            .setView(parentLayout)
            .setPositiveButton(R.string.continue_) { _, _ ->
                requestPermissions()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                val permissions = requestedPermissions.filter {
                    requiredPermissions?.contains(it) == true
                }
                if (permissions.isNullOrEmpty()) {
                    requestedCallback?.invoke()
                } else {
                    showSettingSnackBar()
                }

            }
            .show().window?.decorView
//        ViewCompat.setElevation(decorView!!, Float.MAX_VALUE)
//        decorView.bringToFront()

    }

    private fun showSettingSnackBar() {
        isRequesting = false
        getView()?.let {
            Snackbar.make(it, R.string.permission_setting_required, Snackbar.LENGTH_LONG)
                .setAction("Settings") {
                    registerListener()
                    val intent = Intent(
                        "android.settings.APPLICATION_DETAILS_SETTINGS",
                        Uri.parse("package:" + requireContext().packageName)
                    )
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    requireContext().startActivity(intent)
                }
                .show()
        }
    }

    init {
        if (isFragment()) {
            requestPermissionLauncher = (fragmentActivity as Fragment).registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(), onActivityResult
            )
            (fragmentActivity as LifecycleOwner).lifecycle.addObserver(this)

        } else {
            requestPermissionLauncher =
                (fragmentActivity as AppCompatActivity).registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(), onActivityResult
                )
            (fragmentActivity as AppCompatActivity).lifecycle.addObserver(this)
        }

    }

}


fun Fragment.permissionContract(): PermissionContract<Fragment> {
    return PermissionContract(this)
}

fun AppCompatActivity.permissionContract(): PermissionContract<AppCompatActivity> {
    return PermissionContract(this)
}