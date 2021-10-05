package com.viam.permissioncontract

interface PermissionRequester {
    fun request(
        vararg permissions: String,
        requiredPermissions: Array<out String>? = permissions,
        callback: () -> Unit
    )
}
