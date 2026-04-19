package pravs.techhub.appanalyzer.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pravs.techhub.appanalyzer.R
import pravs.techhub.appanalyzer.model.AppFilter
import pravs.techhub.appanalyzer.model.AppInfo
import pravs.techhub.appanalyzer.ui.adapter.AppsAdapter
import pravs.techhub.appanalyzer.utils.AppInfoManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipUser: Chip
    private lateinit var chipSystem: Chip
    private lateinit var chipUnknown: Chip
    private lateinit var appCountTextView: TextView
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var appsAdapter: AppsAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()
    private var currentFilter = AppFilter.ALL
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupSearchAndFilters()
        loadApps()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        chipAll = findViewById(R.id.chipAll)
        chipUser = findViewById(R.id.chipUser)
        chipSystem = findViewById(R.id.chipSystem)
        chipUnknown = findViewById(R.id.chipUnknown)
        appCountTextView = findViewById(R.id.appCountTextView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        appsAdapter = AppsAdapter { appInfo ->
            openAppDetails(appInfo)
        }

        appsRecyclerView.apply {
            adapter = appsAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 4)
        }
    }

    private fun setupSearchAndFilters() {
        // Search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.lowercase() ?: ""
                filterApps()
            }
        })

        // Filter chip functionality
        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                R.id.chipUser -> AppFilter.USER
                R.id.chipSystem -> AppFilter.SYSTEM
                R.id.chipUnknown -> AppFilter.UNKNOWN_SOURCE
                else -> AppFilter.ALL
            }
            filterApps()
        }
    }

    private fun loadApps() {
        progressBar.visibility = View.VISIBLE
        appsRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            allApps = withContext(Dispatchers.IO) {
                AppInfoManager.getAllInstalledApps(this@MainActivity)
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                appsRecyclerView.visibility = View.VISIBLE
                filterApps()
            }
        }
    }

    private fun filterApps() {
        filteredApps = allApps.filter { app ->
            // Apply filter
            val matchesFilter = when (currentFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
                AppFilter.UNKNOWN_SOURCE -> app.installSource == "Unknown" && app.isSystemApp.not() ||
                        app.installSource.isEmpty() && app.isSystemApp.not() ||
                        (AppInfoManager.listOfOEM.contains(app.installSource).not() && app.isSystemApp.not())
            }

            // Apply search
            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                app.appName.lowercase().contains(searchQuery) ||
                        app.packageName.lowercase().contains(searchQuery) ||
                        app.signatureMD5.lowercase().contains(searchQuery.lowercase()) ||
                        app.signatureSha256.lowercase().contains(searchQuery.lowercase()) ||
                        app.signatureSha1.lowercase().contains(searchQuery.lowercase())
            }

            matchesFilter && matchesSearch
        }

        appsAdapter.submitList(filteredApps)
        updateAppCount()
    }

    private fun updateAppCount() {
        appCountTextView.text = getString(R.string.apps_installed, filteredApps.size)
    }

    private fun openAppDetails(appInfo: AppInfo) {
        val intent = Intent(this, AppDetailActivity::class.java).apply {
            putExtra("package_name", appInfo.packageName)
        }
        startActivity(intent)
    }
}
