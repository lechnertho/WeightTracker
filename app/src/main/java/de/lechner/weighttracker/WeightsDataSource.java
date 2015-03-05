package de.lechner.weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Author: Thomas Lechner on 11.02.2015.
 *
 * This Class represents the source for the weights. On first use a SQLite database will be created.
 * The data in the database can be accessed and manipulated through the functions in this class.
 */
public class WeightsDataSource {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase database;

    public WeightsDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    /**
     * Call this method when you want to use the database and it is currently closed.
     *
     * @throws SQLiteException
     */
    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Call this method when the database is no longer used. e.g. when the Activity is paused.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Create a new entry in the database. If there is an entry for the date, the entry will be
     * deleted before the new entry is created.
     *
     * @param weight the weight you want to enter in the database.
     * @param year the year when the weight was measured.
     * @param month the month when the weight was measured.
     * @param day the day when the weight was measured.
     */
    public void createEntry(float weight, int year, int month, int day) {

        database.rawQuery("DELETE FROM weights WHERE year = " + year + " AND month = " + month + " AND day = " + day, null);

        ContentValues values = new ContentValues();
        values.put("weight", weight);
        values.put("year", year);
        values.put("month", month);
        values.put("day", day);
        database.insert("weights", null, values);
    }

    /**
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public boolean hasEntry(int year, int month, int day) {
        Cursor cursor = database.rawQuery("SELECT * FROM weights WHERE year = " + year + " AND month = " + month + " AND day = " + day, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns the databaseentry for the specified date. If there is no entry for the date null will
     * be turned.
     *
     * @param year
     * @param month
     * @param day
     * @return databaseentry
     */
    public DatabaseEntry getEntry(int year, int month, int day) {
        Cursor cursor = database.rawQuery("SELECT * FROM weights WHERE year = " + year + " AND month = " + month + " AND day = " + day, null);
        cursor.moveToFirst();
        if(cursor.getCount() == 0) {
            return null;
        } else {
            return cursorToEntry(cursor);
        }
    }

    /**
     * This methods reads all the entries from the database for a specific month. The entries are
     * sorted by date.
     * @param year
     * @param month
     * @return list of DatabaseEntrys
     */
    public List<DatabaseEntry> getEntriesMonth(int year, int month) {
        List<DatabaseEntry> entries = new ArrayList<DatabaseEntry>();

        Cursor cursor = database.rawQuery("SELECT * FROM weights WHERE year = " + year + " AND month = " + month + " ORDER BY year, month, day", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DatabaseEntry entry = cursorToEntry(cursor);
            entries.add(entry);
            cursor.moveToNext();
        }
        cursor.close();
        return entries;
    }

    /**
     * This methods reads all the entries from the weights database and returns them in a list.
     * @return list with all DatabaseEntries
     */
    public List<DatabaseEntry> getAllEntries() {
        Cursor cursor = database.rawQuery("SELECT * FROM weights", null);
        List<DatabaseEntry> entries = new ArrayList<DatabaseEntry>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DatabaseEntry entry = cursorToEntry(cursor);
            entries.add(entry);
            cursor.moveToNext();
        }
        cursor.close();
        return entries;
    }

    /**
     * This methods "casts" a cursor in the database to a DatabaseEntry object.
     *
     * @param cursor the cursor you want to casts to a DatabaseEntry object
     * @return DatabaseEntry object with the attributes from the cursor
     */
    private DatabaseEntry cursorToEntry(Cursor cursor) {
        DatabaseEntry entry = new DatabaseEntry(cursor.getFloat(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4));
        return entry;
    }
}