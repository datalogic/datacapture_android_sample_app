package com.datalogic.aladdin.aladdinusbapp.views.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R

val AladdinFontFamily = FontFamily(
    Font(R.font.barlow_bold, FontWeight.Bold),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow, FontWeight.Normal),
)

val TypographyAladdin = Typography(
    bodyLarge = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Color.Black
    ),
    bodyMedium = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Color.Black
    ),
    bodySmall = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = Color.Black
    ),
    headlineLarge = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Color.Black
    ),
    headlineMedium = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = Color.Black
    ),
    headlineSmall = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = Color.Black
    ),
    labelLarge = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = Color.Black
    ),
    labelMedium = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Color.Black
    ),
    labelSmall = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color.Black
    ),
    titleLarge = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black
    ),
    titleMedium = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black
    ),
    titleSmall = TextStyle(
        fontFamily = AladdinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color.Black
    ),
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)