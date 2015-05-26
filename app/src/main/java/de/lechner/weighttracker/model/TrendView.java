package de.lechner.weighttracker.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.lechner.weighttracker.R;
import de.lechner.weighttracker.model.DatabaseEntry;
import de.lechner.weighttracker.model.WeightsDataSource;
import de.lechner.weighttracker.model.WeightsDataSourceListener;

/**
 * Author: Thomas Lechner on 11.02.2015.
 * <p/>
 * This Class draws values in a 2-D coordinate system. The values are stored in a list as
 * DatabaseEntrys . The list is provided through the constructor or values can be added to the list
 * through the addValue() function
 * function.
 */
public class TrendView extends View implements WeightsDataSourceListener, SharedPreferences.OnSharedPreferenceChangeListener{

    public static final int LASTWEEK = 1;
    public static final int LASTMONTH = 2;
    public static final int LASTYEAR = 3;

    private static Paint gridPaint;
    private static Paint dotPaint;
    private static Paint yAxisLabelPaint;
    private static Paint xAxisLabelPaint;
    private static Paint backgroundPaint;
    private static Paint messagePaint;
    //radius of the dots
    //TODO: dot radius dependend on Screensize
    private static float dotRadius = 5;


    static {

        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{20, 5}, 0));

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        dotPaint.setColor(Color.RED);

        yAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        yAxisLabelPaint.setColor(Color.BLACK);

        xAxisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        xAxisLabelPaint.setColor(Color.BLACK);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(128, 192, 235, 255));

        messagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        messagePaint.setColor(Color.RED);

    }

    private List<Float> valuesToDisplay;
    private WeightsDataSource datasource;
    private String xAxisLabel = "";
    private String xAxisLabelMinValue = "0";
    private String xAxisLabelMaxValue = "0";
    private String yAxisLabelMinValue = "0";
    private String yAxisLabelMaxValue = "0";
    private Context context;

    private Rect xAxisLabelBounds = new Rect();
    private Rect xAxisLabelMaxValueBounds = new Rect();
    private Rect xAxisLabelMinValueBounds = new Rect();

    private Rect yAxisLabelMaxValueBounds = new Rect();
    private Rect yAxisLabelMinValueBounds = new Rect();

    private Rect messageBounds = new Rect();

    public TrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        yAxisLabelPaint.setTextSize(18 * getResources().getDisplayMetrics().density);
        xAxisLabelPaint.setTextSize(18 * getResources().getDisplayMetrics().density);
        messagePaint.setTextSize(18 * getResources().getDisplayMetrics().density);

        datasource = new WeightsDataSource(context);
        this.datasource.setWeightsDataSourceListener(this);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        loadValues();
    }

    public void addWeight(float weight, int year, int month, int day, int week) {
        datasource.open();
        datasource.createEntry(weight, year, month, day, week);
        datasource.close();
    }

    @Override
    public void dataSourceChanged(DatabaseEntry entry) {
        loadValues();
        postInvalidate();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        System.out.println("Pref Changed");
        if(key.equals("pref_displayMode")) {
            loadValues();
            postInvalidate();
        }
    }

    private void loadValues() {

        datasource.open();

        float minValue;
        float maxValue;
        boolean minValueInitialized;
        List<DatabaseEntry> entries;
        Calendar date;

        switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_displayMode", String.valueOf(LASTMONTH)))) {
            case LASTWEEK:

                minValue = 0;
                maxValue = 0;

                //indicate whether or not minValue has been initialized
                minValueInitialized = false;

                //TODO: change here for other week then current
                entries = datasource.getEntriesWeek(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));

                valuesToDisplay = new LinkedList<>();
                date = Calendar.getInstance();

                //TODO: change here for other week then current
                for (int i = 1; i <= Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_WEEK); i++) {

                    for (DatabaseEntry entry : entries) {
                        //if entry exist add it to list, else add dummy entry to list
                        //TODO: Week starts with sunday
                        date.set(entry.getYear(), entry.getMonth() - 1, entry.getDay());
                        if (date.get(Calendar.DAY_OF_WEEK) == i) {

                            valuesToDisplay.add(entry.getValue());

                            //initialize minValue
                            if (!minValueInitialized) {
                                minValue = entry.getValue();
                                minValueInitialized = true;
                            }

                            //find max value
                            if (entry.getValue() > maxValue) {
                                maxValue = entry.getValue();
                            }
                            //find min value
                            if (entry.getValue() < minValue) {
                                minValue = entry.getValue();

                            }
                        } else {
                            valuesToDisplay.add(-1.0f);
                        }
                    }
                }

                //calculate content of labels
                //y-Axis labels
                yAxisLabelMaxValue = String.valueOf(((int) Math.ceil(maxValue) + 5) / 5 * 5);
                yAxisLabelMinValue = String.valueOf(((int) Math.floor(minValue) - 1) / 5 * 5);

                //calculate content of labels
                //x-Axis labels
                //TODO: change here for other week then current
                xAxisLabel = context.getString(R.string.week) + " " + String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
                xAxisLabelMaxValue = "Sat";
                xAxisLabelMinValue = "Sun";
                break;

            case LASTMONTH:

                minValue = 0;
                maxValue = 0;

                //indicate whether or not minValue has been initialized
                minValueInitialized = false;

                //TODO: change here for other month then current
                entries = datasource.getEntriesMonth(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1);

                valuesToDisplay = new LinkedList<>();

                //TODO: change here for other month then current
                for (int i = 1; i <= Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH); i++) {

                    for (DatabaseEntry entry : entries) {
                        //if entry exist add it to list, else add dummy entry to list
                        if (entry.getDay() == i) {

                            valuesToDisplay.add(entry.getValue());

                            //initialize minValue
                            if (!minValueInitialized) {
                                minValue = entry.getValue();
                                minValueInitialized = true;
                            }

                            //find max value
                            if (entry.getValue() > maxValue) {
                                maxValue = entry.getValue();
                            }
                            //find min value
                            if (entry.getValue() < minValue) {
                                minValue = entry.getValue();

                            }
                        } else {
                            valuesToDisplay.add(-1.0f);
                        }
                    }
                }

                //calculate content of labels
                //y-Axis labels
                yAxisLabelMaxValue = String.valueOf(((int) Math.ceil(maxValue) + 5) / 5 * 5);
                yAxisLabelMinValue = String.valueOf(((int) Math.floor(minValue) - 1) / 5 * 5);

                //calculate content of labels
                //x-Axis labels
                //TODO: change here for other month then current
                xAxisLabel = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                xAxisLabelMaxValue = String.valueOf(Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
                xAxisLabelMinValue = "1";
                break;

            case LASTYEAR:

                minValue = 0;
                maxValue = 0;

                //indicate whether or not minValue has been initialized
                minValueInitialized = false;

                //TODO: change here for other year then current
                entries = datasource.getEntriesYear(Calendar.getInstance().get(Calendar.YEAR));

                valuesToDisplay = new LinkedList<>();
                date = Calendar.getInstance();

                //TODO: change here for other year then current
                for (int i = 1; i <= Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR); i++) {

                    for (DatabaseEntry entry : entries) {
                        //if entry exist add it to list, else add dummy entry to list
                        date.set(entry.getYear(), entry.getMonth() - 1, entry.getDay());
                        if (date.get(Calendar.DAY_OF_YEAR) == i) {

                            valuesToDisplay.add(entry.getValue());

                            //initialize minValue
                            if (!minValueInitialized) {
                                minValue = entry.getValue();
                                minValueInitialized = true;
                            }

                            //find max value
                            if (entry.getValue() > maxValue) {
                                maxValue = entry.getValue();
                            }
                            //find min value
                            if (entry.getValue() < minValue) {
                                minValue = entry.getValue();

                            }
                        } else {
                            valuesToDisplay.add(-1.0f);
                        }
                    }
                }

                //calculate content of labels
                //y-Axis labels
                yAxisLabelMaxValue = String.valueOf(((int) Math.ceil(maxValue) + 5) / 5 * 5);
                yAxisLabelMinValue = String.valueOf(((int) Math.floor(minValue) - 1) / 5 * 5);

                //calculate content of labels
                //x-Axis labels
                //TODO: change here for other year then current
                xAxisLabel = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                xAxisLabelMaxValue = String.valueOf(Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR));
                xAxisLabelMinValue = "1";
                break;
        }
        datasource.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        //calculate bounds of labels
        //x-Axis labels
        xAxisLabelPaint.getTextBounds(xAxisLabel, 0, xAxisLabel.length(), xAxisLabelBounds);
        xAxisLabelPaint.getTextBounds(xAxisLabelMaxValue, 0, xAxisLabelMaxValue.length(), xAxisLabelMaxValueBounds);
        xAxisLabelPaint.getTextBounds(xAxisLabelMinValue, 0, xAxisLabelMinValue.length(), xAxisLabelMinValueBounds);
        float xAxisLabelMaxHeight = Math.max(xAxisLabelBounds.height(), Math.max(xAxisLabelMaxValueBounds.height(), xAxisLabelMinValueBounds.height()));

        //y-Axis labels
        yAxisLabelPaint.getTextBounds(yAxisLabelMaxValue, 0, yAxisLabelMaxValue.length(), yAxisLabelMaxValueBounds);
        yAxisLabelPaint.getTextBounds(yAxisLabelMinValue, 0, yAxisLabelMinValue.length(), yAxisLabelMinValueBounds);
        float yAxisLabelMaxLength = Math.max(yAxisLabelMaxValueBounds.width(), yAxisLabelMinValueBounds.width());

        //draw labels
        //x-Axis
        canvas.drawText(xAxisLabel, (canvasWidth / 2) + (yAxisLabelMaxLength / 2) - (xAxisLabelBounds.width() / 2), canvasHeight - xAxisLabelBounds.bottom, xAxisLabelPaint);
        canvas.drawText(xAxisLabelMaxValue, canvasWidth - xAxisLabelMaxValueBounds.right, canvasHeight - xAxisLabelMaxValueBounds.bottom, xAxisLabelPaint);
        canvas.drawText(xAxisLabelMinValue, yAxisLabelMaxLength - xAxisLabelMinValueBounds.left, canvasHeight - xAxisLabelMinValueBounds.bottom, xAxisLabelPaint);
        //y-Axis
        canvas.drawText(yAxisLabelMaxValue, 0 - yAxisLabelMaxValueBounds.left, 0 - yAxisLabelMaxValueBounds.top, yAxisLabelPaint);
        canvas.drawText(yAxisLabelMinValue, 0 - yAxisLabelMinValueBounds.left, canvasHeight - xAxisLabelMaxHeight - yAxisLabelMinValueBounds.bottom, yAxisLabelPaint);

        //draw background color
        canvas.drawRect(0 + yAxisLabelMaxLength, 0, canvasWidth, canvasHeight - xAxisLabelMaxHeight, backgroundPaint);

        //calculate additional y-Axis labels
        Rect yAxisLabelDummyBounds = new Rect();
        yAxisLabelPaint.getTextBounds("0123456789", 0, 10, yAxisLabelDummyBounds);
        int placeForAdditionalLabels = (int) Math.floor((canvasHeight - xAxisLabelMaxHeight - yAxisLabelMinValueBounds.height() - yAxisLabelMaxValueBounds.height()) / yAxisLabelDummyBounds.height());
        int deltaAdditionalLabels = 0;
        for (int i = 5; i <= 500; i = i + 5) {
            int neededAdditionalLabels = ((Integer.valueOf(yAxisLabelMaxValue) - Integer.valueOf(yAxisLabelMinValue)) / i) - 1;
            if (neededAdditionalLabels <= placeForAdditionalLabels) {
                deltaAdditionalLabels = i;
                break;
            }
        }

        //grid x-lines
        Path path = new Path();
        path.moveTo(0 + yAxisLabelMaxLength, yAxisLabelMaxValueBounds.height() / 2);
        path.lineTo(canvasWidth, yAxisLabelMaxValueBounds.height() / 2);
        canvas.drawPath(path, gridPaint);

        path.moveTo(0 + yAxisLabelMaxLength, canvasHeight - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2)));
        path.lineTo(canvasWidth, canvasHeight - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2)));
        canvas.drawPath(path, gridPaint);

        //values
        boolean nothingToDisplay = true;
        if (valuesToDisplay != null) {
            float coordinatedSystemWidth = canvasWidth - yAxisLabelMaxLength;
            float horizontalSpaceBetweenValues = coordinatedSystemWidth / valuesToDisplay.size();

            int yAxisSpan = Integer.valueOf(yAxisLabelMaxValue) - Integer.valueOf(yAxisLabelMinValue);
            float verticalPixelsPerValue = (canvasHeight - (yAxisLabelMaxValueBounds.height() / 2) - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2))) / yAxisSpan;


            for (int i = 0; i < valuesToDisplay.size(); i++) {
                if (valuesToDisplay.get(i) != -1) {
                    nothingToDisplay = false;
                    canvas.drawCircle(yAxisLabelMaxLength + (horizontalSpaceBetweenValues / 2) + (i * horizontalSpaceBetweenValues),
                            canvasHeight - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2)) - ((valuesToDisplay.get(i) - Integer.valueOf(yAxisLabelMinValue)) * verticalPixelsPerValue),
                            dotRadius, dotPaint);
                }
            }
        }

        messagePaint.getTextBounds(context.getString(R.string.nothing_to_display), 0, context.getString(R.string.nothing_to_display).length(), messageBounds);
        if (nothingToDisplay) {
            canvas.drawText(context.getString(R.string.nothing_to_display), (canvasWidth - messageBounds.width()) / 2, canvasHeight / 2, messagePaint);
        }


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}