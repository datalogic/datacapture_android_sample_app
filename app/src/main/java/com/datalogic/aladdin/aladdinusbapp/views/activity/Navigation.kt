package com.datalogic.aladdin.aladdinusbapp.views.activity

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.ConfigurationTabLandscape
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.ConfigurationTabPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.DirectIOTabLandscape
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.DirectIOTabPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.HomeScreenLayoutLandscape
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.HomeScreenLayoutPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.HomeTabLandscape
import com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen.HomeTabPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.imageCapture.ImageCaptureTabPortrait

@Composable
fun Navigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreenLayout()
        }
    }

}

@Composable
fun HomeScreenLayout() {
    val configuration = LocalConfiguration.current

    // Choose layout based on orientation
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> HomeScreenLayoutLandscape()
        else -> HomeScreenLayoutPortrait()
    }
}


