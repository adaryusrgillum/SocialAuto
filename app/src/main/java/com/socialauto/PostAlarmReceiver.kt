package com.socialauto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val postId = intent.getLongExtra("post_id", -1)
        if (postId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = PostDatabase.getDatabase(context)
            val post = db.postDao().getDuePosts(System.currentTimeMillis()).find { it.id == postId }
            post?.let {
                // Trigger accessibility service
                val serviceIntent = Intent(context, SocialAutoService::class.java).apply {
                    action = SocialAutoService.ACTION_EXECUTE_POST
                    putExtra("post_id", it.id)
                    putExtra("content", it.content)
                    putExtra("target_app", it.targetApp)
                    putExtra("use_clipboard", it.useClipboard)
                    putExtra("use_smart_share", it.useSmartShare)
                }
                context.startService(serviceIntent)
                db.postDao().markExecuted(it.id)
            }
        }
    }
}