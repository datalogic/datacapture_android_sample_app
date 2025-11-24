package com.datalogic.aladdin.aladdinusbapp.utils

import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice

data class CradleState(
    val device: DatalogicDevice,
    val docked: Boolean = false,
    val linked: Boolean = false
)
