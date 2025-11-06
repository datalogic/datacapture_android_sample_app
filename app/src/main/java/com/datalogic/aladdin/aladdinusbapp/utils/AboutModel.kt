package com.datalogic.aladdin.aladdinusbapp.utils

import com.datalogic.aladdin.aladdinusbscannersdk.BuildConfig

data class AboutModel(
    val osName: String,
    val osVersion: String,
    val sdkInt: Int,
    val deviceModel: String,
    val deviceBrand: String,
    val arch: String,
    val timeZone: String,
    val versionSDK: String,
    val appVersion: String,
)
