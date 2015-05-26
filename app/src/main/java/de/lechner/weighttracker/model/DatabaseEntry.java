package de.lechner.weighttracker.model;

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
    private int week;

    public DatabaseEntry(float weight, int year, int month, int day, int week) {
        this.weight = weight;
        this.year = year;
        this.month = month;
        this.day = day;
        this.week = week;
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

    public int getWeek() {
        return this.week;
    }
}
