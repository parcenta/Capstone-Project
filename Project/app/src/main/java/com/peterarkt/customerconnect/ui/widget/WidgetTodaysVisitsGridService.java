package com.peterarkt.customerconnect.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.VisitContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.ui.utils.DateUtils;

import java.util.Date;

import timber.log.Timber;


public class WidgetTodaysVisitsGridService extends RemoteViewsService {
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetTodaysVisitsGridRemoteViewsFactory(this.getApplicationContext());
    }
}


class WidgetTodaysVisitsGridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private Cursor mCursor;

    WidgetTodaysVisitsGridRemoteViewsFactory(Context context){
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

        Timber.i("Widget Adapter is refreshing...");

        // Closing any previuously created cursor
        if(mCursor !=null && !mCursor.isClosed()) mCursor.close();

        mCursor = CustomerDBUtils.getTodaysVisits(mContext);
    }

    @Override
    public void onDestroy() {
        if(mCursor!=null && !mCursor.isClosed()) mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor!=null ? mCursor.getCount() : 0;
    }

    // Acts like the OnBindViewHolder
    @Override
    public RemoteViews getViewAt(int position) {
        if (mCursor == null || mCursor.getCount() == 0)
            return null;
        if(mCursor.moveToPosition(position)){

            // Get Date
            long visitDateAsLong = mCursor.getLong(mCursor.getColumnIndex(VisitContract.VisitEntry.COLUMN_VISIT_DATETIME));
            Date visitDate = new Date(visitDateAsLong);
            String visitDateAsString = DateUtils.getDateAsMMMddYYYY(visitDate);

            // Get Commentary
            String visitCommentary = mCursor.getString(mCursor.getColumnIndex(VisitContract.VisitEntry.COLUMN_VISIT_COMMENTARY));


            // Set Layout.
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.list_item_customer_visits);
            remoteViews.setTextViewText(R.id.item_visit_commentary,visitCommentary);
            remoteViews.setTextViewText(R.id.item_visit_date,visitDateAsString);

            return remoteViews;
        }
        else
            return null;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

