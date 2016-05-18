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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.service.HttpService;

public class PostShopActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    //private static final String TAG = PostShopActivity.class.getSimpleName();
    public static Handler mUiHandler;

    // number of images to select
    private static final int PICK_IMAGE = 1;
    private Uri outputFileUri;

    private String imgPath;

    private ImageView image;
    private EditText input_title, input_price, input_description;
    private TextInputLayout inputLayoutTitle, inputLayoutPrice, inputLayoutDescription;
    private Spinner spinner_category, spinner_condition;
    private Button btn_submit;
    private CheckBox privacy;
    private ProgressBar progressBar;

    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_shop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.post_shopping_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        image = (ImageView) findViewById(R.id.post_shop_image);
        input_title = (EditText) findViewById(R.id.input_shop_title);
        input_price = (EditText) findViewById(R.id.input_shop_price);
        input_description = (EditText) findViewById(R.id.input_shop_description);
        inputLayoutTitle = (TextInputLayout) findViewById(R.id.input_shop_title_layout);
        inputLayoutPrice = (TextInputLayout) findViewById(R.id.input_shop_price_layout);
        inputLayoutDescription = (TextInputLayout) findViewById(R.id.input_shop_description_layout);
        spinner_category = (Spinner) findViewById(R.id.input_shop_category);
        spinner_condition = (Spinner) findViewById(R.id.input_shop_condition);
        btn_submit = (Button) findViewById(R.id.post_shop_button);
        privacy = (CheckBox) findViewById(R.id.post_shop_privacy);
        progressBar = (ProgressBar) findViewById(R.id.post_shop_progressBar);

        image.setOnClickListener(this);
        image.setOnLongClickListener(this);
        btn_submit.setOnClickListener(this);

        requestFocus(input_title);

        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case AppConfig.httpHandlerPostShop: {

                        progressBar.setVisibility(View.GONE);
                        btn_submit.setVisibility(View.VISIBLE);

                        if (((String) msg.obj).trim().equals("success"))
                            finish();
                        else if (((String) msg.obj).trim().equals("fail"))
                            showSnackBar("Error while posting shop!");
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

            case R.id.post_shop_image:
                openImageIntent();
                break;

            case R.id.post_shop_button:
                submit();
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {

            case R.id.post_shop_image: {

                image.setImageResource(R.drawable.ic_add_img);
                imgPath = null;
            }
            return true;
        }
        return false;
    }


    private void submit() {

        if (!validateInput(input_title, inputLayoutTitle))
        {
            return;
        }

        if (!validateInput(input_description, inputLayoutDescription))
        {
            return;
        }

        if (!validateInput(input_price, inputLayoutPrice))
        {
            return;
        }

        if(privacy.isChecked()) {

            String title = input_title.getText().toString().trim();
            String description = input_description.getText().toString().trim();
            String price = input_price.getText().toString().trim();
            String category = spinner_category.getSelectedItem().toString().trim();
            String condition = spinner_condition.getSelectedItem().toString().trim();


            if (cd.isConnectingToInternet()) {

                progressBar.setVisibility(View.VISIBLE);
                btn_submit.setVisibility(View.GONE);

                Intent intent = new Intent(this, HttpService.class);
                intent.putExtra("intent_type", AppConfig.httpIntentPostShop);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                intent.putExtra("price", price);
                intent.putExtra("category", category);
                intent.putExtra("condition", condition);
                intent.putExtra("img_path", imgPath);
                startService(intent);
            } else showSnackBar(getString(R.string.err_no_internet));
        }
    }


    private boolean validateInput(EditText EdTxt, TextInputLayout inputLayout) {
        if (EdTxt.getText().toString().trim().isEmpty()) {
            inputLayout.setError(getString(R.string.err_msg_input));
            requestFocus(EdTxt);
            return false;
        } else {
            inputLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory()+ AppConfig.DIR_SHOP_IMAGE);
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

            Toast.makeText(getApplicationContext(),"Press & hold to remove image" , Toast.LENGTH_LONG).show();
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

        image.setImageBitmap(bitmap);

        imgPath = filePath;

       /* ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();

        image_file = Base64.encodeToString(data, 0);//encodeBytes(data);*/
    }


    //SnackBar function
    private void showSnackBar(String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.post_shop_CoordinatorLayout);

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
