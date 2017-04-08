package com.example.inventoryapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inventoryapp.Data.ItemContract.ItemEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ImageView imageEdit;

    private EditText nameEdit;
    private EditText supplierEdit;
    private EditText priceEdit;
    private EditText quantityEdit;

    private Button imageButton;

    private Uri data;

    private boolean mItemHasChanged = false;

    private static final int EXISTING_ITEM_LOADER = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private boolean safeToMove = true;

    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        data = intent.getData();

        if (data == null) {
            setTitle("New Item");

            invalidateOptionsMenu();
        } else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        imageEdit = (ImageView) findViewById(R.id.edit_img);

        nameEdit = (EditText) findViewById(R.id.edit_name);
        supplierEdit = (EditText) findViewById(R.id.edit_supplier);
        priceEdit = (EditText) findViewById(R.id.edit_price);
        quantityEdit = (EditText) findViewById(R.id.edit_quantity);

        imageEdit.setOnTouchListener(mTouchListener);
        priceEdit.setOnTouchListener(mTouchListener);
        supplierEdit.setOnTouchListener(mTouchListener);
        nameEdit.setOnTouchListener(mTouchListener);
        quantityEdit.setOnTouchListener(mTouchListener);

        imageButton = (Button) findViewById(R.id.edit_img_button);

        if(data!= null){
            imageButton.setText("Change Photo");
        } else {
            imageButton.setText("Take Photo");
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });



    }

    private void saveItem() {

        String nameString = nameEdit.getText().toString().trim();
        String priceString = priceEdit.getText().toString();
        String quantityString = quantityEdit.getText().toString();
        String supplierString = supplierEdit.getText().toString().trim();

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }


        double priceDouble = 0.0;
        if( !TextUtils.isEmpty(priceString)){
            priceDouble = Double.parseDouble(priceEdit.getText().toString());
        }

        if (data == null &&
            TextUtils.isEmpty(nameString) &&
            TextUtils.isEmpty(priceString) &&
            TextUtils.isEmpty(supplierString) &&
            TextUtils.isEmpty(quantityString) &&
            imageEdit.getDrawable()!=null) {
            return;
        }


        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, supplierString);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceDouble);

        if(imageEdit.getDrawable()!=null){
            BitmapDrawable imgBitmapDrawable = (BitmapDrawable) imageEdit.getDrawable();
            Bitmap imageBitmap = imgBitmapDrawable.getBitmap();
            ByteArrayOutputStream bios =new  ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG,100, bios);
            byte [] b=bios.toByteArray();
            String img=Base64.encodeToString(b, Base64.DEFAULT);

            values.put(ItemEntry.COLUMN_ITEM_IMAGE, img);

        }

        if (data == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
            if (newUri == null) {
                safeToMove = false;
                Toast.makeText(this, "Failed Insert Items",
                        Toast.LENGTH_SHORT).show();
            } else {
                safeToMove= true;
                Toast.makeText(this, "Items Updated",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(data, values, null, null);
            if (rowsAffected == 0) {
                safeToMove= false;
                Toast.makeText(this, "Failed to Update Items",
                        Toast.LENGTH_SHORT).show();
            } else {
                safeToMove = true;
                Toast.makeText(this, "Items Updated",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageEdit.setImageBitmap(photo);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (data == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                if(safeToMove){
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {

            int nameColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int supplierColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
            int imageColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
            int quantityColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

            String name = data.getString(nameColumnIndex);
            String price = data.getString(priceColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String image = data.getString(imageColumnIndex);
            String quantity = data.getString(quantityColumnIndex);

            nameEdit.setText(name);
            priceEdit.setText(price);
            supplierEdit.setText(supplier);
            quantityEdit.setText(quantity);

            String bitmapString = image;
            Bitmap bitmap = null;
            try {
                byte [] encodeByte= Base64.decode(bitmapString,Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            } catch(Exception e) {
                e.getMessage();
            }

            if(bitmap != null){
                imageEdit.setImageBitmap(bitmap);
            } else {
                imageEdit.setImageResource(R.drawable.no_img);
            }


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEdit.setText("");
        priceEdit.setText("");
        supplierEdit.setText("");
        quantityEdit.setText("");
        imageEdit.setImageBitmap(null);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You're changes are not saved...");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to Delete?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (data != null) {
            int rowsDeleted = getContentResolver().delete(data, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Failed to Delete",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Delete Successful",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

}
