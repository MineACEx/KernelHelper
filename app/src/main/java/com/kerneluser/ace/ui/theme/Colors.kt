package com.kerneluser.ace.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Light palette ──────────────────────────────────
object AppColors {
    val Primary = Color(0xFF3B82F6)
    val PrimaryLight = Color(0xFF93BBFD)

    val Bg = Color(0xFFF0F2F5)
    val Background = Bg
    val BgSecondary = Color(0xFFE8EAED)

    val Surface = Color(0xFFFFFFFF)
    val SurfaceHover = Color(0xFFF8F9FB)

    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)

    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)

    val Divider = Color(0xFFE5E7EB)

    val NavBg = Color(0xFFFCFCFD)
    val NavActive = Color(0xFF3B82F6)
    val NavInactive = Color(0xFF9CA3AF)

    val GlassBg = NavBg
    val TabActive = NavActive
    val TabInactive = NavInactive

    val BlurBg = Color(0xCCFFFFFF)
    val BlurBorder = Color(0x1A000000)
    val GlassBorder = BlurBorder

    // Tab bar
    val TabBarBg = Color(0xFFF8F9FB)
    val TabBarTopLine = Color(0xFFE5E7EB)
}

// ── Dark palette ───────────────────────────────────
object DarkColors {
    val Primary = Color(0xFF60A5FA)
    val PrimaryLight = Color(0xFF93BBFD)

    val Bg = Color(0xFF0F172A)
    val Background = Bg
    val BgSecondary = Color(0xFF1E293B)

    val Surface = Color(0xFF1E293B)
    val SurfaceHover = Color(0xFF334155)

    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    val Success = Color(0xFF34D399)
    val Warning = Color(0xFFFBBF24)
    val Error = Color(0xFFF87171)

    val Divider = Color(0xFF334155)

    val NavBg = Color(0xFF1E293B)
    val NavActive = Color(0xFF60A5FA)
    val NavInactive = Color(0xFF64748B)

    val GlassBg = NavBg
    val TabActive = NavActive
    val TabInactive = NavInactive

    val BlurBg = Color(0xCC1E293B)
    val BlurBorder = Color(0x1AFFFFFF)
    val GlassBorder = BlurBorder

    val TabBarBg = Color(0xFF1A2332)
    val TabBarTopLine = Color(0xFF334155)
}

// ── Theme context ──────────────────────────────────
data class AceThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val bg: Color,
    val background: Color,
    val bgSecondary: Color,
    val surface: Color,
    val surfaceHover: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val divider: Color,
    val navBg: Color,
    val navActive: Color,
    val navInactive: Color,
    val glassBg: Color,
    val tabActive: Color,
    val tabInactive: Color,
    val blurBg: Color,
    val blurBorder: Color,
    val glassBorder: Color,
    val tabBarBg: Color,
    val tabBarTopLine: Color
)

val LightTheme = AceThemeColors(
    primary = AppColors.Primary, primaryLight = AppColors.PrimaryLight,
    bg = AppColors.Bg, background = AppColors.Background,
    bgSecondary = AppColors.BgSecondary, surface = AppColors.Surface,
    surfaceHover = AppColors.SurfaceHover,
    textPrimary = AppColors.TextPrimary, textSecondary = AppColors.TextSecondary,
    textMuted = AppColors.TextMuted, success = AppColors.Success,
    warning = AppColors.Warning, error = AppColors.Error,
    divider = AppColors.Divider, navBg = AppColors.NavBg,
    navActive = AppColors.NavActive, navInactive = AppColors.NavInactive,
    glassBg = AppColors.GlassBg, tabActive = AppColors.TabActive,
    tabInactive = AppColors.TabInactive, blurBg = AppColors.BlurBg,
    blurBorder = AppColors.BlurBorder, glassBorder = AppColors.GlassBorder,
    tabBarBg = AppColors.TabBarBg, tabBarTopLine = AppColors.TabBarTopLine
)

val DarkTheme = AceThemeColors(
    primary = DarkColors.Primary, primaryLight = DarkColors.PrimaryLight,
    bg = DarkColors.Bg, background = DarkColors.Background,
    bgSecondary = DarkColors.BgSecondary, surface = DarkColors.Surface,
    surfaceHover = DarkColors.SurfaceHover,
    textPrimary = DarkColors.TextPrimary, textSecondary = DarkColors.TextSecondary,
    textMuted = DarkColors.TextMuted, success = DarkColors.Success,
    warning = DarkColors.Warning, error = DarkColors.Error,
    divider = DarkColors.Divider, navBg = DarkColors.NavBg,
    navActive = DarkColors.NavActive, navInactive = DarkColors.NavInactive,
    glassBg = DarkColors.GlassBg, tabActive = DarkColors.TabActive,
    tabInactive = DarkColors.TabInactive, blurBg = DarkColors.BlurBg,
    blurBorder = DarkColors.BlurBorder, glassBorder = DarkColors.GlassBorder,
    tabBarBg = DarkColors.TabBarBg, tabBarTopLine = DarkColors.TabBarTopLine
)

val LocalAceColors = staticCompositionLocalOf { LightTheme }