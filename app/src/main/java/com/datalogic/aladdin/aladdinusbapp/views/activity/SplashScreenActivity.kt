package com.datalogic.aladdin.aladdinusbapp.views.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbscannersdk.BuildConfig
import kotlinx.coroutines.delay

class SplashScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = CommonUtils.orientation

        setContent {
            SplashScreen()
        }
    }

    @Composable
    fun SplashScreen() {
        val version = getAppVersion()

        LaunchedEffect(Unit) {
            delay(3000L)
            startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
            finish()
        }

        Box(
            modifier = Modifier
                .semantics { contentDescription = "splash_background" }
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.ic_splash_background),
                    contentScale = ContentScale.FillBounds
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .semantics { contentDescription = "splash_app_logo" }
                    .size(
                        dimensionResource(id = R.dimen._190sdp),
                        dimensionResource(id = R.dimen._105sdp)
                    )
                    .paint(
                        painterResource(id = R.drawable.ic_logo_splash_version_screen),
                        contentScale = ContentScale.Fit
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "V $version",
                    color = Color.White,
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .semantics { contentDescription = "lblVersion" }
                        .padding(
                            start = dimensionResource(id = R.dimen._35sdp),
                            top = dimensionResource(id = R.dimen._20sdp)
                        )
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_splash_datalogic_logo),
                contentDescription = null,
                modifier = Modifier
                    .semantics { contentDescription = "splash_datalogic_logo" }
                    .padding(bottom = dimensionResource(id = R.dimen._90sdp))
                    .size(
                        dimensionResource(id = R.dimen._100sdp),
                        dimensionResource(id = R.dimen._12sdp)
                    )
                    .align(Alignment.BottomCenter)
            )
        }
    }

    fun getAppVersion(): String {
        return try {
            val versionSDK = BuildConfig.LIBRARY_VERSION_NAME
            versionSDK.removePrefix("AladdinUsbSdk_")
        } catch (exp: PackageManager.NameNotFoundException) {
            Log.d("SplashScreenActivity", "Failed to get version number: ${exp.printStackTrace()}")
            "N/A"
        }
    }
}