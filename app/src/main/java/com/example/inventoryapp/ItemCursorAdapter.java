package com.example.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ItemContract.ItemEntry;

/**
 * Created by giddu on 4/3/17.
 */

public class ItemCursorAdapter extends CursorAdapter {


    public ItemCursorAdapter (Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        ImageView img= (ImageView) view.findViewById(R.id.img);
        final TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView supply = (TextView) view.findViewById(R.id.supply);

        final int name_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        final int price_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        final int img_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
        final int supply_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        final int supplier_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
        final int id_id = cursor.getColumnIndex(ItemEntry._ID);

        name.setText(cursor.getString(name_id));
        price.setText("$"+cursor.getString(price_id));
        supply.setText(cursor.getString(supply_id)+" left");

        final String bitmapString = cursor.getString(img_id);
        Bitmap bitmap = null;
        try {
            byte [] encodeByte= Base64.decode(bitmapString,Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.getMessage();
        }

        if(bitmap != null){
            img.setImageBitmap(bitmap);
        } else {
            img.setImageResource(R.drawable.no_img);
        }

        Button saleButton = (Button) view.findViewById(R.id.make_sale_item);
        final ContentValues values= new ContentValues();

        final long idCurrent = getItemId(cursor.getPosition());

        final int newValue = cursor.getInt(supply_id);
        final double priceFinal = cursor.getDouble(price_id);
        final String supplierFinal = cursor.getString(supplier_id);
        final String nameFinal = cursor.getString(name_id);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, idCurrent);
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY,newValue);
                values.put(ItemEntry.COLUMN_ITEM_PRICE,priceFinal);
                values.put(ItemEntry.COLUMN_ITEM_SUPPLIER,supplierFinal);
                values.put(ItemEntry.COLUMN_ITEM_NAME,nameFinal);
                values.put(ItemEntry.COLUMN_ITEM_IMAGE,bitmapString);
                context.getContentResolver().update(currentItemUri, values,null, null);
            }
      });
    }
}
