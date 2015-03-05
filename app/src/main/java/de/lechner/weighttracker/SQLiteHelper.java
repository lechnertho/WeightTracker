package de.lechner.weighttracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Author: Thomas Lechner on 11.02.2015.
 *
 * This Class creates a SQLite Database. The database is called weights.db. The database has one
 * table with 3 rows(_id, weight, date).
 */
public class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(Context context) {
            super(context, "weights.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table weights(_id integer primary key autoincrement, weight real not null, year int not null, month int not null, day int not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //TODO: ?
        }
}