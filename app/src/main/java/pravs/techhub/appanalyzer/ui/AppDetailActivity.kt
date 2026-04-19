package pravs.techhub.appanalyzer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pravs.techhub.appanalyzer.R
import pravs.techhub.appanalyzer.model.AppInfo
import pravs.techhub.appanalyzer.utils.AppInfoManager

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

    // Chips
    private lateinit var chipGeneral: Chip
    private lateinit var chipSecurity: Chip
    private lateinit var chipComponents: Chip
    private lateinit var chipPermissions: Chip

    // Layouts
    private lateinit var layoutGeneral: View
    private lateinit var layoutSecurity: View
    private lateinit var layoutComponents: View
    private lateinit var layoutPermissions: View

    private var appInfo: AppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        initViews()
        setupToolbar()
        setupSectionSwitching()
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

        chipGeneral = findViewById(R.id.chipGeneral)
        chipSecurity = findViewById(R.id.chipSecurity)
        chipComponents = findViewById(R.id.chipComponents)
        chipPermissions = findViewById(R.id.chipPermissions)

        layoutGeneral = findViewById(R.id.layoutGeneral)
        layoutSecurity = findViewById(R.id.layoutSecurity)
        layoutComponents = findViewById(R.id.layoutComponents)
        layoutPermissions = findViewById(R.id.layoutPermissions)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSectionSwitching() {

        fun showOnly(view: View) {
            layoutGeneral.visibility = View.GONE
            layoutSecurity.visibility = View.GONE
            layoutComponents.visibility = View.GONE
            layoutPermissions.visibility = View.GONE

            view.visibility = View.VISIBLE
        }

        chipGeneral.setOnClickListener { showOnly(layoutGeneral) }
        chipSecurity.setOnClickListener { showOnly(layoutSecurity) }
        chipComponents.setOnClickListener { showOnly(layoutComponents) }
        chipPermissions.setOnClickListener { showOnly(layoutPermissions) }
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

        // Header
        appIcon.setImageDrawable(app.icon)
        appName.text = app.appName
        developerName.text = app.developer
        categoryText.text = "${app.category} • v${app.versionName}"

        setupButtons(app)

        // GENERAL
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Package", app.packageName)
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Version", app.versionName)
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Version Code", app.versionCode.toString())
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Size", AppInfoManager.formatSize(app.size))
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Installed", AppInfoManager.formatDate(app.installedDate))
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Updated", AppInfoManager.formatDate(app.lastUpdated))
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Target SDK", app.targetSdk.toString())
        addInfoField(generalInfoContainer, R.drawable.ic_info, "Min SDK", app.minSdk.toString())

        // SECURITY
        addInfoField(securityInfoContainer, R.drawable.ic_security, "SHA-256", app.signatureSha256)
        addInfoField(securityInfoContainer, R.drawable.ic_security, "SHA-1", app.signatureSha1)
        addInfoField(securityInfoContainer, R.drawable.ic_security, "MD5", app.signatureMD5)
        addInfoField(securityInfoContainer, R.drawable.ic_security, "System App", if (app.isSystemApp) "Yes" else "No")

        // COMPONENTS
        addInfoField(componentsContainer, R.drawable.ic_components, "Activities", "${app.activities.size}")
        addInfoField(componentsContainer, R.drawable.ic_components, "Services", "${app.services.size}")
        addInfoField(componentsContainer, R.drawable.ic_components, "Receivers", "${app.receivers.size}")
        addInfoField(componentsContainer, R.drawable.ic_components, "Providers", "${app.providers.size}")

        // PERMISSIONS (CHIPS)
        // Permissions
        if (app.permissions.isNotEmpty()) {
            app.permissions.forEachIndexed { index, permission ->
                val permissionName = permission.substringAfterLast(".")
                addInfoField(
                    permissionsContainer,
                    R.drawable.ic_permission,
                    permissionName,
                    permission
                )
            }
        } else {
            addInfoField(
                permissionsContainer,
                R.drawable.ic_permission,
                getString(R.string.no_permissions),
                ""
            )
        }
    }

    /*private fun addPermissionChips(permissions: List<String>) {
        permissionsChipGroup.removeAllViews()

        if (permissions.isEmpty()) {
            val chip = Chip(this).apply {
                text = "No Permissions"
            }
            permissionsChipGroup.addView(chip)
            return
        }

        permissions.forEach { permission ->
            val chip = Chip(this).apply {
                text = permission.substringAfterLast(".")
                isClickable = true
            }

            chip.setOnClickListener {
                copyToClipboard("Permission", permission)
            }

            permissionsChipGroup.addView(chip)
        }
    }*/

    private fun addInfoField(
        container: LinearLayout,
        iconRes: Int,
        label: String,
        value: String
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_info_field, container, false)

        val fieldIcon = view.findViewById<ImageView>(R.id.fieldIcon)
        val fieldLabel = view.findViewById<TextView>(R.id.fieldLabel)
        val fieldValue = view.findViewById<TextView>(R.id.fieldValue)

        fieldIcon.setImageResource(iconRes)
        fieldLabel.text = label
        fieldValue.text = value

        view.setOnClickListener {
            if (value.isNotEmpty()) {
                copyToClipboard(label, value)
            }
        }

        container.addView(view)
    }

    private fun setupButtons(app: AppInfo) {

        openButton.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) startActivity(intent)
            else Toast.makeText(this, "Cannot open app", Toast.LENGTH_SHORT).show()
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${app.appName}\n${app.packageName}")
            }
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }

        uninstallButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${app.packageName}")
            }
            startActivity(intent)
        }
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }
}