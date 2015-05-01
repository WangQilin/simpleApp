package qilin.caiqiaolinpan.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import qilin.caiqiaolinpan.ImageAdapter;
import qilin.caiqiaolinpan.R;


// todo: onBackpress on this page, app crashes, find a way to store the profile image
public class ChooseAvatarActivity extends Activity {

    private final String TAG = this.getClass().getName();

    private final int[] imageIds = {
            R.drawable.take_a_photo,
            R.drawable.profile_0,
            R.drawable.profile_1,
            R.drawable.profile_2,
            R.drawable.profile_3,
            R.drawable.profile_4,
            R.drawable.profile_5,
            R.drawable.profile_6,
            R.drawable.profile_7,
            R.drawable.profile_8,
            R.drawable.profile_9,
            R.drawable.profile_10,
    };

    // Uri of the photo bitmap
    private Uri avatarUri;

    // avatar file location
    private static final String AVATAR_FILE_LOCATION = "file:///sdcard/avatar.jpg";

    // request codes
    private final static int IMAGE_REQUEST_CODE = 0;
    private final static int CAMERA_REQUEST_CODE = 1;
    private final static int RESULT_REQUEST_CODE = 2;

    // result code
    private final static int SYSTEM_AVATAR = 1;
    private final static int USER_DEFINED_AVATAR = 2;
    private final static int BACK_BUTTON_PRESSED = 3;

    // strings of the alert window
    String[] items = new String[]{"gallery", "camera"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_avatar);

        avatarUri = Uri.parse(AVATAR_FILE_LOCATION);

        ImageAdapter adapter = new ImageAdapter(ChooseAvatarActivity.this, imageIds);
        GridView gv = (GridView) findViewById(R.id.gv);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // if the take_a_photo pic is pressed, allow user to take a photo and set as avatar
                if (position == 0) {
                    Log.i(TAG, "user chose to use a self-defined pic as avatar");
                    showDialog();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("imageId", imageIds[position]);
                    setResult(SYSTEM_AVATAR, intent);
                    Log.i(TAG, "user chose a system pic as avatar");
                    finish();
                }
            }
        });
    }

    // show dialog, allow the user to choose from gallery or camera
    private void showDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.activity_choose_avatar_popup_title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case IMAGE_REQUEST_CODE:
                                Log.i(TAG, "user chose to choose from gallery");
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*");
                                intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
                                break;
                            case CAMERA_REQUEST_CODE:
                                //capture a big bitmap and store it in Uri
                                Log.i(TAG, "user chose to take a photo as avatar");
                                if (avatarUri == null) {
                                    Log.e(TAG, "avatar uri is null");
                                }
                                Intent intentFromCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                // check if memory card is applicable for image storage
                                if (hasSdcard()) {
                                    intentFromCamera.putExtra(MediaStore.EXTRA_OUTPUT, avatarUri);
                                }
                                startActivityForResult(intentFromCamera, CAMERA_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // choose pic from gallery
                case IMAGE_REQUEST_CODE:
                    Log.i(TAG, "got photo from gallery, about to start cropping");
                    startPhotoZoom(data.getData());
                    break;
                // take a pic
                case CAMERA_REQUEST_CODE:
                    if (hasSdcard()) {
                        Log.i(TAG, "got photo from camera, about to start cropping");
                        startPhotoZoom(avatarUri);
                    } else {
                        Toast.makeText(ChooseAvatarActivity.this, "didn't find an SD card for image storage", Toast.LENGTH_SHORT).show();
                    }
                    break;
                // when photo cropping finishes
                case RESULT_REQUEST_CODE:
                    if (data != null) {
                        // save the image cropped
                        Log.i(TAG, "finish cropping, about to get back to AddContactActivity...");
                        data.putExtra("avatarUri", avatarUri.toString());
                        Log.i(TAG, avatarUri.toString());
                        setResult(USER_DEFINED_AVATAR, data);
                        finish();
                    }
                    break;
            }
        }
    }

    // crop picture
    public void startPhotoZoom(Uri uri) {
        Log.i(TAG, "start photo zooming");
        Log.i(TAG, uri.toString());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // set to crop
        intent.putExtra("crop", "true");
        // aspectX aspectY is the ratio of width & height
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY are pic's width and height in dp?
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    private static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(BACK_BUTTON_PRESSED, null);
        finish();
    }
}