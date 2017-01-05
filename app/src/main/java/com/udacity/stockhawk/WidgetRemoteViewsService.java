package com.udacity.stockhawk;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.data.Contract;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();
    private static final String[] QUOTE_COLUMNS = Contract.Quote.QUOTE_COLUMNS;
    // these indices must match the projection
    static final int INDEX_ID = 0;
    static final int INDEX_SYMBOL = 1;
    static final int INDEX_PRICE= 2;
    static final int INDEX_ABSOLUTE_CHANGE = 3;
    static final int INDEX_PERCENT_CHANGE= 4;
    static final int INDEX_HISTORY = 5;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                Uri stockUri = Contract.Quote.URI;
                data = getContentResolver().query(stockUri,
                        QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);
                int stockId = data.getInt(INDEX_ID);
                String symbol = data.getString(INDEX_SYMBOL);
                String price = data.getString(INDEX_PRICE);
                String absoluteChange = data.getString(INDEX_ABSOLUTE_CHANGE);
                String percentageChange = data.getString(INDEX_PERCENT_CHANGE);
                String history = data.getString(INDEX_HISTORY);

                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_price, price);
                views.setTextViewText(R.id.widget_absolute_change, absoluteChange);
                views.setTextViewText(R.id.widget_percentage_change, percentageChange);
                views.setTextViewText(R.id.widget_history, history);

                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(symbol);
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                try {
                    RemoteViews result = new RemoteViews(getPackageName(), R.layout.widget_list_item);
                    return result;
                }
                catch(Exception exception) {
                    Log.e("RemoteViews", exception.toString());
                    return null;
                }
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
