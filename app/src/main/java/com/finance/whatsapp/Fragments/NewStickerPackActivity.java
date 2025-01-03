package com.finance.whatsapp.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.finance.whatsapp.Constants.Constants;
import com.finance.whatsapp.R;
import com.finance.whatsapp.WhatsappApi.Sticker;
import com.finance.whatsapp.WhatsappApi.StickerContentProvider;
import com.finance.whatsapp.WhatsappApi.StickerPack;
import com.finance.whatsapp.WhatsappApi.StickerPackDetailsActivity;
import com.finance.whatsapp.utils.FileUtils;
import com.finance.whatsapp.utils.StickerPacksManager;
import com.google.gson.Gson;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewStickerPackActivity extends AppCompatActivity {
    ImageAdapter imageAdapter;
    EditText nameEdit;
    EditText authorEdit;
    LottieAnimationView empty;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sticker_pack);

        // UI references.
        Toolbar tool = findViewById(R.id.toolbar1);
        tool.setTitle("Sticker");
        setSupportActionBar(tool);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nameEdit = findViewById(R.id.sticker_pack_name_edit);
        empty = findViewById(R.id.animation_view);
        authorEdit = findViewById(R.id.sticker_pack_author_edit);
        FrameLayout btnCreate = findViewById(R.id.btn_create_pack);
        btnCreate.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                selectImages();
            }
        });


        GridView gridview = findViewById(R.id.sticker_pack_grid_images_preview);
        imageAdapter = new ImageAdapter(this);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener((parent, v, position, id) -> {
            Toast.makeText(NewStickerPackActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
            imageAdapter.uries.remove(position);
            imageAdapter.notifyDataSetChanged();
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Check initial permissions
        checkAndRequestPermissions();

    }

    private boolean checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
                return false;
            }
        }
        return true;
    }


    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImages();
            } else {
                Toast.makeText(this, "Permission denied. Cannot proceed.", Toast.LENGTH_SHORT).show();
            }
        }
    }*/


    /*private void selectImages() {
        empty.setVisibility(View.GONE);
        FishBun.with(NewStickerPackActivity.this)
                .setImageAdapter(new GlideAdapter())
                .setMaxCount(30)
                .exceptGif(true)
                .setActionBarColor(Color.parseColor("#fead00"), Color.parseColor("#fead00"), false)
                .setMinCount(3).setActionBarTitleColor(Color.parseColor("#ffffff"))
                .startAlbum();
    }*/

    //NEW CODE FOR SELECT IMAGES
    //-------------------------------------------------------------------------------------------------------------------
    private static final int REQUEST_SELECT_IMAGE = 102;

    private void selectImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageAdapter.uries.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    imageAdapter.uries.add(imageUri);
                }
                imageAdapter.notifyDataSetChanged(); // Update the GridView
                ((TextView) findViewById(R.id.stickers_selected_textview))
                        .setText(imageAdapter.uries.size() + " stickers selected");
            }
        }
    }
    //-------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_sticker_pack, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.save_sticker_pack) {
            if (validateValues()) {
                Toast.makeText(this, "You have to fill all empty spaces", Toast.LENGTH_SHORT).show();
            } else {
                saveStickerPack(imageAdapter.uries, nameEdit.getText().toString(), authorEdit.getText().toString());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateValues() {
        return nameEdit.getText().toString().trim().isEmpty() || authorEdit.getText().toString().trim().isEmpty() || imageAdapter.uries.isEmpty();
    }

    private void saveStickerPack(List<Uri> uries, String name, String author) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Wait a moment while we process your stickers..."); // Setting Message
        progressDialog.setTitle("Processing images"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        new Thread(() -> {
            try {

                Intent intent = new Intent(NewStickerPackActivity.this, StickerPackDetailsActivity.class);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);

                String identifier = "." + FileUtils.generateRandomIdentifier();
                StickerPack stickerPack = new StickerPack(identifier, name, author, Objects.requireNonNull(uries.toArray())[0].toString(), "", "", "", "");

                //Save the sticker images locally and get the list of new stickers for pack
                List<Sticker> stickerList = StickerPacksManager.saveStickerPackFilesLocally(stickerPack.identifier, uries, NewStickerPackActivity.this);
                stickerPack.setStickers(stickerList);

                //Generate image tray icon
                String stickerPath = Constants.STICKERS_DIRECTORY_PATH + identifier;
                String trayIconFile = FileUtils.generateRandomIdentifier() + ".png";
                StickerPacksManager.createStickerPackTrayIconFile(uries.get(0), Uri.parse(stickerPath + "/" + trayIconFile), NewStickerPackActivity.this);
                stickerPack.trayImageFile = trayIconFile;

                //Save stickerPack created to write in json
                StickerPacksManager.stickerPacksContainer.addStickerPack(stickerPack);
                StickerPacksManager.saveStickerPacksToJson(StickerPacksManager.stickerPacksContainer);
                insertStickerPackInContentProvider(stickerPack);

                //Start new activity with stickerpack information
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPack);
                startActivity(intent);
                NewStickerPackActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }).start();
    }

    private void insertStickerPackInContentProvider(StickerPack stickerPack) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("stickerPack", new Gson().toJson(stickerPack));
        getContentResolver().insert(StickerContentProvider.AUTHORITY_URI, contentValues);
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ALBUM_REQUEST_CODE) {
            ArrayList<Uri> uries;
            if (resultCode == RESULT_OK) {
                uries = data.getParcelableArrayListExtra(Define.INTENT_PATH);
                if (uries.size() > 0) {
                    imageAdapter.uries = uries;
                    imageAdapter.notifyDataSetChanged();
                    ((TextView) findViewById(R.id.stickers_selected_textview)).setText(uries.size() + " stickers selected");
                }
            }
        }
    }*/

    class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return uries.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            SimpleDraweeView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new SimpleDraweeView(mContext);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (SimpleDraweeView) convertView;
            }

            imageView.setImageURI(uries.get(position));
            return imageView;
        }

        // references to our images
        ArrayList<Uri> uries = new ArrayList<>();
    }
}

