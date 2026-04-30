package com.socialauto;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PostDao_Impl implements PostDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Post> __insertionAdapterOfPost;

  private final EntityDeletionOrUpdateAdapter<Post> __deletionAdapterOfPost;

  private final EntityDeletionOrUpdateAdapter<Post> __updateAdapterOfPost;

  private final SharedSQLiteStatement __preparedStmtOfMarkExecuted;

  private final SharedSQLiteStatement __preparedStmtOfClearExecuted;

  public PostDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPost = new EntityInsertionAdapter<Post>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `posts` (`id`,`content`,`targetApp`,`scheduledTime`,`isExecuted`,`useClipboard`,`useSmartShare`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Post entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getContent());
        statement.bindString(3, entity.getTargetApp());
        statement.bindLong(4, entity.getScheduledTime());
        final int _tmp = entity.isExecuted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.getUseClipboard() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        final int _tmp_2 = entity.getUseSmartShare() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
      }
    };
    this.__deletionAdapterOfPost = new EntityDeletionOrUpdateAdapter<Post>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `posts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Post entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPost = new EntityDeletionOrUpdateAdapter<Post>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `posts` SET `id` = ?,`content` = ?,`targetApp` = ?,`scheduledTime` = ?,`isExecuted` = ?,`useClipboard` = ?,`useSmartShare` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Post entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getContent());
        statement.bindString(3, entity.getTargetApp());
        statement.bindLong(4, entity.getScheduledTime());
        final int _tmp = entity.isExecuted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.getUseClipboard() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        final int _tmp_2 = entity.getUseSmartShare() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfMarkExecuted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE posts SET isExecuted = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearExecuted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM posts WHERE isExecuted = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Post post, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPost.insertAndReturnId(post);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Post post, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPost.handle(post);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Post post, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPost.handle(post);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markExecuted(final long postId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkExecuted.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, postId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkExecuted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearExecuted(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearExecuted.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearExecuted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Post>> getPendingPosts() {
    final String _sql = "SELECT * FROM posts WHERE isExecuted = 0 ORDER BY scheduledTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"posts"}, false, new Callable<List<Post>>() {
      @Override
      @Nullable
      public List<Post> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTargetApp = CursorUtil.getColumnIndexOrThrow(_cursor, "targetApp");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfIsExecuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isExecuted");
          final int _cursorIndexOfUseClipboard = CursorUtil.getColumnIndexOrThrow(_cursor, "useClipboard");
          final int _cursorIndexOfUseSmartShare = CursorUtil.getColumnIndexOrThrow(_cursor, "useSmartShare");
          final List<Post> _result = new ArrayList<Post>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Post _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpTargetApp;
            _tmpTargetApp = _cursor.getString(_cursorIndexOfTargetApp);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final boolean _tmpIsExecuted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsExecuted);
            _tmpIsExecuted = _tmp != 0;
            final boolean _tmpUseClipboard;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseClipboard);
            _tmpUseClipboard = _tmp_1 != 0;
            final boolean _tmpUseSmartShare;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfUseSmartShare);
            _tmpUseSmartShare = _tmp_2 != 0;
            _item = new Post(_tmpId,_tmpContent,_tmpTargetApp,_tmpScheduledTime,_tmpIsExecuted,_tmpUseClipboard,_tmpUseSmartShare);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDuePosts(final long currentTime,
      final Continuation<? super List<Post>> $completion) {
    final String _sql = "SELECT * FROM posts WHERE isExecuted = 0 AND scheduledTime <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Post>>() {
      @Override
      @NonNull
      public List<Post> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTargetApp = CursorUtil.getColumnIndexOrThrow(_cursor, "targetApp");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfIsExecuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isExecuted");
          final int _cursorIndexOfUseClipboard = CursorUtil.getColumnIndexOrThrow(_cursor, "useClipboard");
          final int _cursorIndexOfUseSmartShare = CursorUtil.getColumnIndexOrThrow(_cursor, "useSmartShare");
          final List<Post> _result = new ArrayList<Post>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Post _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpTargetApp;
            _tmpTargetApp = _cursor.getString(_cursorIndexOfTargetApp);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final boolean _tmpIsExecuted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsExecuted);
            _tmpIsExecuted = _tmp != 0;
            final boolean _tmpUseClipboard;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseClipboard);
            _tmpUseClipboard = _tmp_1 != 0;
            final boolean _tmpUseSmartShare;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfUseSmartShare);
            _tmpUseSmartShare = _tmp_2 != 0;
            _item = new Post(_tmpId,_tmpContent,_tmpTargetApp,_tmpScheduledTime,_tmpIsExecuted,_tmpUseClipboard,_tmpUseSmartShare);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Post>> getAllPosts() {
    final String _sql = "SELECT * FROM posts ORDER BY scheduledTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"posts"}, false, new Callable<List<Post>>() {
      @Override
      @Nullable
      public List<Post> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTargetApp = CursorUtil.getColumnIndexOrThrow(_cursor, "targetApp");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfIsExecuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isExecuted");
          final int _cursorIndexOfUseClipboard = CursorUtil.getColumnIndexOrThrow(_cursor, "useClipboard");
          final int _cursorIndexOfUseSmartShare = CursorUtil.getColumnIndexOrThrow(_cursor, "useSmartShare");
          final List<Post> _result = new ArrayList<Post>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Post _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final String _tmpTargetApp;
            _tmpTargetApp = _cursor.getString(_cursorIndexOfTargetApp);
            final long _tmpScheduledTime;
            _tmpScheduledTime = _cursor.getLong(_cursorIndexOfScheduledTime);
            final boolean _tmpIsExecuted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsExecuted);
            _tmpIsExecuted = _tmp != 0;
            final boolean _tmpUseClipboard;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfUseClipboard);
            _tmpUseClipboard = _tmp_1 != 0;
            final boolean _tmpUseSmartShare;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfUseSmartShare);
            _tmpUseSmartShare = _tmp_2 != 0;
            _item = new Post(_tmpId,_tmpContent,_tmpTargetApp,_tmpScheduledTime,_tmpIsExecuted,_tmpUseClipboard,_tmpUseSmartShare);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
