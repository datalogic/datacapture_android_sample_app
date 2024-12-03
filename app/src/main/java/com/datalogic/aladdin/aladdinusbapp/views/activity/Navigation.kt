package com.datalogic.aladdin.aladdinusbapp.views.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.HomeScreenLayoutLandscape
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.HomeScreenLayoutPortrait

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "home_screen") {
        composable("home_screen") {
            if (CommonUtils.isTablet)
                HomeScreenLayoutLandscape()
            else
                HomeScreenLayoutPortrait()
        }
    }
}