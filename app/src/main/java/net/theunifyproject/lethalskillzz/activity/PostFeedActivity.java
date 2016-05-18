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
import android.support.v7.widget.ListPopupWindow;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import net.theunifyproject.lethalskillzz.adapter.ListPopupAdapter;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.ConnectionDetector;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.ListPopupItem;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.util.Logout;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostFeedActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnLongClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener  {

    private static final String TAG = PostFeedActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private ConnectionDetector cd;
    private PrefManager pref;
    private Logout mLogout;

    // number of images to select
    private static final int PICK_IMAGE = 1;
    private Uri outputFileUri;

    private String imgPath;

    private EmojiconEditText input_post_feed;
    private FeedImageView image_post_feed;
    private ImageButton feed_done_btn, feed_cancel_btn, btn_show_emoji, btn_hide_emoji;
    private ProgressBar progressBar;
    private ListPopupWindow listPopupWindow;
    private List<ListPopupItem> listPopupItems;
    private ListPopupAdapter listPopupAdapter;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    private String intent_type;
    private String feedId;
    private boolean isReady = true;
    private boolean isPopUp = false;
    private final int TAG_MENTION = 1;
    private final int TAG_HASH = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        pref = new PrefManager(this);
        mLogout = new Logout(this);
        // creating connection detector class instance
        cd = new ConnectionDetector(getApplicationContext());

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        input_post_feed = (EmojiconEditText)findViewById(R.id.input_post_feed);
        image_post_feed = (FeedImageView)findViewById(R.id.image_post_feed);
        feed_done_btn = (ImageButton)findViewById(R.id.feed_done_btn);
        feed_cancel_btn = (ImageButton)findViewById(R.id.feed_cancel_btn);
        btn_show_emoji = (ImageButton)findViewById(R.id.feed_show_emoji_btn);
        btn_hide_emoji = (ImageButton)findViewById(R.id.feed_hide_emoji_btn);
        progressBar = (ProgressBar) findViewById(R.id.feed_progressBar);

        input_post_feed.addTextChangedListener(new MyTextWatcher(input_post_feed));
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_img);
//        image_post_feed.setImageBitmap(bmp);
        image_post_feed.setDefaultImageResId(R.drawable.ic_add_img);
        image_post_feed.setImageUrl("http://testing.null", imageLoader);

        listPopupItems  = new ArrayList<ListPopupItem>();
        listPopupAdapter = new ListPopupAdapter(this, R.layout.item_list_popup, listPopupItems);

        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(listPopupAdapter);
        listPopupWindow.setAnchorView(input_post_feed);
        listPopupWindow.setWidth(300);
        listPopupWindow.setHeight(400);
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener(this);


        image_post_feed.setOnClickListener(this);
        image_post_feed.setOnLongClickListener(this);
        feed_done_btn.setOnClickListener(this);
        feed_cancel_btn.setOnClickListener(this);
        btn_show_emoji.setOnClickListener(this);
        btn_hide_emoji.setOnClickListener(this);

        input_post_feed.setUseSystemDefault(false);
        setEmojiconFragment(false);
        requestFocus(input_post_feed);

        Intent intent = getIntent();
        intent_type = intent.getStringExtra("intent_type");

        if(intent_type.equals("edit")) {
            feedId = intent.getStringExtra("feedId");
            isReady = false;
            if (cd.isConnectingToInternet()) {
                displayFeed(feedId);
            } else {
                showSnackBar(0, getString(R.string.err_no_internet));
            }
        }

        // Receive messages from service class
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {

                    case AppConfig.httpHandlerPostFeed: {

                        progressBar.setVisibility(View.GONE);
                        feed_done_btn.setVisibility(View.VISIBLE);

                        if(((String)msg.obj).trim().equals("success"))
                            finish();
                         else if(((String)msg.obj).trim().equals("fail"))
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
        if(isReady)
        switch (view.getId()) {

            case R.id.image_post_feed:
                //selectImage();
                openImageIntent();
                break;

            case R.id.feed_cancel_btn:
                cancel();
                break;

            case R.id.feed_done_btn:
                done();
                break;

            case R.id.feed_show_emoji_btn:
                showEmoji();
                break;

            case R.id.feed_hide_emoji_btn:
                hideEmoji();
                break;

        }
    }

    @Override
    public boolean onLongClick(View view) {
        if(isReady)
        switch (view.getId()) {

            case R.id.image_post_feed: {

                image_post_feed.setImageResource(R.drawable.ic_add_img);
                imgPath = null;
            }
            return true;
        }
        return false;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {

        isPopUp = true;

        Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
        String atMentionScheme = "mention://";

        Pattern HashPattern = Pattern.compile("#([A-Za-z0-9_]+)");
        String HashScheme = "hash://";

        Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
            //skip the first character to filter out '@'
            public String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };

        ListPopupItem item = listPopupItems.get(position);
        String copy = input_post_feed.getText().toString();

        if(item.getType() == TAG_MENTION) {
            copy = copy.substring(0, copy.lastIndexOf("@"));
            input_post_feed.setText(copy + " @" + item.getSubTitle());
            input_post_feed.post(new Runnable() {
                @Override
                public void run() {
                    input_post_feed.setSelection(input_post_feed.getText().length());
                }
            });
        } else {
            copy = copy.substring(0, copy.lastIndexOf("#"));
            input_post_feed.setText(copy + " " + item.getTitle());
            input_post_feed.post(new Runnable() {
                @Override
                public void run() {
                    input_post_feed.setSelection(input_post_feed.getText().length());
                }
            });
        }

        listPopupWindow.dismiss();
        input_post_feed.moveCursorToVisibleOffset();
        //Linkify.addLinks(input_post_feed, Linkify.ALL);
        Linkify.addLinks(input_post_feed, atMentionPattern, atMentionScheme, null, transformFilter);
        Linkify.addLinks(input_post_feed, HashPattern, HashScheme, null, transformFilter);

    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(input_post_feed, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(input_post_feed);
    }


    private void showEmoji() {
        FrameLayout emojicons = (FrameLayout) findViewById(R.id.emojicons);
        emojicons.setVisibility(View.VISIBLE);
        btn_show_emoji.setVisibility(View.GONE);
        btn_hide_emoji.setVisibility(View.VISIBLE);
    }



    private void hideEmoji() {
        FrameLayout emojicons = (FrameLayout) findViewById(R.id.emojicons);
        emojicons.setVisibility(View.GONE);
        btn_show_emoji.setVisibility(View.VISIBLE);
        btn_hide_emoji.setVisibility(View.GONE);
    }


    private void openImageIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory()+ AppConfig.DIR_FEED_IMAGE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == PICK_IMAGE) {
                final boolean isCamera;
                if(data == null || data.getData() == null) {
                    isCamera = true;
                }
                else {
                    final String action = data.getAction();
                    if(action == null) {
                        isCamera = false;
                    }
                    else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                Uri selectedImageUri;
                if(isCamera) {
                    selectedImageUri = outputFileUri;
                    decodeFile(selectedImageUri.getPath());
                }
                else {
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
            Toast.makeText(getApplicationContext(),"Long press to remove" , Toast.LENGTH_LONG).show();
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

        image_post_feed.setImageBitmap(bitmap);
        imgPath = filePath;

       /* ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();

        image_file = Base64.encodeToString(data, 0);//encodeBytes(data);*/
    }


    private void done() {

        String msg = input_post_feed.getText().toString().trim();

        if(msg.length()!=0 || imgPath.length()!=0) {
            if (cd.isConnectingToInternet()) {

                progressBar.setVisibility(View.VISIBLE);
                feed_done_btn.setVisibility(View.GONE);

                Intent intent = new Intent(this, HttpService.class);

                if (intent_type.equals("edit")) {
                    intent.putExtra("intent_type", AppConfig.httpIntentEditFeed);
                    intent.putExtra("feedId", feedId);
                } else
                    intent.putExtra("intent_type", AppConfig.httpIntentPostFeed);

                intent.putExtra("msg", msg);
                intent.putExtra("img_path", imgPath);
                startService(intent);
            } else
                showSnackBar(getString(R.string.err_no_internet));
        }
    }

    private void cancel() {
        finish();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void setUpFeed(String message,String image) {

        if (!TextUtils.isEmpty(message)) {

            Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
            String atMentionScheme = "mention://";

            Pattern HashPattern = Pattern.compile("#([A-Za-z0-9_]+)");
            String HashScheme = "hash://";

            Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
                //skip the first character to filter out '@'
                public String transformUrl(final Matcher match, String url) {
                    return match.group(1);
                }
            };

            this.input_post_feed.setText(message);

            Linkify.addLinks(this.input_post_feed, atMentionPattern, atMentionScheme, null, transformFilter);
            Linkify.addLinks(this.input_post_feed, HashPattern, HashScheme, null, transformFilter);

            //StripUnderline.stripUnderlines(this.statusMsg);
        }

        if (image.length()>0) {
            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();
            this.image_post_feed.setImageUrl(image+AppConfig.AUTO_REF_HACK(), imageLoader);
        }

        isReady = true;
    }


        private void displayFeed(final String feedId) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DISPLAY_FEED, new Response.Listener<String>() {

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
                        JSONArray feedArray = dataObj.getJSONArray("feed");
                        JSONObject feedObj = (JSONObject) feedArray.get(0);

                        int id = feedObj.getInt("id");
                        String feed_message = feedObj.getString("message");
                        String feed_image = feedObj.getString("image");

                        setUpFeed(feed_message, feed_image);

                    } else {

                    }



                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();


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
                params.put("feedId", feedId);
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



    private void loadTagPopup(final String query, final String type) {

        // making fresh volley request and getting json
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOAD_TAG_POPUP, new Response.Listener<String>() {

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
                        parseJsonTag(dataObj);

                    } else {

                    }



                } catch (JSONException e) {
                    Toast.makeText(getApplication().getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    //showSnackBar(0, getString(R.string.err_network_timeout));
                } else if (error instanceof AuthFailureError) {
                    //TODO
                } else if (error instanceof ServerError) {
                    //TODO
                } else if (error instanceof NetworkError) {
                    //TODO
                } else if (error instanceof ParseError) {
                    //TODO
                }

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
                params.put("query", query);
                params.put("type", type);

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


    /**
     * Parsing json response and passing the data to listPopupWindow adapter
     */
    private void parseJsonTag(JSONObject response) {
        try {
            JSONArray popupArray = response.getJSONArray("tag");

            if (popupArray.length() > 0) {
                listPopupItems.clear();
                listPopupWindow.show();
            }else
                listPopupWindow.dismiss();


            for (int i = 0; i < popupArray.length(); i++) {
                JSONObject feedObj = (JSONObject) popupArray.get(i);

                ListPopupItem item = new ListPopupItem();
                item.setType(feedObj.getInt("type"));
                item.setTitle(feedObj.getString("title"));
                item.setSubTitle(feedObj.getString("subtitle"));

                listPopupItems.add(item);
            }

            // notify data changes to feed adapater
            listPopupAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if(!isPopUp) {

                String text = charSequence.toString();

                if (text.lastIndexOf("@") > text.lastIndexOf(" ")) {
                    String tag = text.substring(text.lastIndexOf("@"), text.length());
                    if (tag.length() > 0)
                        loadTagPopup(tag, "mention");
                } else if (text.lastIndexOf("#") > text.lastIndexOf(" ")) {

                    String tag = text.substring(text.lastIndexOf("#"), text.length());
                    if (tag.length() > 0)
                        loadTagPopup(tag, "hash");
                }

            }
            isPopUp = false;


        }

        public void afterTextChanged(Editable editable) {

        }
    }




    //SnackBar function
    private void showSnackBar(String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.post_feed_CoordinatorLayout);

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
                        displayFeed(feedId);
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
