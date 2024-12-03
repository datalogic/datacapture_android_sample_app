package com.datalogic.aladdin.aladdinusbapp.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceUtil (val context: Context) {
    companion object {
        private const val prefName = "aladdinUSBPrefKeys"
        private var sharedPreferences: SharedPreferences? = null
        private var editor: SharedPreferences.Editor? = null

        fun initSharedPreference(context: Context) {
            sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            editor = sharedPreferences?.edit()
        }
    }
}