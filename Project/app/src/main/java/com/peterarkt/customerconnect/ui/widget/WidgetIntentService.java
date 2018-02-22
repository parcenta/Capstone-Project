package com.peterarkt.customerconnect.ui.widget;


import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.peterarkt.customerconnect.R;

import timber.log.Timber;

public class WidgetIntentService extends IntentService {

    private static final String ACTION_WIDGET_SHOW_TODAYS_VISITS  = "ACTION_WIDGET_SHOW_TODAYS_VISITS";

    public WidgetIntentService(){
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent!=null){
            final String action = intent.getAction();

            if(action == null || action.isEmpty()) return;

            if(action.equals(ACTION_WIDGET_SHOW_TODAYS_VISITS))
                handleShowTodaysVisits();
        }
    }

    public static void startActionShowTodaysVisits(Context context){
        Intent intent = new Intent(context,WidgetIntentService.class);
        intent.setAction(ACTION_WIDGET_SHOW_TODAYS_VISITS);
        context.startService(intent);
    }

    private void handleShowTodaysVisits(){
        Timber.i("Widget is going to be refreshed from IntentService...");

        // Refresh the Widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetsIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetTodaysVisitsProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetsIds, R.id.todays_visits_widget_grid_view);
        WidgetTodaysVisitsProvider.updateCustomerConnectWidgets(this,appWidgetManager,appWidgetsIds);
    }

}
