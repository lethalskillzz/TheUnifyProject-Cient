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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.android.volley.AuthFailureError;
import net.android.volley.DefaultRetryPolicy;
import net.android.volley.NetworkError;
import net.android.volley.NoConnectionError;
import net.android.volley.ParseError;
import net.android.volley.Request;
import net.android.volley.Response;
import net.android.volley.ServerError;
import net.android.volley.TimeoutError;
import net.android.volley.VolleyError;
import net.android.volley.toolbox.ImageLoader;
import net.android.volley.toolbox.StringRequest;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.util.Logout;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

public class EditShopActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = EditShopActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private PrefManager pref;
    private ConnectionDetector cd;
    private Logout mLogout;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    // number of images to select
    private static final int PICK_IMAGE = 1;
    private Uri outputFileUri;


    private FeedImageView image;
    private EditText input_title, input_price, input_description;
    private TextInputLayout inputLayoutTitle, inputLayoutPrice, inputLayoutDescription;
    private Spinner spinner_category, spinner_condition;
    private Button btn_submit;
    private CheckBox privacy;
    private ProgressBar progressBar;

    private String shopId;
    private String imgPath;
    private boolean isReady;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_shop_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(this);
        //profile = pref.getUserDetails();

        Intent intent = getIntent();
        shopId = intent.getStringExtra("shopId");

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        image = (FeedImageView) findViewById(R.id.edit_shop_image);
        input_title = (EditText) findViewById(R.id.edit_shop_title);
        input_price = (EditText) findViewById(R.id.edit_shop_price);
        input_description = (EditText) findViewById(R.id.edit_shop_description);
        inputLayoutTitle = (TextInputLayout) findViewById(R.id.edit_shop_title_layout);
        inputLayoutPrice = (TextInputLayout) findViewById(R.id.edit_shop_price_layout);
        inputLayoutDescription = (TextInputLayout) findViewById(R.id.edit_shop_description_layout);
        spinner_category = (Spinner) findViewById(R.id.edit_shop_category);
        spinner_condition = (Spinner) findViewById(R.id.edit_shop_condition);
        btn_submit = (Button) findViewById(R.id.edit_shop_button);
        privacy = (CheckBox) findViewById(R.id.edit_shop_privacy);
        progressBar = (ProgressBar) findViewById(R.id.edit_shop_progressBar);

        image.setDefaultImageResId(R.drawable.ic_image);
        image.setImageUrl("http://testing.null", imageLoader);

        image.setOnClickListener(this);
        image.setOnLongClickListener(this);
        btn_submit.setOnClickListener(this);

        requestFocus(input_title);

        if (cd.isConnectingToInternet()) {

            progressBar.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.GONE);
            displayShopping(shopId);
        } else
            showSnackBar(0,getString(R.string.err_no_internet));


        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {

                progressBar.setVisibility(View.GONE);
                btn_submit.setVisibility(View.VISIBLE);

                switch (msg.what) {

                    case AppConfig.httpHandlerEditShop: {

                        if (((String) msg.obj).trim().equals("success"))
                            finish();
                        else if (((String) msg.obj).trim().equals("fail"))
                            showSnackBar(1, "Error while editing shop!");
                        else
                            showSnackBar(1, getString(R.string.err_network_timeout));
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        if (isReady) {
            switch (view.getId()) {

                case R.id.edit_shop_image:
                    openImageIntent();
                    break;

                case R.id.edit_shop_button:
                    submit();
                    break;
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (isReady) {
            switch (view.getId()) {

                case R.id.edit_shop_image: {

                    image.setImageResource(R.drawable.ic_add_img);
                    imgPath = null;
                }
                return true;
            }
        }
            return false;

    }

    private void setUpShop(String img, String category, String condition, String price, String title, String description) {

        if(img.length()>0) {
            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();
            this.image.setImageUrl(img+AppConfig.AUTO_REF_HACK(), imageLoader);
            this.image.setResponseObserver(new FeedImageView.ResponseObserver() {
                @Override
                public void onError() {
                }

                @Override
                public void onSuccess() {
                }
            });
        } else
            image.setImageResource(R.drawable.ic_add_img);

        String[] categoryTitles = getApplicationContext().getResources().getStringArray(R.array.shopping_array);
        for (int i = 0; i < categoryTitles.length; i++) {
            if(categoryTitles[i].equals(category)) {
                this.spinner_category.setSelection(i);
            }
        }

        String[] conditionTitles = getApplicationContext().getResources().getStringArray(R.array.condition_array);
        for (int i = 0; i < conditionTitles.length; i++) {
            if(conditionTitles[i].equals(condition)) {
                this.spinner_condition.setSelection(i);
            }
        }

        this.input_price.setText(price);
        this.input_title.setText(title);
        this.input_description.setText(description);


        isReady = true;
    }



    private void displayShopping(final String shopId) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DISPLAY_SHOP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response);

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object

                    //Checking if current session is active
                    if(!responseObj.getBoolean("isSession"))
                        mLogout.logout();

                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error
                    if (!error) {

                        // parsing the feed data
                        JSONObject dataObj = responseObj.getJSONObject("data");
                        JSONArray feedArray = dataObj.getJSONArray("shopping");

                        JSONObject feedObj = (JSONObject) feedArray.get(0);
                        int id = feedObj.getInt("id");
                        String image = feedObj.getString("image");
                        String category = feedObj.getString("category");
                        String condition = feedObj.getString("condition");
                        String price = feedObj.getString("price");
                        String title = feedObj.getString("title");
                        String description = feedObj.getString("description");

                        setUpShop(image, category, condition, price, title, description);

                    } else {

                    }

                    progressBar.setVisibility(View.GONE);
                    btn_submit.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    progressBar.setVisibility(View.GONE);
                    btn_submit.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    showSnackBar(0, getString(R.string.err_network_timeout));
                } else if (error instanceof AuthFailureError) {
                    //TODO
                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO
                }

                progressBar.setVisibility(View.GONE);
                btn_submit.setVisibility(View.VISIBLE);
            }
        }) {

            /**
             * Passing feed parameters to our server
             *
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", pref.getUsername());
                params.put("sessionId", pref.getSessionId());
                params.put("shopId", shopId);

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        //Setting request policy to max timeout
        strReq.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Adding request to volley request queue
        AppController.getInstance().addToRequestQueue(strReq);
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
                intent.putExtra("intent_type", AppConfig.httpIntentEditShop);
                intent.putExtra("shopId", shopId);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                intent.putExtra("price", price);
                intent.putExtra("category", category);
                intent.putExtra("condition", condition);
                intent.putExtra("img_path", imgPath);
                startService(intent);
            } else showSnackBar(1, getString(R.string.err_no_internet));
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

            Toast.makeText(getApplicationContext(), "Press & hold to remove image", Toast.LENGTH_LONG).show();
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
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.edit_shop_CoordinatorLayout);

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(id == 0){
                    if (cd.isConnectingToInternet()) {
                        displayShopping(shopId);
                    } else {
                        showSnackBar(0, getString(R.string.err_no_internet));
                    }
                }else {

                }
            }
        });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();

    }


}
