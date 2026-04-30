package com.socialauto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val targetApp: String,
    val scheduledTime: Long,
    val isExecuted: Boolean = false,
    val useClipboard: Boolean = true,
    val useSmartShare: Boolean = false
)

enum class TargetApp(val packageName: String, val displayName: String, val shareType: String) {
    INSTAGRAM("com.instagram.android", "Instagram", "image/*"),
    TIKTOK("com.zhiliaoapp.musically", "TikTok", "text/plain"),
    TWITTER("com.twitter.android", "Twitter/X", "text/plain"),
    FACEBOOK("com.facebook.katana", "Facebook", "text/plain"),
    THREADS("com.instagram.barcelona", "Threads", "text/plain"),
    SNAPCHAT("com.snapchat.android", "Snapchat", "image/*"),
    LINKEDIN("com.linkedin.android", "LinkedIn", "text/plain");

    companion object {
        fun fromPackageName(pkg: String): TargetApp? = values().find { it.packageName == pkg }
        fun fromName(name: String): TargetApp? = values().find { it.name.equals(name, ignoreCase = true) }
    }
}