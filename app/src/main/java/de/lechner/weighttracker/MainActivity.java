package de.lechner.weighttracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Calendar;

import de.lechner.weighttracker.model.TrendView;

/**
 * Author: Thomas Lechner on 24.04.2015.
 */
public class MainActivity extends ActionBarActivity {

    private TrendView bodyWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        bodyWeight = (TrendView) findViewById(R.id.trendview_weight);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        LayoutInflater inflater =
                (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_addweight, null);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        int id = item.getItemId();

        switch (id) {
            case R.id.action_add:
                AlertDialog.Builder addWeightAlert = new AlertDialog.Builder(MainActivity.this);
                addWeightAlert.setTitle(R.string.add_weight_dialog);
                addWeightAlert.setView(view);
                addWeightAlert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Float weight = Float.parseFloat(((EditText) view.findViewById(R.id.addweight_edittext)).getText().toString());
                            bodyWeight.addWeight(weight, Calendar.getInstance().get(Calendar.YEAR), (Calendar.getInstance().get(Calendar.MONTH) + 1), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
                        } catch (NumberFormatException e) {
                            // TODO: inform user about wrong input(input wasnt a float)
                        } finally {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                });

                addWeightAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                });
                addWeightAlert.show();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
