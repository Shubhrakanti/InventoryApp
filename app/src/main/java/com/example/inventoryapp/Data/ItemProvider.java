package com.example.inventoryapp.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.inventoryapp.Data.ItemContract.ItemEntry;


/**
 * Created by giddu on 4/2/17.
 */

public class ItemProvider extends ContentProvider {

    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private ItemDbHelper itemDbHelper;
    private static final int ITEM = 100;
    private static final int ITEM_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEM);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {

        itemDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = itemDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getApplicationContext().getContentResolver(), uri);
        return cursor;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        boolean proceed = checkValues(values);

        if(proceed){
            SQLiteDatabase db = itemDbHelper.getWritableDatabase();
            long id = db.insert(ItemEntry.TABLE_NAME, null, values);
            if (id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = itemDbHelper.getWritableDatabase();
        int rowsUpdated;
        getContext().getContentResolver().notifyChange(uri, null);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                rowsUpdated = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsUpdated!=0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsUpdated = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsUpdated!=0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        boolean proceed = checkValues(values);

        if(proceed){
            if (values.size() == 0) {
                return 0;
            }


            SQLiteDatabase db= itemDbHelper.getWritableDatabase();

            int rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }

        return 0;


    }


    private boolean checkValues (ContentValues contentValues){

        Integer amount = contentValues.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        if (amount == null || amount <= 0) {
            Toast.makeText(getContext(), "You Must Enter a quantity",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


        Double price = contentValues.getAsDouble(ItemEntry.COLUMN_ITEM_PRICE);
        if (price == null || price <= 0) {
            Toast.makeText(getContext(), "You Must Enter a price",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


        String name = contentValues.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if (name.equals("")) {
            Toast.makeText(getContext(), "You Must Enter a name",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String supplier = contentValues.getAsString(ItemEntry.COLUMN_ITEM_SUPPLIER);
        if (supplier.equals("")) {
            Toast.makeText(getContext(), "You Must Enter a supplier",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        String imgBitmap = contentValues.getAsString(ItemEntry.COLUMN_ITEM_IMAGE);
        if (imgBitmap == null){
            Toast.makeText(getContext(), "You Must Enter an image",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }
}
