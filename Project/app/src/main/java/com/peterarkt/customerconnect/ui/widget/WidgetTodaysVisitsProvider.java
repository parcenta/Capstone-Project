package com.peterarkt.customerconnect.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;

/**
 * Created by USUARIO on 20/02/2018.
 */

public class WidgetTodaysVisitsProvider  extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_customer_todays_visits);

        // Detect if there are records for today.
        boolean resultsFound = false;
        Cursor todayVisitsCursor = CustomerDBUtils.getTodaysVisits(context);
        if(todayVisitsCursor!=null){
            resultsFound = todayVisitsCursor.getCount() > 0;
            if(!todayVisitsCursor.isClosed()) todayVisitsCursor.close();
        }

        // Hide/Show values.
        if(resultsFound){
            // Show GridView. Hide "No results found" message.
            views.setViewVisibility(R.id.no_visits_found_text_view, View.GONE);
            views.setViewVisibility(R.id.todays_visits_widget_grid_view, View.VISIBLE);

            // Set the "adapter" for the gridview inside the widget
            Intent intent = new Intent(context, WidgetTodaysVisitsGridService.class);
            views.setRemoteAdapter(R.id.todays_visits_widget_grid_view, intent);
        }else{
            // Hide GridView. Show "No results found" message.
            views.setViewVisibility(R.id.todays_visits_widget_grid_view, View.GONE);
            views.setViewVisibility(R.id.no_visits_found_text_view, View.VISIBLE);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateCustomerConnectWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetIntentService.startActionShowTodaysVisits(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

