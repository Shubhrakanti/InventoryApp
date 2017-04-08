package com.example.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
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
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView img= (ImageView) view.findViewById(R.id.img);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView supply = (TextView) view.findViewById(R.id.supply);

        int name_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int price_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int img_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
        int supply_id = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        name.setText(cursor.getString(name_id));
        price.setText("$"+cursor.getString(price_id));
        supply.setText(cursor.getString(supply_id)+" left");

        String bitmapString = cursor.getString(img_id);
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


    }
}
