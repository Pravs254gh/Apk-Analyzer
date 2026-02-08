package pravs.techhub.appanalyzer.utils


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import pravs.techhub.appanalyzer.model.AppInfo
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

object AppInfoManager {

    fun getAllInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS or
                    PackageManager.GET_SIGNING_CERTIFICATES or
                    PackageManager.GET_SIGNATURES
        )

        return packages.mapNotNull { packageInfo ->
            try {
                createAppInfo(context, packageInfo)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }

    private fun createAppInfo(context: Context, packageInfo: PackageInfo): AppInfo? {
        val packageManager = context.packageManager
        val applicationInfo = packageInfo.applicationInfo
        if(applicationInfo==null){
            return null
        }
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val icon = packageManager.getApplicationIcon(applicationInfo)
        val apkFile = File(applicationInfo.sourceDir)
        val size = apkFile.length()

        val installedDate = packageInfo.firstInstallTime
        val lastUpdated = packageInfo.lastUpdateTime

        val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val signatureSha256 = getSignatureSha256(packageInfo)

        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

        val activities = packageInfo.activities?.map { it.name } ?: emptyList()
        val services = packageInfo.services?.map { it.name } ?: emptyList()
        val receivers = packageInfo.receivers?.map { it.name } ?: emptyList()
        val providers = packageInfo.providers?.map { it.name } ?: emptyList()

        val targetSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo.targetSdkVersion
        } else {
            applicationInfo.targetSdkVersion
        }

        val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo.minSdkVersion
        } else {
            0
        }

        val installSource = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageInfo.packageName).installingPackageName ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageInfo.packageName) ?: "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }

        return AppInfo(
            appName = appName,
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            },
            icon = icon,
            size = size,
            installedDate = installedDate,
            lastUpdated = lastUpdated,
            targetSdk = targetSdk,
            minSdk = minSdk,
            developer = "Google LLC", // This is a placeholder, actual developer info is not available
            category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {applicationInfo.category?.let { getCategoryName(it) }?:"ISNULL"}else{"NODATA"},
            apkPath = applicationInfo.sourceDir?:"null",
            isSystemApp = isSystemApp,
            signatureSha256 = signatureSha256,
            permissions = permissions,
            activities = activities,
            services = services,
            receivers = receivers,
            providers = providers,
            dataDir = applicationInfo.dataDir?:"null",
            nativeLibraryDir = applicationInfo.nativeLibraryDir,
            uid = applicationInfo.uid?:0,
            installSource = installSource
        )
    }

    private fun getSignatureSha256(packageInfo: PackageInfo): String {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.let { signingInfo ->
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null && signatures.isNotEmpty()) {
                val cert = signatures[0].toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(cert)
                digest.joinToString(":") { String.format("%02x", it) }
            } else {
                "Not Available"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun getCategoryName(category: Int): String {
        return when (category) {
            ApplicationInfo.CATEGORY_GAME -> "Game"
            ApplicationInfo.CATEGORY_AUDIO -> "Audio"
            ApplicationInfo.CATEGORY_VIDEO -> "Video"
            ApplicationInfo.CATEGORY_IMAGE -> "Image"
            ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            ApplicationInfo.CATEGORY_NEWS -> "News"
            ApplicationInfo.CATEGORY_MAPS -> "Maps"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            else -> "Communication"
        }
    }

    fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}