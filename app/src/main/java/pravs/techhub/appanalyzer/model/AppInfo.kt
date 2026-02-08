package pravs.techhub.appanalyzer.model


import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable?,
    val size: Long,
    val installedDate: Long,
    val lastUpdated: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val developer: String,
    val category: String,
    val apkPath: String,
    val isSystemApp: Boolean,
    val signatureSha256: String,
    val permissions: List<String>,
    val activities: List<String>,
    val services: List<String>,
    val receivers: List<String>,
    val providers: List<String>,
    val dataDir: String,
    val nativeLibraryDir: String?,
    val uid: Int,
    val installSource: String
)

enum class AppFilter {
    ALL,
    USER,
    SYSTEM,
    UNKNOWN_SOURCE
}
