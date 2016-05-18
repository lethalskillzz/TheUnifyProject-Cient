package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.theunifyproject.lethalskillzz.util.Logout;
import net.theunifyproject.lethalskillzz.util.StripUnderline;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

public class DisplayShopActivity extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG = DisplayShopActivity.class.getSimpleName();
    public static Handler mUiHandler;
    private PrefManager pref;
    private ConnectionDetector cd;
    private Logout mLogout;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private boolean isReady;
    private String shopId;
    private String mobile;

    private FeedImageView image;
    private TextView title, condition, price, description, username;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_shop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.display_shop_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogout = new Logout(this);
        pref = new PrefManager(this);
        //profile = pref.getUserDetails();

        Intent intent = getIntent();
        shopId = intent.getStringExtra("shopId");

        // creating connection detector class instance
        cd = new ConnectionDetector(this);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();


        image = (FeedImageView) findViewById(R.id.collapsible_image);
        title = (TextView) findViewById(R.id.display_shop_title);
        condition = (TextView) findViewById(R.id.display_shop_condition);
        price = (TextView) findViewById(R.id.display_shop_price);
        description = (TextView) findViewById(R.id.display_shop_description);
        username = (TextView) findViewById(R.id.display_shop_username);
        fab = (FloatingActionButton) findViewById(R.id.display_shop_fab);

        image.setDefaultImageResId(R.drawable.ic_image);
        image.setImageUrl("http://testing.null", imageLoader);

        fab.setOnClickListener(this);

        if (cd.isConnectingToInternet()) {
            displayShopping(shopId);
        } else {
            showSnackBar(0, getString(R.string.err_no_internet));
        }

    }

    @Override
    public void onClick(View view) {
        if(isReady) {
            switch (view.getId()) {
                case R.id.display_shop_fab: {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setPackage("com.android.server.telecom");
                    callIntent.setData(Uri.parse("tel:" + mobile));
                    startActivity(callIntent );
                }
                break;
            }
        }
    }


    private void setUpShop(String img, String condition, String price, String title, String description, String username) {

        if(img.length()>0) {
            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();
            this.image.setImageUrl(img+AppConfig.AUTO_REF_HACK(), imageLoader);
            this.image.setResponseObserver(new FeedImageView.ResponseObserver() {
                @Override
                public void onError() {
                    image.setImageResource(R.drawable.ic_image);
                }

                @Override
                public void onSuccess() {
                }
            });
        } else
            image.setImageResource(R.drawable.ic_image);

        this.condition.setText(condition);
        this.price.setText(price);
        this.title.setText(title);
        this.description.setText(description);


        Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
        String atMentionScheme = "mention://";

        Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
            //skip the first character to filter out '@'
            public String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };

        this.username.setText("@"+username);
        Linkify.addLinks(this.username, atMentionPattern, atMentionScheme, null, transformFilter);

        StripUnderline.stripUnderlines(this.username);

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
                        mobile = feedObj.getString("mobile");
                        String image = feedObj.getString("image");
                        String condition = feedObj.getString("condition");
                        String price = feedObj.getString("price");
                        String title = feedObj.getString("title");
                        String description = feedObj.getString("description");
                        String username = feedObj.getString("username");

                        setUpShop(image, condition, price, title, description, username);

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

    //SnackBar function
    private void showSnackBar(final int id, String msg) {

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.display_shop_CoordinatorLayout);

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