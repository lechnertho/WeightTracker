package de.lechner.weighttracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Author: Thomas Lechner on 11.02.2015.
 *
 * This Class draws values in a 2-D coordinate system. The values are stored in a list as
 * DatabaseEntrys . The list is provided through the constructor or values can be added to the list
 * through the addValue() function
 * function.
 */
public class TrendView extends View {

    public static final int LASTWEEK = 1;
    public static final int LASTMONTH = 2;
    public static final int LASTYEAR = 3;

    private static Paint yAxisPaint;
    private static Paint gridPaint;
    private static Paint dotPaint;
    private static Paint yAxisLabelPaint;
    private static Paint xAxisLabelPaint;
    private static Paint backgroundPaint;
    //radius of the dots
    //TODO: dot radius dependend on Screensize
    private static float dotRadius = 5;


    static {

        yAxisPaint = new Paint();
        yAxisPaint.setColor(Color.BLACK);
        yAxisPaint.setStrokeWidth(5);

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

    }

    private WeightsDataSource datasource;
    private int displayMode = LASTMONTH;

    public TrendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        yAxisLabelPaint.setTextSize(18 * getResources().getDisplayMetrics().density);
        xAxisLabelPaint.setTextSize(18 * getResources().getDisplayMetrics().density);
    }

    /**
     * Sets the
     * @param datasource
     */
    public void setDataSource(WeightsDataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * There are several modes available: last week, last month, last year. Use the constants from
     * this class.
     *
     * @param displayMode
     */
    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
        this.invalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float maxValue = 0;
        float minValue = 0;

        String xAxisLabel = "";
        String xAxisLabelMaxValue= "0";
        String xAxisLabelMinValue= "0";

        //calculate which values to display
        List<DatabaseEntry> valuesToDisplay;
        switch(this.displayMode) {
            case 1:
                //display values from current week
                valuesToDisplay = new LinkedList<DatabaseEntry>();
                break;
            case 2:
                //display values from current month
                valuesToDisplay = new LinkedList<DatabaseEntry>();
                //indicate whether or not minValue has been initialized
                boolean minValueInitialized = false;
                for(int i = 1; i <= Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    DatabaseEntry entry = datasource.getEntry(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, i);

                    //if entry exist add it to list, else add dummy entry to list
                    if(entry != null) {
                        valuesToDisplay.add(entry);

                        //initialize minValue
                        if(!minValueInitialized) {
                            minValue = entry.getValue();
                            minValueInitialized = true;
                        }

                        //find max value
                        if(entry.getValue() > maxValue) {
                            maxValue = entry.getValue();
                        }
                        //find min value
                        if(entry.getValue() < minValue) {
                            minValue = entry.getValue();

                        }
                    } else {
                        valuesToDisplay.add(new DatabaseEntry(-1, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, i));
                    }
                }
                //calculate content of labels
                //x-Axis labels
                xAxisLabel = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                xAxisLabelMaxValue= String.valueOf(Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
                xAxisLabelMinValue= "1";
                break;
            case 3:
                //display values from current year
                valuesToDisplay = new LinkedList<DatabaseEntry>();
                break;
            default:
                valuesToDisplay = new LinkedList<DatabaseEntry>();
                break;
        }

        //calculate content of labels
        //y-Axis labels
        String yAxisLabelMaxValue = String.valueOf(((int) Math.ceil(maxValue) + 5) / 5 * 5);
        String yAxisLabelMinValue = String.valueOf(((int) Math.floor(minValue) - 1) / 5 * 5);


        //calculate bounds of labels
        //x-Axis labels
        Rect xAxisLabelBounds = new Rect();
        xAxisLabelPaint.getTextBounds(xAxisLabel, 0, xAxisLabel.length(), xAxisLabelBounds);

        Rect xAxisLabelMaxValueBounds = new Rect();
        xAxisLabelPaint.getTextBounds(xAxisLabelMaxValue, 0, xAxisLabelMaxValue.length(), xAxisLabelMaxValueBounds);

        Rect xAxisLabelMinValueBounds = new Rect();
        xAxisLabelPaint.getTextBounds(xAxisLabelMinValue, 0, xAxisLabelMinValue.length(), xAxisLabelMinValueBounds);

        float xAxisLabelMaxHeight = Math.max(xAxisLabelBounds.height(), Math.max(xAxisLabelMaxValueBounds.height(), xAxisLabelMinValueBounds.height()));

        //y-Axis labels
        Rect yAxisLabelMaxValueBounds = new Rect();
        yAxisLabelPaint.getTextBounds(yAxisLabelMaxValue, 0, yAxisLabelMaxValue.length(), yAxisLabelMaxValueBounds);

        Rect yAxisLabelMinValueBounds = new Rect();
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
        yAxisLabelPaint.getTextBounds("0123456789", 0 , 10, yAxisLabelDummyBounds);
        int placeForAdditionalLabels = (int) Math.floor((canvasHeight - xAxisLabelMaxHeight - yAxisLabelMinValueBounds.height() - yAxisLabelMaxValueBounds.height()) / yAxisLabelDummyBounds.height());
        int deltaAdditionalLabels = 0;
        for(int i = 5; i <= 500; i = i + 5) {
            int neededAdditionalLabels = ((Integer.valueOf(yAxisLabelMaxValue) - Integer.valueOf(yAxisLabelMinValue)) / i) - 1;
            if(neededAdditionalLabels <= placeForAdditionalLabels) {
                deltaAdditionalLabels = i;
                break;
            }
        }
        System.out.println("HERE: " + deltaAdditionalLabels);
        //grid x-lines
        Path path = new Path();
        path.moveTo(0 + yAxisLabelMaxLength, yAxisLabelMaxValueBounds.height() / 2);
        path.lineTo(canvasWidth, yAxisLabelMaxValueBounds.height() / 2);
        canvas.drawPath(path, gridPaint);

        path.moveTo(0 + yAxisLabelMaxLength, canvasHeight - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2)));
        path.lineTo(canvasWidth, canvasHeight - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2)));
        canvas.drawPath(path, gridPaint);

        //values
        float coordinatedSystemWidth = canvasWidth - yAxisLabelMaxLength;
        float horizontalSpaceBetweenValues = coordinatedSystemWidth / valuesToDisplay.size();

        int yAxisSpan = Integer.valueOf(yAxisLabelMaxValue) - Integer.valueOf(yAxisLabelMinValue);
        float verticalPixelsPerValue =  (canvasHeight - (yAxisLabelMaxValueBounds.height() / 2) - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2))) / yAxisSpan;

        for(int i = 0; i < valuesToDisplay.size(); i++) {
            if(valuesToDisplay.get(i).getValue() != -1) {
                canvas.drawCircle(yAxisLabelMaxLength + (horizontalSpaceBetweenValues / 2) + (i * horizontalSpaceBetweenValues),
                        canvasHeight - (xAxisLabelMaxHeight + (yAxisLabelMinValueBounds.height() / 2)) - ((valuesToDisplay.get(i).getValue() - Integer.valueOf(yAxisLabelMinValue)) * verticalPixelsPerValue),
                        dotRadius, dotPaint);
            }
        }


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}