package com.glvz.egais.ui.doc.photo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.glvz.egais.BuildConfig;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.photo.PhotoRec;
import com.glvz.egais.service.photo.PhotoArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.MessageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class ActPhotoList extends ActBaseDocList {
    static final int CAMERA_PERMISSION_CODE = 1;
    static final int CAMERA_REQUEST = 1;
    private Uri imageToUploadUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_photolist);
        adapter = new PhotoArrayAdapter(this, R.layout.rec_photo, list);
        super.setResources();
        Button btnNewPhoto = (Button) findViewById(R.id.btnNewPhoto);
        btnNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryCreateNewPhoto();
            }
        });
        registerForContextMenu(lv);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.listView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_photorec, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.delete:
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите удаление строки фотографии",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().deletePhoto((PhotoRec) list.get(info.position));
                                updateList();
                                lv.smoothScrollToPosition(0);
                            }
                        });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void tryCreateNewPhoto() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                MessageUtils.showToastMessage("Необходимо дать разрешение!");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            captureCameraImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                captureCameraImage();
            }
        }
    }

    private void captureCameraImage() {
        imageToUploadUri =getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToUploadUri);
        startActivityForResult(photoIntent, CAMERA_REQUEST);

/*        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(DaoMem.getDaoMem().getBasePath() + "/POST_IMAGE.jpg");
        //final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/GLVZpicFolder/";
       // File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/GLVZpicFolder/");
//        File newdir = new File(dir);
//        newdir.mkdirs();
//        File newfile = new File(dir+"POST_IMAGE.jpg");
        imageToUploadUri = FileProvider.getUriForFile(
                this,
                this.getApplicationContext().getPackageName() + ".provider",
                f);

        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToUploadUri);
        //imageToUploadUri = Uri.fromFile(newfile);
        startActivityForResult(chooserIntent, CAMERA_REQUEST);*/
    }

    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return byteArray;
    }

    private Uri getOutputMediaFileUri(int mediaTypeImage)
    {
        //check for external storage
        if(isExternalStorageAvaiable())
        {
            File mediaStorageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            String fileName = "";
            String fileType = "";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

            fileName = "IMG_"+timeStamp;
            fileType = ".jpg";

            File mediaFile;
            try
            {
                mediaFile = File.createTempFile(fileName,fileType,mediaStorageDir);
                Log.i("st","File: "+Uri.fromFile(mediaFile));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.i("St","Error creating file: " + mediaStorageDir.getAbsolutePath() +fileName +fileType);
                return null;
            }
            return Uri.fromFile(mediaFile);
         //   return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", mediaFile);
        }
        //something went wrong
        return null;
    }

    private boolean isExternalStorageAvaiable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                getContentResolver().notifyChange(imageToUploadUri, null);
                try {
                    byte[] byteArray = DaoMem.getDaoMem().readBytes(new File(imageToUploadUri.getPath()));

                    Bitmap photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    Bitmap photoMini = getResizedBitmap(photo, 100);
                    //Bitmap photoMini = (Bitmap) data.getExtras().get("data");
                    byte[] byteArrayMini = getBytes(photoMini);

                    PhotoRec rec = DaoMem.getDaoMem().addPhotoRec(DaoMem.getDaoMem().getShopId(), DaoMem.getDaoMem().getShopInName(), byteArray, byteArrayMini);
                    updateList();
                    syncDoc();
                    // scroll to it
                    lv.smoothScrollToPosition(0);

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void syncDoc() {
        //- выполняется проверка подключенного WiFi, при наличии JPEG-файл выгружается по FTP с записью в журнал.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DaoMem.getDaoMem().syncWiFiFtpShared();
                    DaoMem.getDaoMem().initDictionary();
                    DaoMem.getDaoMem().syncWiFiFtpShopDocs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    protected void updateList() {
        // Прочитать список
        list.clear();
        for (PhotoRec rec : DaoMem.getDaoMem().getPhotoRecListOrdered()) {
            list.add(rec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {

        File file = new File(DaoMem.getDaoMem().getPhotoFileName((PhotoRec)req));
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        //Uri data = Uri.parse("file://" + file.getAbsolutePath());
        Uri data = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(data, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

}
