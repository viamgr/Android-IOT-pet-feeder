package com.viam.feeder.core.utility.dexter

import android.Manifest
import android.view.View
import androidx.annotation.StringDef
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.viam.feeder.R

val permissions = mapOf(
    Manifest.permission.RECORD_AUDIO to R.string.record_audio,
    Manifest.permission.WRITE_EXTERNAL_STORAGE to R.string.write_external_storage,
)

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
)
annotation class PermissionConstant

fun View.checkPermission(
    @PermissionConstant permission: String,
    callback: () -> Unit
) {
    Dexter.withContext(context)
        .withPermission(permission)
        .withListener(
            CompositePermissionListener(
                object : BasePermissionListener() {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        callback()
                    }
                },
                SnackbarOnDeniedPermissionListener.Builder
                    .with(
                        this,
                        context.getString(
                            R.string.permission_denied_message,
                            context.getString(
                                permissions[permission] ?: error("Unknown permission")
                            )
                        )
                    )
                    .withOpenSettingsButton(context.getString(R.string.settings))
                    .withDuration(Int.MAX_VALUE).build()
            )
        )
        .check()
}