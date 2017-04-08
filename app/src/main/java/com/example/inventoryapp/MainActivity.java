package com.example.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.example.inventoryapp.Data.ItemContract.ItemEntry;

import com.example.inventoryapp.Data.ItemDbHelper;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ItemDbHelper itemDbHelper;
    private static final int LOADER_ID = 0;
    ItemCursorAdapter itemCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemDbHelper = new ItemDbHelper(getApplicationContext());

        ListView listView = (ListView) findViewById(R.id.list);

        itemCursorAdapter = new ItemCursorAdapter(this, null);

        listView.setAdapter(itemCursorAdapter);

        listView.setEmptyView(findViewById(R.id.empty_view));

        Button ig = (Button) findViewById(R.id.add);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                Intent intent;
                switch (view.getId()){
                    case R.id.make_sale:
                        ContentValues values= new ContentValues();
                        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                        int supply_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
                        int newValue = cursor.getInt(supply_id)-1;
                        values.put(ItemEntry.COLUMN_ITEM_QUANTITY,newValue);
                        getContentResolver().update(currentItemUri, values,null, null);
                        Log.d("sale","made");
                    case R.id.order_more:
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_EMAIL, "example@gmail.com");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Please order more");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }

                    case R.id.delete_curr_item:
                        getContentResolver().delete(currentItemUri,null,null);

                    default:
                        intent = new Intent(MainActivity.this, EditorActivity.class);
                        currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                        intent.setData(currentItemUri);
                        startActivity(intent);
                }
            }
        });

        ig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertPet() {

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Sample Product");
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, "7");
        values.put(ItemEntry.COLUMN_ITEM_PRICE, 4.02);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, "Supplier");

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.no_img);

        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String image= Base64.encodeToString(b, Base64.DEFAULT);
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, image);
        getContentResolver().insert(ItemEntry.CONTENT_URI, values);

    }

    private void deletePets() {
        getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            case R.id.action_delete_all_entries:
                deletePets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_IMAGE,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE
        };

        return  new CursorLoader(this,
                ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        itemCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemCursorAdapter.swapCursor(null);
    }
}
