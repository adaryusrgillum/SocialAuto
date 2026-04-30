package com.socialauto

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlinx.coroutines.*

class SocialAutoService : AccessibilityService() {

    companion object {
        const val ACTION_EXECUTE_POST = "com.socialauto.EXECUTE_POST"
        private const val TAG = "SocialAutoService"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingContent: String? = null
    private var pendingTarget: String? = null
    private var automationJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        showToast("SocialAuto Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (pendingContent == null) return

        val rootNode = rootInActiveWindow ?: return
        val packageName = event.packageName?.toString() ?: return
        val targetApp = TargetApp.fromName(pendingTarget ?: "") ?: return

        if (packageName != targetApp.packageName) {
            rootNode.recycle()
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                serviceScope.launch {
                    delay(800) // Wait for UI to settle
                    performAutomation(rootNode, targetApp)
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_EXECUTE_POST -> {
                val content = intent.getStringExtra("content") ?: return START_NOT_STICKY
                val target = intent.getStringExtra("target_app") ?: return START_NOT_STICKY
                val useSmartShare = intent.getBooleanExtra("use_smart_share", false)

                pendingContent = content
                pendingTarget = target

                if (useSmartShare) {
                    launchSmartShare(content, target)
                } else {
                    setClipboard(content)
                    launchTargetApp(target)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun launchSmartShare(content: String, targetAppName: String) {
        val target = TargetApp.fromName(targetAppName) ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = target.shareType
            putExtra(Intent.EXTRA_TEXT, content)
            `package` = target.packageName
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(shareIntent)
            showToast("Opened ${target.displayName} with content")
            clearPending()
        } catch (e: Exception) {
            showToast("Failed to open ${target.displayName}. Trying clipboard mode...")
            setClipboard(content)
            launchTargetApp(targetAppName)
        }
    }

    private fun setClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("SocialAuto", text))
    }

    private fun launchTargetApp(targetAppName: String) {
        val target = TargetApp.fromName(targetAppName) ?: run {
            showToast("Unknown app: $targetAppName")
            return
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(target.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
            showToast("Launching ${target.displayName}...")
        } else {
            showToast("${target.displayName} not installed")
            clearPending()
        }
    }

    private fun performAutomation(rootNode: AccessibilityNodeInfo, targetApp: TargetApp) {
        val content = pendingContent ?: return

        // Strategy 1: Find EditText and paste
        val editTexts = findNodesByClass(rootNode, "android.widget.EditText")
        val textInputs = findNodesByClass(rootNode, "android.widget.TextView")
            .filter { it.isEditable }

        val allInputs = editTexts + textInputs

        if (allInputs.isNotEmpty()) {
            val targetField = allInputs.firstOrNull { it.hintText?.contains("caption", true) == true
                    || it.hintText?.contains("post", true) == true
                    || it.hintText?.contains("what's", true) == true
                    || it.hintText?.contains("happening", true) == true
            } ?: allInputs.first()

            // Set text directly if possible, otherwise paste
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content)
            }
            val success = targetField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            if (!success) {
                targetField.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                targetField.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            }

            // Wait then click post button
            serviceScope.launch {
                delay(1000)
                val refreshedRoot = rootInActiveWindow ?: return@launch
                clickPostButton(refreshedRoot, targetApp)
                refreshedRoot.recycle()
            }
        } else {
            // If no input found, try clicking create post buttons first
            serviceScope.launch {
                delay(500)
                val refreshedRoot = rootInActiveWindow ?: return@launch
                val createLabels = when (targetApp) {
                    TargetApp.INSTAGRAM -> listOf("Create", "New post", "+", "Compose")
                    TargetApp.TIKTOK -> listOf("+", "Create", "Post")
                    TargetApp.TWITTER -> listOf("Compose", "+", "New post")
                    TargetApp.FACEBOOK -> listOf("What's on your mind?", "Create post")
                    TargetApp.THREADS -> listOf("Create", "New thread", "+")
                    TargetApp.SNAPCHAT -> listOf("Camera", "Create")
                    TargetApp.LINKEDIN -> listOf("Start a post", "Create a post", "Write article", "Post")
                }
                val createButtons = findNodesByText(refreshedRoot, createLabels)
                if (createButtons.isNotEmpty()) {
                    createButtons.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    delay(1500)
                    val newRoot = rootInActiveWindow ?: return@launch
                    performAutomation(newRoot, targetApp)
                    newRoot.recycle()
                }
                refreshedRoot.recycle()
            }
        }

        rootNode.recycle()
    }

    private fun clickPostButton(rootNode: AccessibilityNodeInfo, targetApp: TargetApp) {
        val postLabels = when (targetApp) {
            TargetApp.INSTAGRAM -> listOf("Share", "Post", "Share to")
            TargetApp.TIKTOK -> listOf("Post", "Publish", "Next")
            TargetApp.TWITTER -> listOf("Post", "Tweet", "Post now")
            TargetApp.FACEBOOK -> listOf("Post", "Share now", "Publish")
            TargetApp.THREADS -> listOf("Post", "Publish")
            TargetApp.SNAPCHAT -> listOf("Send", "Post to Story")
            TargetApp.LINKEDIN -> listOf("Post", "Publish", "Share")
        }

        val buttons = findNodesByText(rootNode, postLabels)
            .filter { it.isClickable || it.parent?.isClickable == true }

        if (buttons.isNotEmpty()) {
            val btn = buttons.first()
            if (btn.isClickable) {
                btn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                btn.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            showToast("Post action triggered!")
            clearPending()
        }
    }

    private fun findNodesByClass(node: AccessibilityNodeInfo, className: String): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        if (node.className?.toString() == className) {
            results.add(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            results.addAll(findNodesByClass(child, className))
        }
        return results
    }

    private fun findNodesByText(node: AccessibilityNodeInfo, texts: List<String>): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        val nodeText = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
        if (texts.any { nodeText.equals(it, ignoreCase = true) || nodeText.contains(it, ignoreCase = true) }) {
            results.add(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            results.addAll(findNodesByText(child, texts))
        }
        return results
    }

    private fun clearPending() {
        pendingContent = null
        pendingTarget = null
        automationJob?.cancel()
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}