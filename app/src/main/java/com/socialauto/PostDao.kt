package com.socialauto

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE isExecuted = 0 ORDER BY scheduledTime ASC")
    fun getPendingPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE isExecuted = 0 AND scheduledTime <= :currentTime")
    suspend fun getDuePosts(currentTime: Long): List<Post>

    @Query("SELECT * FROM posts ORDER BY scheduledTime DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: Post): Long

    @Update
    suspend fun update(post: Post)

    @Delete
    suspend fun delete(post: Post)

    @Query("UPDATE posts SET isExecuted = 1 WHERE id = :postId")
    suspend fun markExecuted(postId: Long)

    @Query("DELETE FROM posts WHERE isExecuted = 1")
    suspend fun clearExecuted()
}