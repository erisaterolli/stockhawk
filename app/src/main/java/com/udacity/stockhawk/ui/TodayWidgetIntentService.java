package com.udacity.stockhawk.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import static android.R.attr.data;

/**
 * Created by erisa on 18/05/17.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TodayWidgetIntentService extends RemoteViewsService {
    static final int ind_symbol = 1;
    static final int ind_id = 0;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor stockdata = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (stockdata != null)
                    stockdata.close();
                final long id = Binder.clearCallingIdentity();
                stockdata = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(id);

            }

            @Override
            public void onDestroy() {
                if(stockdata != null){
                    stockdata.close();
                    stockdata = null;
                }
            }

            @Override
            public int getCount() {
                return stockdata == null ? 0 : stockdata.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if (i == AdapterView.INVALID_POSITION ||
                        stockdata == null || !stockdata.moveToPosition(i)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.stock_list);
                String symbol = stockdata.getString(ind_symbol);
                views.setTextViewText(R.id.widget_symbol, symbol);

                Float price = stockdata.getFloat(Contract.Quote.POSITION_PRICE);
                views.setTextViewText(R.id.widget_price, Float.toString(price));

                float rawAbsoluteChange = stockdata.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = stockdata.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE)/100;


                if (PrefUtils.getDisplayMode(getApplicationContext())
                        .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.widget_change, Float.toString(rawAbsoluteChange));
                } else {
                    views.setTextViewText(R.id.widget_change, Float.toString(percentageChange));
                }
//
//                final Intent fillIntent = new Intent();
//                fillIntent.putExtra("symbol", symbol);
//                views.setOnClickFillInIntent(R.id.widget, fillIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.stock_list);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                if(stockdata.moveToPosition(i))
                    return stockdata.getLong(ind_id);
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };

    }
}
