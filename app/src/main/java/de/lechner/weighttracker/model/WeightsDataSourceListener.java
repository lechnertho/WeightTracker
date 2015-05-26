package de.lechner.weighttracker.model;

import de.lechner.weighttracker.model.DatabaseEntry;

/**
 * Author: Thomas Lechner on 25.04.2015.
 */
public interface WeightsDataSourceListener {
    public void dataSourceChanged(DatabaseEntry entry);
}
