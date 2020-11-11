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
import androidx.activity.ComponentActivity
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
    private val context: T,
) : PermissionRequester, LifecycleObserver where T : LifecycleOwner {

    private var requestedPermissions: Array<out String>? = null
    private var requestedRequiredPermissions: Array<out String>? = null
    private var requestedCallback: (() -> Unit)? = null
    private var listenerEnabled = false

    init {
        (requireContext() as LifecycleOwner).lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (listenerEnabled) {
            listenerEnabled = false
            val permissions = requestedPermissions!!.filter {
                requestedRequiredPermissions?.contains(it) == true
            }
            if (getDeniedPermissions(permissions).isNullOrEmpty()) {
                getView()?.postDelayed(300L) {
                    getView()?.let {
                        requestedCallback?.invoke()
                    }
                }
            }
        }

    }

    private fun isFragment() = context is Fragment

    private fun getView(): View? {
        return if (isFragment()) (context as Fragment).view else (context as Activity).window.decorView.rootView
    }

    private fun requireContext(): Context {
        return if (isFragment()) (context as Fragment).requireContext() else context as Context
    }

    private fun requireActivity(): ComponentActivity {
        return if (isFragment()) (context as Fragment).requireActivity() else context as ComponentActivity
    }


    private fun getDeniedPermissions(permissions: List<String>): List<String> {
        return permissions.filter {
            checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_DENIED
        }
    }

    private fun registerListener() {
        listenerEnabled = true
    }

    private fun showSettingSnackBar() {
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

    private val requestPermissionLauncher = requireActivity().registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults ->
        if (!grantResults
                .filter { requestedRequiredPermissions?.contains(it.key) == true }
                .any { it.value == false }
        ) {
            requestedCallback?.invoke()
        } else {
            showSettingSnackBar()
        }
    }


    private fun requestPermissions() {
        requestPermissionLauncher.launch(requestedPermissions)
    }


    private fun showRationalDialog(
        requestedPermissions: List<String>,
        requestedRequiredPermissions: Array<out String>? = null
    ) {

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
                requestedRequiredPermissions?.contains(it) == true
            parentLayout.findViewById<ViewGroup>(R.id.wrapper).addView(view)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(parentLayout)
            .setPositiveButton(R.string.continue_) { _, _ ->
                requestPermissions()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->

                val permissions = requestedPermissions.filter {
                    requestedRequiredPermissions?.contains(it) == true
                }
                if (permissions.isNullOrEmpty()) {
                    requestedCallback?.invoke()
                } else {
                    showSettingSnackBar()
                }

            }
            .show()
    }

    override fun request(
        vararg permissions: String,
        requiredPermissions: Array<out String>?,
        callback: () -> Unit
    ) {
        requestedPermissions = permissions
        requestedCallback = callback
        requestedRequiredPermissions = requiredPermissions
        // TODO: 11/11/2020 get it from host
        val shouldShowRequestPermissionRationale = true
        val deniedPermissions = getDeniedPermissions(permissions.toList())

        when {
            deniedPermissions.isEmpty() -> {
                callback()
            }
            shouldShowRequestPermissionRationale -> {
                showRationalDialog(deniedPermissions, requestedRequiredPermissions)
            }
            else -> {
                requestPermissions()
            }
        }
    }

}


fun Fragment.permissionContract(): PermissionContract<Fragment> {
    return PermissionContract(this)
}

fun AppCompatActivity.permissionContract(): PermissionContract<AppCompatActivity> {
    return PermissionContract(this)
}