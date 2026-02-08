package pravs.techhub.appanalyzer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import pravs.techhub.appanalyzer.model.AppInfo
import pravs.techhub.appanalyzer.utils.AppInfoManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pravs.techhub.appanalyzer.R

class AppDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var appIcon: ImageView
    private lateinit var appName: TextView
    private lateinit var developerName: TextView
    private lateinit var categoryText: TextView
    private lateinit var openButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var uninstallButton: MaterialButton
    private lateinit var generalInfoContainer: LinearLayout
    private lateinit var securityInfoContainer: LinearLayout
    private lateinit var componentsContainer: LinearLayout
    private lateinit var permissionsContainer: LinearLayout

    private var appInfo: AppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        initViews()
        setupToolbar()
        loadAppDetails()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        appIcon = findViewById(R.id.appIcon)
        appName = findViewById(R.id.appName)
        developerName = findViewById(R.id.developerName)
        categoryText = findViewById(R.id.categoryText)
        openButton = findViewById(R.id.openButton)
        shareButton = findViewById(R.id.shareButton)
        uninstallButton = findViewById(R.id.uninstallButton)
        generalInfoContainer = findViewById(R.id.generalInfoContainer)
        securityInfoContainer = findViewById(R.id.securityInfoContainer)
        componentsContainer = findViewById(R.id.componentsContainer)
        permissionsContainer = findViewById(R.id.permissionsContainer)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadAppDetails() {
        val packageName = intent.getStringExtra("package_name") ?: return

        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppInfoManager.getAllInstalledApps(this@AppDetailActivity)
            }

            appInfo = apps.find { it.packageName == packageName }
            appInfo?.let { displayAppDetails(it) }
        }
    }

    private fun displayAppDetails(app: AppInfo) {
        // Header Information
        appIcon.setImageDrawable(app.icon)
        appName.text = app.appName
        developerName.text = app.developer
        categoryText.text = "${app.category} • v${app.versionName}"

        // Button Actions
        setupButtons(app)

        // General Information
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.package_name),
            app.packageName
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.version),
            app.versionName
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.version_code),
            app.versionCode.toString()
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.size),
            AppInfoManager.formatSize(app.size)
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.installed),
            AppInfoManager.formatDate(app.installedDate)
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.last_updated),
            AppInfoManager.formatDate(app.lastUpdated)
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.target_sdk),
            app.targetSdk.toString()
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.min_sdk),
            app.minSdk.toString()
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.apk_path),
            app.apkPath
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.data_directory),
            app.dataDir
        )
        app.nativeLibraryDir?.let {
            addInfoField(
                generalInfoContainer,
                R.drawable.ic_info,
                getString(R.string.native_library),
                it
            )
        }
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.uid),
            app.uid.toString()
        )
        addInfoField(
            generalInfoContainer,
            R.drawable.ic_info,
            getString(R.string.install_source),
            app.installSource,
            isLast = true
        )

        // Security Information
        addInfoField(
            securityInfoContainer,
            R.drawable.ic_security,
            getString(R.string.signature_sha256),
            app.signatureSha256
        )
        addInfoField(
            securityInfoContainer,
            R.drawable.ic_security,
            getString(R.string.is_system_app),
            if (app.isSystemApp) "Yes" else "No",
            isLast = true
        )

        // Components
        addInfoField(
            componentsContainer,
            R.drawable.ic_components,
            getString(R.string.activities),
            "${app.activities.size} activities"
        )
        addInfoField(
            componentsContainer,
            R.drawable.ic_components,
            getString(R.string.services),
            "${app.services.size} services"
        )
        addInfoField(
            componentsContainer,
            R.drawable.ic_components,
            getString(R.string.receivers),
            "${app.receivers.size} receivers"
        )
        addInfoField(
            componentsContainer,
            R.drawable.ic_components,
            getString(R.string.providers),
            "${app.providers.size} providers",
            isLast = true
        )

        // Permissions
        if (app.permissions.isNotEmpty()) {
            app.permissions.forEachIndexed { index, permission ->
                val permissionName = permission.substringAfterLast(".")
                addInfoField(
                    permissionsContainer,
                    R.drawable.ic_permission,
                    permissionName,
                    permission,
                    isLast = index == app.permissions.size - 1
                )
            }
        } else {
            addInfoField(
                permissionsContainer,
                R.drawable.ic_permission,
                getString(R.string.no_permissions),
                "",
                isLast = true
            )
        }
    }

    private fun addInfoField(
        container: LinearLayout,
        iconRes: Int,
        label: String,
        value: String,
        isLast: Boolean = false
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_info_field, container, false)

        val fieldIcon = view.findViewById<ImageView>(R.id.fieldIcon)
        val fieldLabel = view.findViewById<TextView>(R.id.fieldLabel)
        val fieldValue = view.findViewById<TextView>(R.id.fieldValue)
        val divider = view.findViewById<View>(R.id.divider)

        fieldIcon.setImageResource(iconRes)
        fieldLabel.text = label
        fieldValue.text = value

        if (isLast) {
            divider.visibility = View.GONE
        }

        // Make field copyable
        view.setOnClickListener {
            if (value.isNotEmpty()) {
                copyToClipboard(label, value)
            }
        }

        container.addView(view)
    }

    private fun setupButtons(app: AppInfo) {
        // Open button
        openButton.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, R.string.cannot_open_app, Toast.LENGTH_SHORT).show()
            }
        }

        // Share button
        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, app.appName)
                putExtra(
                    Intent.EXTRA_TEXT,
                    """
                    App: ${app.appName}
                    Package: ${app.packageName}
                    Version: ${app.versionName}
                    Size: ${AppInfoManager.formatSize(app.size)}
                    """.trimIndent()
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Share App Info"))
        }

        // Uninstall button
        uninstallButton.setOnClickListener {
            if (app.isSystemApp) {
                Toast.makeText(this, R.string.cannot_uninstall_system_app, Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${app.packageName}")
                }
                startActivity(intent)
            }
        }
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }
}
