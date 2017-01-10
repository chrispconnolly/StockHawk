package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DetailActivity extends Activity {
    public final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String[] QUOTE_COLUMNS = Contract.Quote.QUOTE_COLUMNS;
    static final int INDEX_SYMBOL = 1;
    static final int INDEX_HISTORY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if(savedInstanceState == null) {
            try {
                Uri stockUri = getIntent().getData();
                Cursor data = getContentResolver().query(stockUri,
                        QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                if (data == null || data.getCount() == 0)
                    return;
                data.moveToNext();
                String symbol = data.getString(INDEX_SYMBOL);
                String history = data.getString(INDEX_HISTORY);

                List<Entry> entries = new ArrayList<>();
                String[] historyArray = history.split("\n");

                for (String day : historyArray) {
                    String[] dayArray = day.split(",");
                    float x = Float.parseFloat(dayArray[0]);
                    float y = Float.parseFloat(dayArray[1]);
                    entries.add(new Entry(x, y));
                }
                Collections.sort(entries, new EntryXComparator());
                LineDataSet dataSet = new LineDataSet(entries, getString(R.string.line_series_label));
                dataSet.setCircleColor(Color.CYAN);

                LineData lineData = new LineData(dataSet);
                LineChart chart = (LineChart) this.findViewById(R.id.chart1);

                chart.setData(lineData);

                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new DateValueFormatter());

                chart.invalidate();
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
        }
    }

    public class DateValueFormatter implements IAxisValueFormatter {

        public DateValueFormatter() {}

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Date date = new Date((long) value);
            String formattedDate = new SimpleDateFormat("MM/dd/yy").format(date);
            return formattedDate;
        }
    }

}
