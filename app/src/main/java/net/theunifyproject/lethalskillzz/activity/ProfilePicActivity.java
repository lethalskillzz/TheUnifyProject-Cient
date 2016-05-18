package net.theunifyproject.lethalskillzz.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

public class ProfilePicActivity extends AppCompatActivity implements View.OnClickListener {

    public static Handler mUiHandler;
    private PrefManager pref;
    // number of images to select
    private static final int PICK_IMAGE = 1;
    private Uri outputFileUri;
    private String username;
    private String imgPath;

    private CropImageView cropImageView;
    private FeedImageView feedImageView;
    private ImageButton btn_edit, btn_cancel, btn_done;
    private ProgressBar progressBar;

    private ConnectionDetector cd;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);

        pref = new PrefManager(this);
        Intent intent = getIntent();
        username  = intent.getStringExtra("username");

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        cropImageView = (CropImageView) findViewById(R.id.profile_pic_cropImageView);
        feedImageView = (FeedImageView) findViewById(R.id.profile_pic_feedImageView);
        btn_edit = (ImageButton) findViewById(R.id.profile_pic_edit_btn);
        btn_cancel = (ImageButton) findViewById(R.id.profile_pic_cancel_btn);
        btn_done = (ImageButton) findViewById(R.id.profile_pic_done_btn);
        progressBar = (ProgressBar) findViewById(R.id.profile_pic_progressBar);

        btn_edit.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_done.setOnClickListener(this);

        showEdit();
        showImage();


        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case AppConfig.httpHandlerChangeProfilePic: {

                        progressBar.setVisibility(View.GONE);
                        showEdit();
                        showImage();

                        if (((String) msg.obj).trim().equals("success")) {
                            AppController.getInstance().getRequestQueue().getCache().remove(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png");
                            //AppController.getInstance().getRequestQueue().getCache().invalidate(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png", true);
                            finish();
                        }

                        else if (((String) msg.obj).trim().equals("fail"))
                            showSnackBar("Error while posting feed!");
                        else
                            showSnackBar(getString(R.string.err_network_timeout));
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_pic_edit_btn: {
                openImageIntent();
            }
            break;

            case R.id.profile_pic_cancel_btn:
                cancel();
                break;

            case R.id.profile_pic_done_btn: {

                if (cd.isConnectingToInternet()) {
                    changeProfilePic();
                } else
                    showSnackBar(getString(R.string.err_no_internet));
            }
            break;

        }
    }


    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory()+ AppConfig.DIR_PROFILE_IMAGE);
        root.mkdirs();
        final String fileName = System.currentTimeMillis()+".jpg";//ToolbarUtil.getUniqueImageFilename();
        final File sdImageMainDirectory = new File(root, fileName);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities
                (captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName
                    (res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        // Filesystem.
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "SelectSource");
        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            if(requestCode == PICK_IMAGE)
            {
                final boolean isCamera;
                if(data == null)
                {
                    isCamera = true;
                }
                else
                {
                    final String action = data.getAction();
                    if(action == null)
                    {
                        isCamera = false;
                    }
                    else
                    {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                Uri selectedImageUri;
                if(isCamera)
                {
                    selectedImageUri = outputFileUri;
                    decodeFile(selectedImageUri.getPath());
                }
                else
                {
                    selectedImageUri = data == null ? null : data.getData();


                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImageUri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    decodeFile(picturePath);
                }


            }

            //Toast.makeText(getApplicationContext(),"Long press to remove" , Toast.LENGTH_LONG).show();
        }
    }


    /**
     * The method decodes the image file to avoid out of memory issues. Sets the
     * selected image in to the ImageView.
     *
     * @param filePath
     */
    public void decodeFile(String filePath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 512;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp <= REQUIRED_SIZE && height_tmp <= REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);

        btn_edit.setVisibility(View.GONE);
        btn_cancel.setVisibility(View.VISIBLE);
        btn_done.setVisibility(View.VISIBLE);
        feedImageView.setVisibility(View.GONE);
        cropImageView.setVisibility(View.VISIBLE);

        cropImageView.setImageBitmap(bitmap);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setAspectRatio(10, 10);

        imgPath = filePath;

    }

    private void showEdit() {
        if(pref.getUsername().equals(username)) {
            btn_edit.setVisibility(View.VISIBLE);
        }
    }


    private void showImage() {

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        feedImageView.setImageUrl(AppConfig.URL_PROFILE_PIC + username + ".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
        feedImageView.setVisibility(View.VISIBLE);
        feedImageView.setResponseObserver(new FeedImageView.ResponseObserver() {
            @Override
            public void onError() {
                feedImageView.setImageResource(R.drawable.ic_user);
            }

            @Override
            public void onSuccess() {
            }
        });
    }


    private void changeProfilePic() {

        progressBar.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.GONE);
        btn_done.setVisibility(View.GONE);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(imgPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        cropImageView.getCroppedImage().compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);


        Intent intent = new Intent(this, HttpService.class);
        intent.putExtra("intent_type", AppConfig.httpIntentChangeProfilePic);
        intent.putExtra("img_path", imgPath);
        startService(intent);

    }

    private void cancel() {

        btn_edit.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.GONE);
        btn_done.setVisibility(View.GONE);
        feedImageView.setVisibility(View.VISIBLE);
        cropImageView.setVisibility(View.GONE);

    }


    //SnackBar function
    private void showSnackBar(String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.profilePicCoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }

}
