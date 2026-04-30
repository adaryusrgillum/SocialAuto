package com.socialauto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(private val onDelete: (Post) -> Unit) :
    ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvApp: TextView = itemView.findViewById(R.id.tvApp)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(post: Post) {
            tvContent.text = post.content
            tvApp.text = TargetApp.fromName(post.targetApp)?.displayName ?: post.targetApp
            tvTime.text = dateFormat.format(Date(post.scheduledTime))
            tvStatus.text = if (post.isExecuted) "Executed" else "Pending"
            tvStatus.setTextColor(
                if (post.isExecuted) 0xFF4CAF50.toInt() else 0xFFFF9800.toInt()
            )
            btnDelete.setOnClickListener { onDelete(post) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}