package com.peterarkt.customerconnect.database.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.peterarkt.customerconnect.database.CustomerConnectDBHelper;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;

import java.util.Timer;

import timber.log.Timber;

/**
 * Created by Andrés on 2/1/18.
 */

public class CustomerConnectProvider extends ContentProvider {

    private static final String CUSTOMER_TABLE_NAME = CustomerContract.CustomerEntry.TABLE_NAME;
    private static final String VISIT_TABLE_NAME    = VisitContract.VisitEntry.TABLE_NAME;

    private static final int CODE_CUSTOMER           = 1000;
    private static final int CODE_VISIT              = 2000;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private CustomerConnectDBHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Addking Recipes to uri matcher
        matcher.addURI(CustomerContract.CONTENT_AUTHORITY, CustomerContract.PATH_CUSTOMERS, CODE_CUSTOMER);

        // Adding Recipe Ingredients to uri matcher.
        matcher.addURI(VisitContract.CONTENT_AUTHORITY, VisitContract.PATH_VISITS, CODE_VISIT);


        return matcher;
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new CustomerConnectDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_CUSTOMER: {
                Timber.d("Query Customer table...");
                cursor = mDbHelper.getReadableDatabase().query(
                        CUSTOMER_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case CODE_VISIT: {
                Timber.d("Query Visit table...");
                cursor = mDbHelper.getReadableDatabase().query(
                        VISIT_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}