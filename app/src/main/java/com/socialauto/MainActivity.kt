package com.socialauto

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.AccessibilityService
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var db: PostDatabase
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = PostDatabase.getDatabase(this)

        tvStatus = findViewById(R.id.tvStatus)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PostAdapter { post ->
            lifecycleScope.launch {
                db.postDao().delete(post)
            }
        }
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddPostDialog()
        }

        findViewById<Button>(R.id.btnLinkedIn).setOnClickListener {
            startActivity(Intent(this, LinkedInWebActivity::class.java))
        }

        db.postDao().getPendingPosts().observe(this) { posts ->
            adapter.submitList(posts)
        }

        updateAccessibilityStatus()
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }

    private fun updateAccessibilityStatus() {
        val enabled = isAccessibilityServiceEnabled(this, SocialAutoService::class.java)

        tvStatus.text = if (enabled) {
            "Status: Service Active"
        } else {
            "Status: Accessibility Service OFF - Tap to enable"
        }
        tvStatus.setOnClickListener {
            if (!enabled) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName &&
                it.resolveInfo.serviceInfo.name == service.name }
    }

    private fun showAddPostDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_post, null)
        val etContent = view.findViewById<EditText>(R.id.etContent)
        val spinnerApp = view.findViewById<Spinner>(R.id.spinnerApp)
        val timePicker = view.findViewById<TimePicker>(R.id.timePicker)
        val checkSmartShare = view.findViewById<CheckBox>(R.id.checkSmartShare)
        val checkClipboard = view.findViewById<CheckBox>(R.id.checkClipboard)

        val apps = TargetApp.values().map { it.displayName }
        spinnerApp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, apps).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        checkClipboard.isChecked = true

        AlertDialog.Builder(this)
            .setTitle("Schedule Post")
            .setView(view)
            .setPositiveButton("Schedule") { _, _ ->
                val content = etContent.text.toString().trim()
                if (content.isEmpty()) {
                    Toast.makeText(this, "Content required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedApp = TargetApp.values()[spinnerApp.selectedItemPosition]
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
                    set(Calendar.SECOND, 0)
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                val post = Post(
                    content = content,
                    targetApp = selectedApp.name,
                    scheduledTime = calendar.timeInMillis,
                    useClipboard = checkClipboard.isChecked,
                    useSmartShare = checkSmartShare.isChecked
                )

                lifecycleScope.launch {
                    val id = db.postDao().insert(post)
                    scheduleAlarm(id, calendar.timeInMillis)
                    Toast.makeText(
                        this@MainActivity,
                        "Scheduled for ${selectedApp.displayName} at ${calendar.time}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleAlarm(postId: Long, timeMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, PostAlarmReceiver::class.java).apply {
            putExtra("post_id", postId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, postId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                lifecycleScope.launch {
                    db.postDao().clearExecuted()
                    Toast.makeText(this@MainActivity, "Cleared executed posts", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_test_now -> {
                showAddPostDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}