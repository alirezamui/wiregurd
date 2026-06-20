package com.example.ui.translation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage {
    ENGLISH,
    PERSIAN
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object Translator {
    private val en = mapOf(
        "app_title" to "WireGuard VPN",
        "home" to "Dashboard",
        "connections" to "Profiles",
        "settings" to "Settings",
        "about" to "About",
        "connect" to "Connect",
        "disconnect" to "Disconnect",
        "status" to "VPN Tunnel Status",
        "public_ip" to "Public IP Address",
        "local_ip" to "Local Address",
        "endpoint" to "Server Endpoint",
        "ping" to "Ping Latency",
        "connecting" to "Connecting",
        "connected" to "Connected",
        "disconnected" to "Disconnected",
        "error" to "Error",
        "no_profiles" to "No WireGuard configurations found. Scan a QR code or import a '.conf' file to begin.",
        "import_title" to "Import Configuration",
        "import_btn" to "Import File",
        "paste_label" to "Paste Conf Configuration",
        "name_label" to "Profile Name",
        "save_btn" to "Save Profile",
        "qr_scan_title" to "Scan WireGuard QR",
        "camera_permission_denied" to "Camera permission is required to scan WireGuard configuration QR codes.",
        "grant_permission" to "Grant Permission",
        "settings_title" to "Application Settings",
        "language_label" to "Interface Language",
        "theme_label" to "Dark Visual Theme",
        "sample_btn" to "Load Sample Iran Telecommunication Conf",
        "about_title" to "About PersianGuard",
        "about_desc" to "PersianGuard is a high-performance WireGuard client designed for Android, featuring modern Jetpack Compose interface structures, local configuration encryption, and rapid network diagnostics.",
        "version_label" to "Version 1.0.0 (Release Build Stable)",
        "security_badge" to "Configuration details are securely stored locally with military-grade AES-128-CBC encryption. Your private keys never leave your device.",
        "empty_state_lbl" to "Ready for Secure Tunneling",
        "qr_scan_prompt" to "Align WireGuard QR code inside the scanner box",
        "rename_profile" to "Rename Profile",
        "delete_profile" to "Delete Profile",
        "confirm_delete" to "Are you sure you want to delete this profile?",
        "cancel" to "Cancel",
        "rename_hint" to "Enter new name",
        "or_paste" to "Or Paste Config Code Down Below:",
        "import_success" to "Profile imported and secured successfully!",
        "validation_success" to "Configuration validated successfully."
    )

    private val fa = mapOf(
        "app_title" to "یوز گارد | WireGuard VPN",
        "home" to "داشبورد",
        "connections" to "پیکربندی‌ها",
        "settings" to "تنظیمات",
        "about" to "درباره ما",
        "connect" to "اتصال",
        "disconnect" to "قطع اتصال",
        "status" to "وضعیت تونل وی‌پی‌ان",
        "public_ip" to "آدرس آی‌پی عمومی شما",
        "local_ip" to "آدرس آی‌پی محلی",
        "endpoint" to "آدرس سرور مقصد",
        "ping" to "میزان تأخیر پینگ",
        "connecting" to "در حال اتصال",
        "connected" to "متصل شد",
        "disconnected" to "قطع اتصال شده",
        "error" to "خطا",
        "no_profiles" to "هیچ پیکربندی وایرگارد یافت نشد. برای شروع، یک کد QR اسکن کنید یا یک فایل '.conf' وارد نمایند.",
        "import_title" to "افزودن پیکربندی جدید",
        "import_btn" to "پیوست فایل پیکربندی",
        "paste_label" to "متن پیکربندی وایرگارد",
        "name_label" to "نام پیکربندی",
        "save_btn" to "ذخیره و رمزنگاری",
        "qr_scan_title" to "اسکن بارکد QR",
        "camera_permission_denied" to "برای اسکن کدهای بارکد وایرگارد، دسترسی به دوربین الزامی می‌باشد.",
        "grant_permission" to "اعطای مجوز دوربین",
        "settings_title" to "تنظیمات برنامه",
        "language_label" to "زبان رابط کاربری",
        "theme_label" to "پوسته تیره برنامه",
        "sample_btn" to "بارگذاری پیکربندی تستی ایران",
        "about_title" to "درباره یوز گارد",
        "about_desc" to "یوز گارد یک کلاینت بومی و پرسرعت برای پروتکل امن وایرگارد (WireGuard) است که با استفاده از فناوری مدرن جت‌پک کامپوز اندروید توسعه یافته و مجهز به امن‌ترین متدهای رمزنگاری پایگاه‌داده محلی و ابزارهای مانیتورینگ شبکه است.",
        "version_label" to "نسخه ۱.۰.۰ (پایدار پایانه ریلیز)",
        "security_badge" to "اطلاعات پیکربندی‌های ذخیره شده به شکل محلی با الگوریتم نظامی AES-128-CBC کدگذاری می‌شوند. کلید خصوصی شما هرگز از دستگاه خارج نخواهد شد.",
        "empty_state_lbl" to "آماده برای اتصال امن تونل",
        "qr_scan_prompt" to "کد QR وایرگارد را وارد کادر اسکنر نمایید",
        "rename_profile" to "تغییر نام پیکربندی",
        "delete_profile" to "حذف پیکربندی",
        "confirm_delete" to "آیا از حذف دائمی این پیکربندی اطمینان کامل دارید؟",
        "cancel" to "انصراف",
        "rename_hint" to "نام جدید را وارد کنید",
        "or_paste" to "یا متن پیکربندی را در کادر زیر کپی نمایید:",
        "import_success" to "پیکربندی با موفقیت ایمپورت و رمزنگاری گردید!",
        "validation_success" to "پیکربندی با موفقیت اعتبارسنجی شد."
    )

    fun translate(key: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> en[key] ?: key
            AppLanguage.PERSIAN -> fa[key] ?: en[key] ?: key
        }
    }

    fun getLayoutDirection(language: AppLanguage): LayoutDirection {
        return when (language) {
            AppLanguage.ENGLISH -> LayoutDirection.Ltr
            AppLanguage.PERSIAN -> LayoutDirection.Rtl
        }
    }
}
