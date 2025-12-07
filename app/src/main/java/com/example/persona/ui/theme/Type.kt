package com.example.persona.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 蓝色系配色方案

// 深色模式 - 浅色调
val Blue80 = Color(0xFFB3D9FF)        // 浅蓝色 - 主色
val SkyBlue80 = Color(0xFFA3D5FF)     // 天蓝色 - 次要色
val Cyan80 = Color(0xFF9FE5F0)        // 青色 - 第三色

// 浅色模式 - 深色调
val Blue40 = Color(0xFF0066CC)        // 深蓝色 - 主色
val SkyBlue40 = Color(0xFF1976D2)     // 深天蓝色 - 次要色
val Cyan40 = Color(0xFF0097A7)        // 深青色 - 第三色

// 深色模式配色
val DarkColorScheme = darkColorScheme(
    primary = Blue80,                  // 主色：浅蓝色
    onPrimary = Color(0xFF003258),     // 主色上的文字：深蓝
    primaryContainer = Color(0xFF004A77), // 主色容器：中蓝
    onPrimaryContainer = Color(0xFFD3E4FF), // 主色容器上的文字

    secondary = SkyBlue80,             // 次要色：浅天蓝
    onSecondary = Color(0xFF00344F),   // 次要色上的文字
    secondaryContainer = Color(0xFF004D70), // 次要色容器
    onSecondaryContainer = Color(0xFFCCE5FF), // 次要色容器上的文字

    tertiary = Cyan80,                 // 第三色：浅青色
    onTertiary = Color(0xFF003640),    // 第三色上的文字
    tertiaryContainer = Color(0xFF004F5B), // 第三色容器
    onTertiaryContainer = Color(0xFFB8EEFF), // 第三色容器上的文字

    background = Color(0xFF001F2A),    // 背景：深蓝黑
    onBackground = Color(0xFFE0F2FF),  // 背景上的文字：浅蓝白

    surface = Color(0xFF001F2A),       // 表面：深蓝黑
    onSurface = Color(0xFFE0F2FF),     // 表面上的文字
    surfaceVariant = Color(0xFF40495D), // 表面变体：蓝灰
    onSurfaceVariant = Color(0xFFC0CADC), // 表面变体上的文字

    outline = Color(0xFF8A95A8)        // 轮廓线：灰蓝
)

// 浅色模式配色
val LightColorScheme = lightColorScheme(
    primary = Blue40,                  // 主色：深蓝色
    onPrimary = Color.White,           // 主色上的文字：白色
    primaryContainer = Color(0xFFD3E4FF), // 主色容器：浅蓝
    onPrimaryContainer = Color(0xFF001C38), // 主色容器上的文字：深蓝黑

    secondary = SkyBlue40,             // 次要色：深天蓝
    onSecondary = Color.White,         // 次要色上的文字
    secondaryContainer = Color(0xFFCCE5FF), // 次要色容器：浅天蓝
    onSecondaryContainer = Color(0xFF001D33), // 次要色容器上的文字

    tertiary = Cyan40,                 // 第三色：深青色
    onTertiary = Color.White,          // 第三色上的文字
    tertiaryContainer = Color(0xFFB8EEFF), // 第三色容器：浅青色
    onTertiaryContainer = Color(0xFF001F26), // 第三色容器上的文字

    background = Color(0xFFFDFCFF),    // 背景：纯白偏蓝
    onBackground = Color(0xFF001F2A),  // 背景上的文字：深蓝黑

    surface = Color(0xFFFDFCFF),       // 表面：纯白偏蓝
    onSurface = Color(0xFF001F2A),     // 表面上的文字
    surfaceVariant = Color(0xFFE0E8F5), // 表面变体：浅蓝灰
    onSurfaceVariant = Color(0xFF42474E), // 表面变体上的文字：深灰

    outline = Color(0xFF72787F)        // 轮廓线：中灰
)

// Typography
val AppTypography = Typography(
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