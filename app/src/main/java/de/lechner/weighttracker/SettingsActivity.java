package de.lechner.weighttracker;

import android.app.Activity;
import android.os.Bundle;

/**
 * Author: Thomas Lechner on 04.05.2015.
 */
public class SettingsActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
