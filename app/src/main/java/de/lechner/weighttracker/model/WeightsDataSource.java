package de.lechner.weighttracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Thomas Lechner on 11.02.2015.
 * <p/>
 * This Class represents the source for the weights. On first use a SQLite database will be created.
 * The data in the database can be accessed and manipulated through the functions in this class.
 */
public class WeightsDataSource {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase database;
    private List<WeightsDataSourceListener> weightsDataSourceListeners;

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
     * @param year   the year when the weight was measured.
     * @param month  the month when the weight was measured.
     * @param day    the day when the weight was measured.
     * @param week   the week of the year when the weight was measured.
     */
    public void createEntry(float weight, int year, int month, int day, int week) {

        //rawQuery will only be executed if moveToFirst() is called
        database.rawQuery("DELETE FROM weights WHERE year = " + year + " AND month = " + month + " AND day = " + day + " AND week = " + week, null).moveToFirst();

        ContentValues values = new ContentValues();
        values.put("weight", weight);
        values.put("year", year);
        values.put("month", month);
        values.put("day", day);
        values.put("week", week);
        database.insert("weights", null, values);

        if (weightsDataSourceListeners != null) {
            for (WeightsDataSourceListener listener : weightsDataSourceListeners) {
                listener.dataSourceChanged(new DatabaseEntry(weight, year, month, day, week));
            }
        }
    }

    /**
     * Returns the databaseentry for the specified date. If there is no entry for the date, null is
     * returned.
     *
     * @param year
     * @param month
     * @param day
     * @return databaseentry
     */
    public DatabaseEntry getEntry(int year, int month, int day) {
        Cursor cursor = database.rawQuery("SELECT * FROM weights WHERE year = " + year + " AND month = " + month + " AND day = " + day, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            return null;
        } else {
            return cursorToEntry(cursor);
        }
    }

    /**
     * This method reads all the entries from the database for a specific month. The entries are
     * sorted by date.
     *
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
     * This method reads all the entries from the database for a specific week of a year. The
     * entries are sorted by date.
     *
     * @param year
     * @param week
     * @return list of DatabaseEntrys
     */
    public List<DatabaseEntry> getEntriesWeek(int year, int week) {
        List<DatabaseEntry> entries = new ArrayList<DatabaseEntry>();

        Cursor cursor = database.rawQuery("SELECT * FROM weights WHERE year = " + year + " AND week = " + week + " ORDER BY year, month, day", null);
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
     * This method reads all the entries from the database for a specific year. The entries are
     * sorted by date.
     *
     * @param year
     * @return list of DatabaseEntrys
     */
    public List<DatabaseEntry> getEntriesYear(int year) {
        List<DatabaseEntry> entries = new ArrayList<DatabaseEntry>();

        Cursor cursor = database.rawQuery("SELECT * FROM weights WHERE year = " + year + " ORDER BY year, month, day", null);
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
        DatabaseEntry entry = new DatabaseEntry(cursor.getFloat(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5));
        return entry;
    }

    /**
     * Add a listener, that gets notified when an entry in the Database gets changed.
     *
     * @param listener
     */
    public void setWeightsDataSourceListener(WeightsDataSourceListener listener) {
        if (weightsDataSourceListeners == null) {
            weightsDataSourceListeners = new ArrayList<>();
            weightsDataSourceListeners.add(listener);
        } else {
            weightsDataSourceListeners.add(listener);
        }
    }
}