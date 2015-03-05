package de.lechner.weighttracker;

/**
 * Author: Thomas Lechner on 11.02.2015.
 *
 * This class represents an entry in the database.
 */
public class DatabaseEntry {

    private float weight;
    private int year;
    private int month;
    private int day;

    public DatabaseEntry(float weight, int year, int month, int day) {
        this.weight = weight;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public float getValue() {
        return this.weight;
    }

    public int getYear() {
        return this.year;
    }

    public int getMonth() {
        return this.month;
    }

    public int getDay() {
        return this.day;
    }
}
