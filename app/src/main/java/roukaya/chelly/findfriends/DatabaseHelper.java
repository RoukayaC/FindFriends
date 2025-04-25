package roukaya.chelly.findfriends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for managing the SQLite database operations.
 * Handles friends' location data storage and retrieval.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database info
    private static final String DATABASE_NAME = "friends_location.db";
    private static final int DATABASE_VERSION = 1;

    // Table for storing friends and their locations
    public static final String TABLE_FRIENDS = "friends";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_LAST_LATITUDE = "latitude";
    public static final String COLUMN_LAST_LONGITUDE = "longitude";
    public static final String COLUMN_LAST_REQUEST_TIME = "request_time";

    // SQL to create the friends table
    private static final String CREATE_TABLE_FRIENDS =
            "CREATE TABLE " + TABLE_FRIENDS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PHONE + " TEXT UNIQUE, " +
                    COLUMN_LAST_LATITUDE + " REAL, " +
                    COLUMN_LAST_LONGITUDE + " REAL, " +
                    COLUMN_LAST_REQUEST_TIME + " INTEGER);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FRIENDS);
        Log.d(TAG, "Friends table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This is a simple upgrade strategy - drop and recreate the table
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }

    /**
     * Add a new friend or update an existing one with the same phone number
     */
    public void addFriend(String phone, double latitude, double longitude) {
        // Skip if phone number is empty
        if (phone == null || phone.trim().isEmpty()) {
            Log.e(TAG, "Cannot add friend with empty phone number");
            return;
        }

        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PHONE, phone);
            values.put(COLUMN_LAST_LATITUDE, latitude);
            values.put(COLUMN_LAST_LONGITUDE, longitude);
            values.put(COLUMN_LAST_REQUEST_TIME, System.currentTimeMillis());

            // Insert with CONFLICT_REPLACE will update existing records with the same phone number
            long result = db.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            if (result == -1) {
                Log.e(TAG, "Failed to add friend: " + phone);
            } else {
                Log.d(TAG, "Friend added or updated: " + phone);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding friend: " + e.getMessage());
        }
    }

    /**
     * Get all friends with their last known locations
     * @return Cursor containing all friend records sorted by most recent first
     */
    public Cursor getAllFriends() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(
                    TABLE_FRIENDS,        // Table name
                    null,                 // All columns
                    null,                 // No WHERE clause
                    null,                 // No WHERE arguments
                    null,                 // No GROUP BY
                    null,                 // No HAVING
                    COLUMN_LAST_REQUEST_TIME + " DESC"  // Order by most recent first
            );
        } catch (Exception e) {
            Log.e(TAG, "Error getting all friends: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a friend record by ID
     * @return true if deletion was successful
     */
    public boolean deleteFriend(long id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            int rowsDeleted = db.delete(
                    TABLE_FRIENDS,
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)}
            );
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting friend: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search for friends by phone number
     * @param query The search query
     * @return Cursor with matching friends
     */
    public Cursor searchFriends(String query) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            String selection = COLUMN_PHONE + " LIKE ?";
            String[] selectionArgs = new String[]{"%" + query + "%"};

            return db.query(
                    TABLE_FRIENDS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    COLUMN_LAST_REQUEST_TIME + " DESC"
            );
        } catch (Exception e) {
            Log.e(TAG, "Error searching friends: " + e.getMessage());
            return null;
        }
    }
}