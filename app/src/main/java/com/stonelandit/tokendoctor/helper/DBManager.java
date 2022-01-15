package com.stonelandit.tokendoctor.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String token, boolean is_used) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper._ID, 1);
        contentValue.put(DatabaseHelper.TOKEN, token);
        contentValue.put(DatabaseHelper.ISUSED, is_used);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.TOKEN, DatabaseHelper.ISUSED };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor get(String Token){
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.TOKEN, DatabaseHelper.ISUSED };
        // calling elements    in an array
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, DatabaseHelper._ID + "=" + Token, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            String name = cursor.getString(1);
            // since name is in position 1 ie second coloumn
            return cursor;
        }
        return null;
    }

    public int update(long _id, String token, boolean is_used) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TOKEN, token);
        contentValues.put(DatabaseHelper.ISUSED, is_used);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }

    public void truncate(){
        database.execSQL("DELETE FROM "+DatabaseHelper.TABLE_NAME);
    }

}