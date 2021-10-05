package com.viam.permissioncontract

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.permissionContract(infoBlock: InfoBlock? = null): PermissionContract<Fragment> {
    return PermissionContract(this, infoBlock)
}

fun AppCompatActivity.permissionContract(infoBlock: InfoBlock? = null): PermissionContract<AppCompatActivity> {
    return PermissionContract(this, infoBlock)
}