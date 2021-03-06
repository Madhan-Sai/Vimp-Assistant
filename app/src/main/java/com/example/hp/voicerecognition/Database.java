package com.example.hp.voicerecognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HP on 08-03-2018.
 */

public class Database extends SQLiteOpenHelper {
    SQLiteDatabase mydb;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "hackathon.db";
    private static final String TABLE_NAME = "commands";
    private static final String KEY_ID = "key";
    private static final String KEY_NAME = "value";
    private static final String KEY_PH_NO = "function";
    public Database(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + KEY_ID + " TEXT," + KEY_NAME + " TEXT," + KEY_PH_NO + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void add(String key,String value,String function) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID,key);
        values.put(KEY_NAME,value);
        values.put(KEY_PH_NO, function);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    String getValue(String key) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID,
                        KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { key }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(1);
    }
    String getFunction(String key) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID,
                        KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { key }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(2);
    }
}
